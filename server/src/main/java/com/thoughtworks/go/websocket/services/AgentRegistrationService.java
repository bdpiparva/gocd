/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.websocket.services;

import com.thoughtworks.go.config.AgentConfig;
import com.thoughtworks.go.config.ErrorCollector;
import com.thoughtworks.go.config.GoConfigDao;
import com.thoughtworks.go.config.exceptions.GoConfigInvalidException;
import com.thoughtworks.go.config.update.UpdateEnvironmentsCommand;
import com.thoughtworks.go.config.update.UpdateResourceCommand;
import com.thoughtworks.go.domain.AgentInstance;
import com.thoughtworks.go.domain.AllConfigErrors;
import com.thoughtworks.go.domain.ConfigErrors;
import com.thoughtworks.go.security.Registration;
import com.thoughtworks.go.security.RegistrationJSONizer;
import com.thoughtworks.go.server.service.*;
import com.thoughtworks.go.server.service.result.HttpOperationResult;
import com.thoughtworks.go.websocket.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static com.thoughtworks.go.websocket.ErrorCode.CONFLICT;
import static com.thoughtworks.go.websocket.ErrorCode.FORBIDDEN;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Service
public class AgentRegistrationService {
    private static final Logger LOG = LoggerFactory.getLogger(AgentRegistrationService.class);
    private final GoConfigService goConfigService;
    private final AgentService agentService;
    private final AgentConfigService agentConfigService;
    private Mac mac;

    @Autowired
    public AgentRegistrationService(GoConfigService goConfigService, AgentService agentService, AgentConfigService agentConfigService) {
        this.goConfigService = goConfigService;
        this.agentService = agentService;
        this.agentConfigService = agentConfigService;
    }

    public WebSocketMessage getToken(String guid) {
        if (StringUtils.isBlank(guid)) {
            String message = "UUID cannot be blank.";
            LOG.error("Rejecting request for token. Error: ErrorCode=[{}] Message=[{}] GUID=[{}]", CONFLICT, message, guid);
            return ErrorMessage.conflict(message);
        }

        final AgentInstance agentInstance = agentService.findAgent(guid);
        if ((!agentInstance.isNullAgent() && agentInstance.isPending()) || goConfigService.hasAgent(guid)) {
            String message = "A token has already been issued for this agent.";
            LOG.error("Rejecting request for token. Error: HttpCode=[{}] Message=[{}] Pending=[{}] UUID=[{}]",
                    HttpStatus.CONFLICT, message, agentInstance.isPending(), guid);
            return ErrorMessage.conflict(message);
        }
        return new DefaultMessage().with("token", encodeBase64String(hmac().doFinal(guid.getBytes())));
    }

    public WebSocketMessage registerAgent(String agentIp, RegisterAgentMessage registerAgentMessage) {
        final AgentRuntimeInfoMessage info = registerAgentMessage.getAgentRuntimeInfoMessage();
        LOG.debug("Processing registration request from agent [{}/{}]", info.getHostname(), agentIp);

        String preferredHostname = info.getHostname();

        try {
            if (!encodeBase64String(hmac().doFinal(info.getGuid().getBytes())).equals(registerAgentMessage.getToken())) {
                String message = "Not a valid token.";
                LOG.error("Rejecting request for registration. Error: HttpCode=[{}] Message=[{}] UUID=[{}] Hostname=[{}] ElasticAgentID=[{}] PluginID=[{}]", FORBIDDEN, message, info.getGuid(), info.getHostname(), info.getElasticAgentId(), info.getElasticPluginId());
                return ErrorMessage.forbidden(message);
            }

            if (goConfigService.serverConfig().shouldAutoRegisterAgentWith(info.getAgentAutoRegisterKey())) {
                preferredHostname = getPreferredHostname(info.getAgentAutoRegisterHostname(), info.getHostname());
            } else {
                if (elasticAgentAutoregistrationInfoPresent(info)) {
                    String message = String.format("Elastic agent registration requires an auto-register agent key to be setup on the server. Agent-id: [%s], Plugin-id: [%s]", info.getElasticAgentId(), info.getElasticPluginId());
                    LOG.error("Rejecting request for registration. Error: HttpCode=[{}] Message=[{}] UUID=[{}] Hostname=[{}] ElasticAgentID=[{}] PluginID=[{}]", UNPROCESSABLE_ENTITY, message, info.getGuid(), info.getHostname(), info.getElasticAgentId(), info.getElasticPluginId());
                    return ErrorMessage.unprocessable(message);
                }
            }

            if (partialElasticAgentAutoregistrationInfo(info.getElasticAgentId(), info.getElasticPluginId())) {
                String message = "Elastic agents must submit both elasticAgentId and elasticPluginId.";
                LOG.error("Rejecting request for registration. Error: HttpCode=[{}] Message=[{}] UUID=[{}] Hostname=[{}] ElasticAgentID=[{}] PluginID=[{}]", UNPROCESSABLE_ENTITY, message, info.getGuid(), info.getHostname(), info.getElasticAgentId(), info.getElasticPluginId());
                return ErrorMessage.unprocessable(message);
            }

            if (elasticAgentIdAlreadyRegistered(info)) {
                String message = "Duplicate Elastic agent Id used to register elastic agent.";
                LOG.error("Rejecting request for registration. Error: HttpCode=[{}] Message=[{}] UUID=[{}] Hostname=[{}] ElasticAgentID=[{}] PluginID=[{}]", UNPROCESSABLE_ENTITY, message, info.getGuid(), info.getHostname(), info.getElasticAgentId(), info.getElasticPluginId());
                return ErrorMessage.unprocessable(message);
            }

            AgentConfig agentConfig = new AgentConfig(info.getGuid(), preferredHostname, agentIp);

            if (elasticAgentAutoregistrationInfoPresent(info)) {
                agentConfig.setElasticAgentId(info.getElasticAgentId());
                agentConfig.setElasticPluginId(info.getElasticPluginId());
            }

            if (goConfigService.serverConfig().shouldAutoRegisterAgentWith(info.getAgentAutoRegisterKey()) && !goConfigService.hasAgent(info.getGuid())) {
                LOG.info("[Agent Auto Registration] Auto registering agent with uuid {} ", info.getGuid());
                GoConfigDao.CompositeConfigCommand compositeConfigCommand = new GoConfigDao.CompositeConfigCommand(
                        new AgentConfigService.AddAgentCommand(agentConfig),
                        new UpdateResourceCommand(info.getGuid(), info.getAgentAutoRegisterResources()),
                        new UpdateEnvironmentsCommand(info.getGuid(), info.getAgentAutoRegisterEnvironments())
                );
                HttpOperationResult result = new HttpOperationResult();
                agentConfig = agentConfigService.updateAgent(compositeConfigCommand, info.getGuid(), result, agentService.agentUsername(info.getGuid(), agentIp, preferredHostname));
                if (!result.isSuccess()) {
                    List<ConfigErrors> errors = ErrorCollector.getAllErrors(agentConfig);
                    ConfigErrors e = new ConfigErrors();
                    e.add("resultMessage", result.detailedMessage());
                    errors.add(e);
                    throw new GoConfigInvalidException(null, new AllConfigErrors(errors).asString());
                }
            }

            boolean registeredAlready = goConfigService.hasAgent(info.getGuid());
            long usableSpace = Long.parseLong(info.getUsablespace());

            AgentRuntimeInfo agentRuntimeInfo = AgentRuntimeInfo.fromServer(agentConfig, registeredAlready, info.getLocation(), usableSpace, info.getOperatingSystem(), false);

            if (elasticAgentAutoregistrationInfoPresent(info)) {
                agentRuntimeInfo = ElasticAgentRuntimeInfo.fromServer(agentRuntimeInfo, info.getElasticAgentId(), info.getElasticPluginId());
            }

            Registration keyEntry = agentService.requestRegistration(agentService.agentUsername(info.getGuid(), agentIp, preferredHostname), agentRuntimeInfo);
            return RegistrationMessage.create(RegistrationJSONizer.toJson(keyEntry));
        } catch (Exception e) {
            LOG.error("Error occurred during agent registration process. Error: HttpCode=[{}] Message=[{}] UUID=[{}] Hostname=[{}] ElasticAgentID=[{}] PluginID=[{}]", UNPROCESSABLE_ENTITY, getErrorMessage(e), info.getGuid(),
                    info.getHostname(), info.getElasticAgentId(), info.getElasticPluginId(), e);
            return ErrorMessage.unprocessable(String.format("Error occurred during agent registration process: %s", getErrorMessage(e)));
        }
    }

    private String getPreferredHostname(String agentAutoRegisterHostname, String hostname) {
        return isNotBlank(agentAutoRegisterHostname) ? agentAutoRegisterHostname : hostname;
    }

    private boolean partialElasticAgentAutoregistrationInfo(String elasticAgentId, String elasticPluginId) {
        return (isBlank(elasticAgentId) && isNotBlank(elasticPluginId)) || (isNotBlank(elasticAgentId) && isBlank(elasticPluginId));
    }

    private boolean elasticAgentAutoregistrationInfoPresent(AgentRuntimeInfoMessage agentRuntimeInfoMessage) {
        return isNotBlank(agentRuntimeInfoMessage.getElasticAgentId()) &&
                isNotBlank(agentRuntimeInfoMessage.getElasticPluginId());
    }

    private boolean elasticAgentIdAlreadyRegistered(AgentRuntimeInfoMessage agentRuntimeInfoMessage) {
        return agentService.findElasticAgent(agentRuntimeInfoMessage.getElasticAgentId(), agentRuntimeInfoMessage.getElasticPluginId()) != null;
    }


    private Mac hmac() {
        if (mac == null) {
            try {
                mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKey = new SecretKeySpec(goConfigService.serverConfig().getTokenGenerationKey().getBytes(), "HmacSHA256");
                mac.init(secretKey);
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }
        return mac;
    }

    private String getErrorMessage(Exception e) {
        if (e instanceof GoConfigInvalidException) {
            return ((GoConfigInvalidException) e).getAllErrorMessages();
        }
        return e.getMessage();
    }
}

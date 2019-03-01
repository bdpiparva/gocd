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

package com.thoughtworks.go.agent.websocket;

import com.thoughtworks.go.agent.AgentAutoRegistrationPropertiesImpl;
import com.thoughtworks.go.agent.UrlBasedArtifactsRepository;
import com.thoughtworks.go.agent.websocket.services.AgentGuidFileService;
import com.thoughtworks.go.agent.websocket.services.ConfigDirectoryService;
import com.thoughtworks.go.agent.websocket.services.TokenFileService;
import com.thoughtworks.go.server.service.AgentRuntimeInfo;
import com.thoughtworks.go.util.SystemEnvironment;
import com.thoughtworks.go.util.SystemUtil;
import com.thoughtworks.go.websocket.AgentRuntimeInfoMessage;
import com.thoughtworks.go.websocket.RegisterAgentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.util.UUID;

@Controller
@EnableScheduling
public class AgentWebSocketClientController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlBasedArtifactsRepository.class);
    private final GoAgentWebSocketClient client;
    private final TokenFileService tokenFileService;
    private final AgentGuidFileService agentGuidFileService;
    private final ConfigDirectoryService configDirectoryService;

    @Autowired
    public AgentWebSocketClientController(GoAgentWebSocketClient client,
                                          TokenFileService tokenFileService,
                                          AgentGuidFileService agentGuidFileService,
                                          ConfigDirectoryService configDirectoryService) {
        this.client = client;
        this.tokenFileService = tokenFileService;
        this.agentGuidFileService = agentGuidFileService;
        this.configDirectoryService = configDirectoryService;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void loop() {
        LOGGER.debug("In loop thread.");
        client.tryEstablishingConnection();
        retrieveTokenIfNecessary();
        registerIfNecessary();
        LOGGER.debug("Done executing loop thread.");
    }

    private void retrieveTokenIfNecessary() {
        if (tokenFileService.dataPresent()) {
            LOGGER.debug("Token already exist.");
            return;
        }

        if (!agentGuidFileService.dataPresent()) {
            agentGuidFileService.store(UUID.randomUUID().toString());
        }

        client.retrieveToken(agentGuidFileService.load());
    }


    private void registerIfNecessary() {
        if (registered()) {
            LOGGER.debug("Agent is already registered with server.");
            return;
        }

        if (!tokenFileService.dataPresent()) {
            LOGGER.debug("Waiting for token.");
            return;
        }

        LOGGER.info("Registering agent with server.");

        final AgentAutoRegistrationPropertiesImpl agentAutoRegistrationProperties = new AgentAutoRegistrationPropertiesImpl(new File(configDirectoryService.webSocketConfigDir(), "autoregister.properties"));
        final AgentRuntimeInfoMessage agentRuntimeInfoMessage = new AgentRuntimeInfoMessage()
                .guid(agentGuidFileService.load())
                .hostname(SystemUtil.getLocalhostNameOrRandomNameIfNotFound())
                .location(SystemUtil.currentWorkingDirectory())
                .usablespace(String.valueOf(AgentRuntimeInfo.usableSpace(SystemUtil.currentWorkingDirectory())))
                .operatingSystem(new SystemEnvironment().getOperatingSystemCompleteName())
                .agentAutoRegisterKey(agentAutoRegistrationProperties.agentAutoRegisterKey())
                .agentAutoRegisterResources(agentAutoRegistrationProperties.agentAutoRegisterResources())
                .agentAutoRegisterEnvironments(agentAutoRegistrationProperties.agentAutoRegisterEnvironments())
                .agentAutoRegisterHostname(agentAutoRegistrationProperties.agentAutoRegisterHostname())
                .elasticAgentId(agentAutoRegistrationProperties.agentAutoRegisterElasticAgentId())
                .elasticPluginId(agentAutoRegistrationProperties.agentAutoRegisterElasticPluginId());

        final RegisterAgentMessage registerAgentMessage = new RegisterAgentMessage();
        registerAgentMessage.setAgentRuntimeInfoMessage(agentRuntimeInfoMessage);
        registerAgentMessage.setToken(tokenFileService.load());
        client.registerAgent(registerAgentMessage);
    }

    private boolean registered() {
        return false;
    }
}

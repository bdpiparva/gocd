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

package com.thoughtworks.go.apiv1.securityauthconfig;

import com.thoughtworks.go.api.ApiController;
import com.thoughtworks.go.api.ApiVersion;
import com.thoughtworks.go.api.CrudController;
import com.thoughtworks.go.api.base.OutputWriter;
import com.thoughtworks.go.api.representers.JsonReader;
import com.thoughtworks.go.api.spring.ApiAuthenticationHelper;
import com.thoughtworks.go.api.util.GsonTransformer;
import com.thoughtworks.go.apiv1.securityauthconfig.representers.AuthorizationExtensionRepresenter;
import com.thoughtworks.go.apiv1.securityauthconfig.representers.CreateUserRepresenter;
import com.thoughtworks.go.apiv1.securityauthconfig.representers.SecurityAuthConfigRepresenter;
import com.thoughtworks.go.apiv1.securityauthconfig.representers.VerifyConnectionResponseRepresenter;
import com.thoughtworks.go.config.SecurityAuthConfig;
import com.thoughtworks.go.config.builder.ConfigurationPropertyBuilder;
import com.thoughtworks.go.config.exceptions.EntityType;
import com.thoughtworks.go.config.exceptions.HttpException;
import com.thoughtworks.go.domain.config.ConfigurationProperty;
import com.thoughtworks.go.plugin.access.authorization.AuthorizationExtension;
import com.thoughtworks.go.plugin.domain.common.CombinedPluginInfo;
import com.thoughtworks.go.plugin.domain.common.PluginConstants;
import com.thoughtworks.go.plugin.domain.common.PluginInfo;
import com.thoughtworks.go.plugin.domain.common.VerifyConnectionResponse;
import com.thoughtworks.go.server.service.EntityHashingService;
import com.thoughtworks.go.server.service.SecurityAuthConfigService;
import com.thoughtworks.go.server.service.plugins.builder.DefaultPluginInfoFinder;
import com.thoughtworks.go.server.service.result.HttpLocalizedOperationResult;
import com.thoughtworks.go.spark.Routes;
import com.thoughtworks.go.spark.spring.SparkSpringController;
import com.thoughtworks.go.util.SystemEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

import static spark.Spark.*;

@Component
public class SecurityAuthConfigInternalControllerV1 extends ApiController implements SparkSpringController, CrudController<SecurityAuthConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuthConfigInternalControllerV1.class);
    private SecurityAuthConfigService securityAuthConfigService;
    private final ApiAuthenticationHelper apiAuthenticationHelper;
    private EntityHashingService entityHashingService;
    private AuthorizationExtension authorizationExtension;
    private SystemEnvironment systemEnvironment;
    private DefaultPluginInfoFinder pluginInfoFinder;

    @Autowired
    public SecurityAuthConfigInternalControllerV1(SecurityAuthConfigService securityAuthConfigService,
                                                  ApiAuthenticationHelper apiAuthenticationHelper,
                                                  EntityHashingService entityHashingService,
                                                  AuthorizationExtension authorizationExtension,
                                                  SystemEnvironment systemEnvironment,
                                                  DefaultPluginInfoFinder pluginInfoFinder) {
        super(ApiVersion.v1);
        this.securityAuthConfigService = securityAuthConfigService;
        this.apiAuthenticationHelper = apiAuthenticationHelper;
        this.entityHashingService = entityHashingService;
        this.authorizationExtension = authorizationExtension;
        this.systemEnvironment = systemEnvironment;
        this.pluginInfoFinder = pluginInfoFinder;
    }

    @Override
    public String controllerBasePath() {
        return Routes.SecurityAuthConfigAPI.INTERNAL_BASE;
    }

    @Override
    public void setupRoutes() {
        path(controllerBasePath(), () -> {
            before(Routes.SecurityAuthConfigAPI.VERIFY_CONNECTION, mimeType, this::setContentType);
            before(Routes.SecurityAuthConfigAPI.SETUP_PASSWORD_FILE, mimeType, this::setContentType);
            before(Routes.SecurityAuthConfigAPI.PASSWORD_FILE_PLUGIN_INFO, mimeType, this::setContentType);

            get(Routes.SecurityAuthConfigAPI.PASSWORD_FILE_PLUGIN_INFO, mimeType, this::passwordFilePluginInfo);
            post(Routes.SecurityAuthConfigAPI.VERIFY_CONNECTION, mimeType, this::verifyConnection);
            post(Routes.SecurityAuthConfigAPI.SETUP_PASSWORD_FILE, mimeType, this::setupNewPasswordFile);

            exception(HttpException.class, this::httpException);
        });
    }

    public String passwordFilePluginInfo(Request request, Response response) throws IOException {
        CombinedPluginInfo pluginInfos = pluginInfoFinder.pluginInfoFor("cd.go.authentication.passwordfile");
        PluginInfo pluginInfo = pluginInfos.extensionFor(PluginConstants.AUTHORIZATION_EXTENSION);
        return writerForTopLevelObject(request, response, writer -> new AuthorizationExtensionRepresenter().toJSON(writer, pluginInfo));
    }

    public String setupNewPasswordFile(Request request, Response response) throws IOException {
        CreateUserRequest createUserRequest = CreateUserRepresenter.fromJSON(GsonTransformer.getInstance().jsonReaderFrom(request.body()));
        String pluginId = "cd.go.authentication.passwordfile";

        ConfigurationPropertyBuilder builder = new ConfigurationPropertyBuilder();
        String passwordFilePath = systemEnvironment.getConfigDir() + "/password.properties";

        ConfigurationProperty configurationProperty = builder.create("PasswordFilePath", passwordFilePath, null, false);
        SecurityAuthConfig securityAuthConfig = new SecurityAuthConfig(UUID.randomUUID().toString(), pluginId, configurationProperty);

        try {
            authorizationExtension.addUser(pluginId, createUserRequest.getConfigurationProperties().getConfigurationAsMap(true), securityAuthConfig);
            securityAuthConfigService.create(currentUsername(), securityAuthConfig, new HttpLocalizedOperationResult());
            return writerForTopLevelObject(request, response, writer -> writer.add("message", "User added successfully"));
        } catch (Exception e) {
            LOGGER.error("Failed to setup up security: ", e);
            response.status(422);
            return writerForTopLevelObject(request, response, writer -> writer.add("message", "Failed to save user"));
        }
    }

    public String verifyConnection(Request request, Response response) throws IOException {
        SecurityAuthConfig securityAuthConfig = buildEntityFromRequestBody(request);
        VerifyConnectionResponse verifyConnectionResponse = securityAuthConfigService.verifyConnection(securityAuthConfig);

        if (!verifyConnectionResponse.isSuccessful()) {
            response.status(422);
        }

        return writerForTopLevelObject(request, response, writer -> VerifyConnectionResponseRepresenter.toJSON(writer, verifyConnectionResponse, securityAuthConfig));
    }

    @Override
    public String etagFor(SecurityAuthConfig entityFromServer) {
        return entityHashingService.md5ForEntity(entityFromServer);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SecurityAuthConfig;
    }

    @Override
    public SecurityAuthConfig doFetchEntityFromConfig(String id) {
        return securityAuthConfigService.findProfile(id);
    }

    @Override
    public SecurityAuthConfig buildEntityFromRequestBody(Request request) {
        JsonReader jsonReader = GsonTransformer.getInstance().jsonReaderFrom(request.body());
        return SecurityAuthConfigRepresenter.fromJSON(jsonReader);
    }

    @Override
    public Consumer<OutputWriter> jsonWriter(SecurityAuthConfig securityAuthConfig) {
        return outputWriter -> SecurityAuthConfigRepresenter.toJSON(outputWriter, securityAuthConfig);
    }
}

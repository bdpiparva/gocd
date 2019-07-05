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

package com.thoughtworks.go.api.support;

import com.thoughtworks.go.api.ControllerMethods;
import com.thoughtworks.go.server.service.support.ServerStatusService;
import com.thoughtworks.go.spark.Routes;
import com.thoughtworks.go.spark.SparkController;
import com.thoughtworks.go.spark.spring.SparkSpringController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static spark.Spark.*;

@Component
public class ApiSupportController implements SparkController, ControllerMethods, SparkSpringController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiSupportController.class);
    private ServerStatusService serverStatusService;

    @Autowired
    public ApiSupportController(ServerStatusService serverStatusService) {
        this.serverStatusService = serverStatusService;
    }

    @Override
    public String controllerBasePath() {
        return Routes.Support.BASE;
    }

    @Override
    public void setupRoutes() {
        path(controllerPath(), () -> {
            before("", APPLICATION_JSON_VALUE, this::setContentType);
            get("", APPLICATION_JSON_VALUE, this::show);
        });
    }

    private void setContentType(Request request, Response response) {
        response.type(APPLICATION_JSON_VALUE);
    }

    public String show(Request request, Response response) throws IOException {
        return writerForTopLevelObject(request, response, outputWriter -> {
            ServerInfoWriterDelegator serverInfoWriter = new ServerInfoWriterDelegator(outputWriter);
            try {
                serverStatusService.serverInfo(serverInfoWriter);
            } catch (Exception e) {
                LOGGER.error("Failed to generate api support json:", e);
                outputWriter.add("message", "Failed to generate api support json. Please look at the 'go-server.log' for more details.");
                response.status(422);
            }
        });
    }

}

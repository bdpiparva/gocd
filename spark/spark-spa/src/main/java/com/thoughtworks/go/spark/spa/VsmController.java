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

package com.thoughtworks.go.spark.spa;

import com.thoughtworks.go.domain.materials.MaterialConfig;
import com.thoughtworks.go.server.service.GoConfigService;
import com.thoughtworks.go.spark.Routes;
import com.thoughtworks.go.spark.SparkController;
import com.thoughtworks.go.spark.spring.SPAAuthenticationHelper;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.TemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class VsmController implements SparkController {
    private final SPAAuthenticationHelper authenticationHelper;
    private final TemplateEngine engine;
    private final GoConfigService goConfigService;

    public VsmController(SPAAuthenticationHelper authenticationHelper, TemplateEngine engine, GoConfigService goConfigService) {
        this.authenticationHelper = authenticationHelper;
        this.engine = engine;
        this.goConfigService = goConfigService;
    }

    @Override
    public String controllerBasePath() {
        return Routes.Vsm.SPA_BASE;
    }

    @Override
    public void setupRoutes() {
        path(controllerBasePath(), () -> {
            before("", authenticationHelper::checkAdminUserAnd403);
            get(Routes.Vsm.PIPELINE_VSM, this::pipelineVMS, engine);
            get(Routes.Vsm.MATERIAL_VSM, this::materialVMS, engine);
        });
    }

    private ModelAndView pipelineVMS(Request request, Response response) {
        String pipelineName = request.params("pipeline_name");
        String pipelineCounter = request.params("pipeline_counter");
        Map<String, String> entity = new HashMap<>();
        entity.put("name", pipelineName);
        entity.put("counter", pipelineCounter);
        
        Map<Object, Object> object = new HashMap<Object, Object>() {{
            put("viewTitle", "Value Stream Map");
            put("meta", getMeta("pipeline", entity));
        }};
        return new ModelAndView(object, null);
    }

    private ModelAndView materialVMS(Request request, Response response) {
        String materialFingerprint = request.params("material_fingerprint");
        String materialRevision = request.params("revision");

        MaterialConfig materialConfig = goConfigService.getCurrentConfig().materialConfigFor(materialFingerprint);
        Map<String, String> entity = new HashMap<>();
        entity.put("name", materialConfig.getDisplayName());
        entity.put("fingerprint", materialFingerprint);
        entity.put("revision", materialRevision);

        Map<Object, Object> object = new HashMap<Object, Object>() {{
            put("viewTitle", "Value Stream Map");
            put("meta", getMeta("material", entity));
        }};
        return new ModelAndView(object, null);
    }

    private Map<String, Object> getMeta(String type, Map<String, String> entity) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("type", type);
        meta.put("entity", entity);
        return meta;
    }
}

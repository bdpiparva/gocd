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

import {SparkRoutes} from "helpers/spark_routes";
import {VsmService} from "../vsm_service";

describe("VsmService", () => {
  beforeEach(() => jasmine.Ajax.install());
  afterEach(() => jasmine.Ajax.uninstall());
  const vsmService = new VsmService();

  it("should make get request for pipeline vsm", () => {
    vsmService.getPipelineVsm("up42", 1);

    const request = jasmine.Ajax.requests.mostRecent();
    expect(request.url).toEqual(SparkRoutes.apiPipelineVsmPath("up42", 1));
    expect(request.method).toEqual("GET");
    expect(request.requestHeaders.Accept).toEqual("application/vnd.go.cd.v1+json");
  });

  it("should make get request for pipeline vsm", () => {
    vsmService.getMaterialVsm("fingerprint", "revision");

    const request = jasmine.Ajax.requests.mostRecent();
    expect(request.url).toEqual(SparkRoutes.apiMaterialVsmPath("fingerprint", "revision"));
    expect(request.method).toEqual("GET");
    expect(request.requestHeaders.Accept).toEqual("application/vnd.go.cd.v1+json");
  });
});

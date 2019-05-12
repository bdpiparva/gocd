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

import {ApiRequestBuilder, ApiResult, ApiVersion, ObjectWithEtag} from "helpers/api_request_builder";
import {SetupSecurity} from "models/setup_security/setup_security";
import {AuthorizationSettings} from "models/shared/plugin_infos_new/extensions";

export class SetupSecurityCrud {
  static save(setupSecurity: SetupSecurity) {
    return ApiRequestBuilder.POST("/go/api/admin/internal/security/auth_configs/setup_password_file",
                                  ApiVersion.v1,
                                  {payload: setupSecurity.toJSON()});
  }

  static passwordFilePluginInfo() {
    return ApiRequestBuilder.GET("/go/api/admin/internal/security/auth_configs/password_file_plugin_info",
                                 ApiVersion.v1).then(SetupSecurityCrud.extractPluginInfo);
  }

  private static extractPluginInfo(result: ApiResult<string>) {
    return result.map((body) => {
      const authorizationSettings = AuthorizationSettings.fromJSON(JSON.parse(body));
      return {
        object: authorizationSettings,
        etag: result.getEtag()
      } as ObjectWithEtag<AuthorizationSettings>;
    });
  }
}

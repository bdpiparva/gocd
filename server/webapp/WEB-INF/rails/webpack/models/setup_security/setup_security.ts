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

import {Stream} from "mithril/stream";
import * as stream from "mithril/stream";
import {AuthConfig} from "models/auth_configs/auth_configs";
import {ValidatableMixin} from "models/mixins/new_validatable_mixin";
import {SetupSecurityCrud} from "models/setup_security/setup_security_crud";
import {PlainTextValue} from "models/shared/config_value";
import {Configuration, Configurations} from "models/shared/configuration";

export class SetupSecurity extends ValidatableMixin {
  private readonly username: Stream<string>;
  private readonly path: Stream<string>;
  private readonly properties: Configurations;

  constructor() {
    super();
    this.properties = new Configurations([]);
    this.username   = stream(this.properties.valueFor("Username"));
    this.path       = stream("");
    this.validatePresenceOf("path");
  }

  getUsernameAsString() {
    return this.username();
  }

  getPathAsString() {
    return this.path();
  }

  getPath() {
    return this.path;
  }

  getProperties() {
    return this.properties;
  }

  authConfig() {
    this.isValid();
    const configuration = new Configuration("PasswordFilePath",
                                            new PlainTextValue(this.getPathAsString()));
    return new AuthConfig("password-file",
                          "cd.go.authentication.passwordfile",
                          new Configurations([configuration]));
  }

  save() {
    return SetupSecurityCrud.save(this);
  }

  toJSON() {
    return {properties: this.properties.toJSON()};
  }
}

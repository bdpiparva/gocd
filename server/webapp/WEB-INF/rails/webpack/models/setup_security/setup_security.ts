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
import {Stream} from "mithril/stream";
import * as stream from "mithril/stream";
import {AuthConfig, AuthConfigJSON} from "models/auth_configs/auth_configs";
import {ValidatableMixin, Validator} from "models/mixins/new_validatable_mixin";
import {PlainTextValue} from "models/shared/config_value";
import {Configuration, Configurations} from "models/shared/configuration";
import {EncryptedValue} from "views/components/forms/encrypted_value";

class SetupSecurityValidator extends Validator {
  private setupSecurity: SetupSecurity;

  constructor(setupSecurity: SetupSecurity) {
    super();
    this.setupSecurity = setupSecurity;
  }

  protected doValidate(entity: any, attr: string): void {
    if (this.setupSecurity.getUsernameAsString().length === 0) {
      entity.errors().add("username", "Username must not be blank");
    }

    if (this.setupSecurity.getPasswordAsString().length < 8) {
      entity.errors().add("password", "Use 8 characters or more for your password");
    }

    if (this.setupSecurity.getPasswordAsString() !== this.setupSecurity.getConfirmPasswordAsString()) {
      entity.errors()
            .add("confirm_password", "Those passwords didn't match. Try again.");
    }
  }
}

export class SetupSecurity extends ValidatableMixin {
  private readonly username: Stream<string>;
  private readonly password: Stream<EncryptedValue>;
  private readonly __confirmPassword: Stream<EncryptedValue>;
  private readonly path: Stream<string>;

  constructor() {
    super();
    ValidatableMixin.call(this);
    this.username          = stream("");
    this.password          = stream(new EncryptedValue({clearText: ""}));
    this.__confirmPassword = stream(new EncryptedValue({clearText: ""}));
    this.path              = stream("");
    this.validateWith(new SetupSecurityValidator(this), "__confirmPassword");
  }

  getUsername() {
    return this.username;
  }

  getPassword() {
    return this.password;
  }

  getConfirmPassword() {
    return this.__confirmPassword;
  }

  getUsernameAsString() {
    return this.username();
  }

  getPasswordAsString() {
    return this.password().value();
  }

  getConfirmPasswordAsString() {
    return this.__confirmPassword().value();
  }

  getPathAsString() {
    return this.path();
  }

  getPath() {
    return this.path;
  }

  authConfig() {
    const configuration = new Configuration("PasswordFilePath",
                                            new PlainTextValue(this.getPathAsString()));
    return new AuthConfig("password-file",
                          "cd.go.authentication.passwordfile",
                          new Configurations([configuration]));
  }

  save() {
    if (this.isValid()) {
      return ApiRequestBuilder.POST("/go/api/admin/internal/security/auth_configs/setup_password_file",
                                    ApiVersion.v1,
                                    {payload: this.toJSON()}).then(SetupSecurity.extractObjectWithEtag);
    }
  }

  toJSON() {
    return {username: this.getUsernameAsString(), password: this.getPasswordAsString()};
  }

  private static extractObjectWithEtag(result: ApiResult<string>) {
    return result.map((body) => {
      const authConfigJSON = JSON.parse(body) as AuthConfigJSON;
      return {
        object: AuthConfig.fromJSON(authConfigJSON),
        etag: result.getEtag()
      } as ObjectWithEtag<AuthConfig>;
    });
  }
}

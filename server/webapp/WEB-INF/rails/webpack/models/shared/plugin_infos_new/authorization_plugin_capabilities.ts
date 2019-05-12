/*
 * Copyright 2018 ThoughtWorks, Inc.
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

export class AuthCapabilities {
  readonly canSearch: boolean;
  readonly canAuthorize: boolean;
  readonly supportedAuthType: string;
  readonly supportsAddUser: boolean;

  constructor(canSearch: boolean, canAuthorize: boolean, supportedAuthType: string, supportsAddUser: boolean) {
    this.canSearch         = canSearch;
    this.canAuthorize      = canAuthorize;
    this.supportedAuthType = supportedAuthType;
    this.supportsAddUser   = supportsAddUser;
  }

  static fromJSON(data: any) {
    return new AuthCapabilities(data.can_search, data.can_authorize, data.supported_auth_type, data.supports_add_user);
  }
}

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

import {ApiResult, ErrorResponse, ObjectWithEtag, SuccessResponse} from "helpers/api_request_builder";
import * as m from "mithril";
import {Stream} from "mithril/stream";
import * as stream from "mithril/stream";
import {AuthConfig} from "models/auth_configs/auth_configs";
import {AuthConfigsCRUD} from "models/auth_configs/auth_configs_crud";
import {SetupSecurity} from "models/setup_security/setup_security";
import {Page, PageState} from "views/pages/page";
import {AddOperation} from "views/pages/page_operations";
import {SetupSecurityWidget} from "views/pages/setup_security/setup_security_widget";

interface State extends AddOperation<SetupSecurity> {
  setupSecurity: Stream<SetupSecurity>;
  loadExistingFile: (e: MouseEvent) => void;
}

export class SetupSecurityPage extends Page<null, State> {
  oninit(vnode: m.Vnode<null, State>) {
    super.oninit(vnode);

    vnode.state.onAdd = (e: MouseEvent) => {
      e.preventDefault();
      vnode.state.setupSecurity().save();
    };

    vnode.state.loadExistingFile = (e: MouseEvent) => {
      e.preventDefault();
      this.pageState = PageState.LOADING;
      AuthConfigsCRUD.verifyConnection(vnode.state.setupSecurity().authConfig())
                     .then(this.onVerifyConnectionResult.bind(this, vnode));
    };
  }

  componentToDisplay(vnode: m.Vnode<null, State>): m.Children {
    return <SetupSecurityWidget {...vnode.state}/>;
  }

  pageName(): string {
    return "Setup security";
  }

  fetchData(vnode: m.Vnode<null, State>): Promise<any> {
    this.pageState            = PageState.OK;
    vnode.state.setupSecurity = stream(new SetupSecurity());
    return Promise.resolve();
  }

  protected headerPanel(vnode: m.Vnode<null, State>): any {
    return null;
  }

  private onVerifyConnectionResult(vnode: m.Vnode<null, State>, result: ApiResult<ObjectWithEtag<AuthConfig>>) {
    this.pageState = PageState.OK;
    result.do(this.onVerifyConnectionSuccess.bind(this, vnode),
              this.onVerifyConnectionError.bind(this, result.unwrap() as ErrorResponse, result.getStatusCode(), vnode));
  }

  private onVerifyConnectionSuccess(vnode: m.Vnode<null, State>) {
    AuthConfigsCRUD.create(vnode.state.setupSecurity().authConfig()).then(this.onSaveResult.bind(this));
  }

  private onVerifyConnectionError(errorResponse: ErrorResponse, statusCode: number, vnode: m.Vnode<null, State>) {
    vnode.state.setupSecurity().errors().add("path", JSON.parse(errorResponse.body!).message);
  }

  private onSaveResult(result: ApiResult<ObjectWithEtag<AuthConfig>>) {
    this.pageState = PageState.OK;
    result.do(this.onSuccess.bind(this), () => null);
  }

  private onSuccess(successResponse: SuccessResponse<ObjectWithEtag<AuthConfig>>) {
    window.location.href = "/";
  }
}

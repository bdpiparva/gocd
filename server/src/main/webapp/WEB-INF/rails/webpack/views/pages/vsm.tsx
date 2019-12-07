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
import {ErrorResponse, SuccessResponse} from "helpers/api_request_builder";
import {SparkRoutes} from "helpers/spark_routes";
import m from "mithril";
import Stream from "mithril/stream";
import {MaterialMeta, PageMeta, Vsm} from "models/vsm/vsm";
import {VSM} from "models/vsm/vsm_json";
import {VsmService} from "models/vsm/vsm_service";
import {Link} from "views/components/link";
import {Page} from "views/pages/page";
import {VsmWidget} from "views/pages/vsm/vsm_widget";
import {MessageType} from "../components/flash_message";
import {HeaderPanel} from "../components/header_panel";

interface State {
  vsm: Stream<Vsm>;
}

export class VsmPage extends Page<null, State> implements State {
  readonly vsm              = Stream<Vsm>();
  private readonly service  = new VsmService();
  private readonly pageMeta = PageMeta.fromJSON(this.getMeta());

  componentToDisplay(vnode: m.Vnode<null, State>): m.Children {
    return <VsmWidget vsm={this.vsm}/>;
  }

  pageName(): string {
    return "Value Stream Map";
  }

  headerPanel(vnode: m.Vnode<null, State>) {
    return <HeaderPanel title={this.pageName()} keyValuePair={this.asKeyValuePair()}/>;
  }

  fetchData(vnode: m.Vnode<null, State>): Promise<any> {
    return this.getVsmData().then((result) => result.do(this.onSuccess.bind(this), this.onError.bind(this)));
  }

  getVsmData() {
    const entity = this.pageMeta.entity;
    if (entity instanceof MaterialMeta) {
      return this.service.getMaterialVsm(entity.fingerprint, entity.revision);
    }
    return this.service.getPipelineVsm(entity.name, entity.counter);
  }

  onSuccess(response: SuccessResponse<string>) {
    this.vsm(Vsm.fromJSON(JSON.parse(response.body) as VSM.VsmJSON));
  }

  onError(response: ErrorResponse) {
    this.flashMessage.setMessage(MessageType.alert, response.message);
  }

  asKeyValuePair() {
    const entity = this.pageMeta.entity;
    if (entity instanceof MaterialMeta) {
      return {
        Material: entity.name,
        Revision: entity.revision
      };
    }

    return {
      Pipeline: <Link href={SparkRoutes.pipelineActivityLink(entity.name)}>{entity.name}</Link>,
      Instance: entity.counter
    };
  }
}

/*
 * Copyright 2020 ThoughtWorks, Inc.
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

import {MithrilViewComponent} from "jsx/mithril-component";
import _ from 'lodash';
import m from "mithril";
import {Tabs} from "../../components/tab";
import {PipelineConfig} from "../../../models/pipeline_configs/pipeline_config";

interface Attrs {
  pipelineConfig: PipelineConfig;
}

const tabs = [{
  name: 'General',
  renderer: () => {
    return <div>General tab</div>;
  }
}, {
  name: 'Environment Variables',
  renderer: () => {
    return <div>Environment Variables tab</div>;
  }
}];

export class PipelineConfigWidget extends MithrilViewComponent<Attrs> {
  view(vnode: m.Vnode<Attrs>) {
    return <Tabs initialSelection={this.selectedTabIndex()}
                 tabs={tabs.map((eachTab) => eachTab.name)}
                 contents={tabs.map((eachTab) => eachTab.renderer())}
                 callback={(index: number) => {
                   m.route.set(vnode.attrs.pipelineConfig.name() + "/" + _.snakeCase(tabs[index].name));
                 }}/>;
  }

  private selectedTabIndex() {
    return tabs.findIndex((eachTab) => {
      return _.snakeCase(eachTab.name) === m.route.param().tab_name;
    });
  }
}

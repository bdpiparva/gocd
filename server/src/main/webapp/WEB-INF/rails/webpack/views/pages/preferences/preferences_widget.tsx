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
import m from "mithril";
import Stream from "mithril/stream";
import {Preferences} from "models/preferences/preferences";
import {Form} from "views/components/forms/form";
import {TextField} from "views/components/forms/input_fields";

interface Attrs {
  dummy?: Preferences;
}

export class PreferencesWidget extends MithrilViewComponent<Attrs> {
  view(vnode: m.Vnode<Attrs>) {
    return <div>
      <h5>EMAIL SETTINGS</h5>
      <Form>
        <TextField required={true}
                   helpText="Enter your username here"
                   docLink="configuration/quick_pipeline_setup.html#step-2-material"
                   label="Username"
                   placeholder="username"
                   property={Stream()}/>,
      </Form>
    </div>;
  }
}

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

import m from "mithril";
import {Preferences} from "models/preferences/preferences";
import {Page} from "views/pages/page";
import {PreferencesWidget} from "views/pages/preferences/preferences_widget";

interface State {
  dummy?: Preferences;
}

export class PreferencesPage extends Page<null, State> {
  componentToDisplay(vnode: m.Vnode<null, State>): m.Children {
    return <PreferencesWidget/>;
  }

  pageName(): string {
    return "Preferences";
  }

  fetchData(vnode: m.Vnode<null, State>): Promise<any> {
    return Promise.resolve();
  }
}

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

import {bind} from "classnames/bind";
import {MithrilViewComponent} from "jsx/mithril-component";
import * as m from "mithril";
import {Stream} from "mithril/stream";
import * as stream from "mithril/stream";
import {SetupSecurity} from "models/setup_security/setup_security";
import {AuthorizationSettings} from "models/shared/plugin_infos_new/extensions";
import * as Buttons from "views/components/buttons/index";
import {Form} from "views/components/forms/form";
import {TextField} from "views/components/forms/input_fields";
import * as foundationStyles from "views/pages/new_plugins/foundation_hax.scss";
import {AddOperation} from "views/pages/page_operations";
import * as styles from "./index.scss";

const AngularPluginNew = require("views/shared/angular_plugin_new");

const classnames = bind(styles);

interface Attrs extends AddOperation<SetupSecurity> {
  setupSecurity: Stream<SetupSecurity>;
  passwordFileSettings: Stream<AuthorizationSettings>;
  loadExistingFile: (e: MouseEvent) => void;
}

export class SetupSecurityWidget extends MithrilViewComponent<Attrs> {
  view(vnode: m.Vnode<Attrs>) {
    if (!vnode.attrs.passwordFileSettings()) {
      return "Loading";
    }

    return (<div className={styles.setup_security}>
      <h1>Setup security</h1>

      <div className={styles.row}>
        <div className={styles.col2}>

          <div className={foundationStyles.foundationFormHax}>
            <div className="row collapse">
              <AngularPluginNew
                pluginInfoSettings={stream(vnode.attrs.passwordFileSettings().userAddSettings)}
                configuration={vnode.attrs.setupSecurity().getProperties()}/>
            </div>
          </div>

          <Buttons.Primary onclick={vnode.attrs.onAdd.bind(vnode)}>Create a new user</Buttons.Primary>
        </div>
        <div className={classnames(styles.col2, styles.help)}>
          <h3 className={styles.helpHeading}>Setup from scratch</h3>
          <p>
            A user will be created and stored in <strong>password.properties</strong> at <strong>/var/lib</strong>.
          </p>
          Additional help can go here
        </div>
      </div>

      <hr className={styles.separator}/>
      <br/>

      <div className={styles.row}>
        <div className={styles.col2}>
          <Form compactForm={true}>
            <TextField required={true}
                       helpText="Existing password file path"
                       label="Password file path"
                       placeholder="path"
                       errorText={vnode.attrs.setupSecurity().errors().errorsForDisplay("path")}
                       property={vnode.attrs.setupSecurity().getPath()}/>

            <Buttons.Primary onclick={vnode.attrs.loadExistingFile.bind(vnode)}>Load file</Buttons.Primary>
          </Form>
        </div>
        <div className={styles.col2}>
          Need help :(
        </div>
      </div>
    </div>);
  }
}

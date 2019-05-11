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
import {SetupSecurity} from "models/setup_security/setup_security";
import * as Buttons from "views/components/buttons/index";
import {Form} from "views/components/forms/form";
import {PasswordField, TextField} from "views/components/forms/input_fields";
import {AddOperation} from "views/pages/page_operations";
import * as styles from "./index.scss";

const classnames = bind(styles);

interface Attrs extends AddOperation<SetupSecurity> {
  setupSecurity: Stream<SetupSecurity>;
  loadExistingFile: (e: MouseEvent) => void;
}

export class SetupSecurityWidget extends MithrilViewComponent<Attrs> {
  view(vnode: m.Vnode<Attrs>) {
    return (<div className={styles.setup_security}>
      <h1>Setup security</h1>

      <div className={styles.row}>
        <div className={styles.col2}>
          <Form compactForm={true}>
            <TextField required={true}
                       helpText="Enter your username here"
                       label="Username"
                       placeholder="username"
                       errorText={vnode.attrs.setupSecurity().errors().errorsForDisplay("username")}
                       property={vnode.attrs.setupSecurity().getUsername()}/>

            <PasswordField required={true}
                           label="Password"
                           errorText={vnode.attrs.setupSecurity().errors().errorsForDisplay("password")}
                           hideReset={true}
                           property={vnode.attrs.setupSecurity().getPassword()}/>

            <PasswordField required={true}
                           label="Confirm password"
                           hideReset={true}
                           errorText={vnode.attrs.setupSecurity().errors().errorsForDisplay("confirm_password")}
                           property={vnode.attrs.setupSecurity().getConfirmPassword()}/>

            <Buttons.Primary onclick={vnode.attrs.onAdd.bind(vnode)}>Save</Buttons.Primary>
          </Form>
        </div>
        <div className={classnames(styles.col2, styles.help)}>
          <h3 className={styles.helpHeading}>Setup from scratch</h3>
          <p>
            User <strong className={styles.wrapWord}>{vnode.attrs.setupSecurity().getUsernameAsString()}</strong> will
            be created and stored in <strong>password.properties</strong> at <strong>/var/lib</strong>.
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

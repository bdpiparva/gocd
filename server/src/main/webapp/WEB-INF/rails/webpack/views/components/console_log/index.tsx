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
import m from "mithril";
import Stream from "mithril/stream";
import * as Icons from "../icons";
import styles from "./index.scss";
import {LineParser} from "./parser";

const classnames = bind(styles);

interface Attrs {
  lines: string[];
  fullscreen: Stream<boolean>;
}

export class ConsoleLog extends MithrilViewComponent<Attrs> {
  view(vnode: m.Vnode<Attrs>): m.Children {
    return <div data-test-id="console-log"
                class={classnames(styles.consoleLog, {[styles.fullscreen]: vnode.attrs.fullscreen()})}>
      <div data-test-id="header" class={styles.header}>
        <div class={styles.right}>
          <ButtonWithIcon icon={<Icons.QuestionMark iconOnly={true}/>}
                          onclick={ConsoleLog.toggleFullscreen(vnode)}>{vnode.attrs.fullscreen() ? "Exit" : "Fullscreen"}</ButtonWithIcon>
        </div>
      </div>
      <div data-test-id="body" class={styles.content}>
        {vnode.attrs.lines.map(new LineParser().parse)}
      </div>
      <div data-test-id="footer" class={styles.footer}>Footer</div>
    </div>;
  }

  private static toggleFullscreen(vnode: m.Vnode<Attrs>) {
    vnode.attrs.fullscreen(!vnode.attrs.fullscreen());
  }
}

interface BtnAttrs {
  icon: m.Child;
  onclick: () => void;
}

class ButtonWithIcon extends MithrilViewComponent<BtnAttrs> {
  view(vnode: m.Vnode<BtnAttrs, this>): m.Children {
    return <button class={styles.consoleLogButton} onclick={vnode.attrs.onclick}>
      {vnode.attrs.icon}
      {vnode.children}
    </button>;
  }
}

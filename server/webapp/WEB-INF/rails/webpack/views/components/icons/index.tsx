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

import {HTMLAttributes} from "jsx/dom";
import * as _ from "lodash";
import * as m from "mithril";

import {MithrilViewComponent} from "jsx/mithril-component";

import {bind} from "classnames/bind";
import * as styles from "./index.scss";

const classnames = bind(styles);

export interface Attrs extends HTMLAttributes {
  onclick?: (e: MouseEvent) => void;
  disabled?: boolean;
  iconOnly?: boolean;
}

class Icon extends MithrilViewComponent<Attrs> {
  private readonly name: string;
  private readonly title: string;

  protected constructor(name: string, title: string) {
    super();
    this.name  = name;
    this.title = title;
  }

  view(vnode: m.Vnode<Attrs>) {
    if (vnode.attrs.iconOnly) {
      return (
        <i title={this.title}
           data-test-id={`${this.title}-icon`}
           data-test-disabled-element={vnode.attrs.disabled}
           className={classnames(this.name, {disabled: vnode.attrs.disabled})}
           {...vnode.attrs}/>
      );
    }

    return (
      <button title={this.title}
              data-test-disabled-element={vnode.attrs.disabled}
              className={(classnames(styles.btnIcon, {disabled: vnode.attrs.disabled}))}
              {...vnode.attrs}>
        <i className={classnames(this.name)}/>
      </button>
    );
  }
}

export class Settings extends Icon {
  constructor() {
    super(styles.settings, "Settings");
  }
}

export class Refresh extends Icon {
  constructor() {
    super(styles.refresh, "Refresh");
  }
}

export class Analytics extends Icon {
  constructor() {
    super(styles.analytics, "Analytics");
  }
}

export class Usage extends Icon {
  constructor() {
    super(styles.usage, "Usage");
  }
}

export class Edit extends Icon {
  constructor() {
    super(styles.edit, "Edit");
  }
}

export class Add extends Icon {
  constructor() {
    super(styles.add, "Add");
  }
}

export class Clone extends Icon {
  constructor() {
    super(styles.clone, "Clone");
  }
}

export class Delete extends Icon {
  constructor() {
    super(styles.remove, "Delete");
  }
}

export class Lock extends Icon {
  constructor() {
    super(styles.lock, "Lock");
  }
}

export class Close extends Icon {
  constructor() {
    super(styles.close, "Close");
  }
}

export class QuestionMark extends Icon {
  constructor() {
    super(styles.question, "Help");
  }
}

export class Spinner extends Icon {
  constructor() {
    super(styles.spinner, "Spinner");
  }
}

export class Check extends Icon {
  constructor() {
    super(styles.check, "Check");
  }
}

export class Minus extends Icon {
  constructor() {
    super(styles.minus, "Minus");
  }
}

export class InfoCircle extends Icon {
  constructor() {
    super(styles.infoCircle, "Info Circle");
  }
}

export class QuestionCircle extends Icon {
  constructor() {
    super(styles.questionCircle, "Question Circle");
  }
}

export class IconGroup extends MithrilViewComponent<Attrs> {
  view(vnode: m.Vnode<Attrs>) {
    return (
      <div className={styles.iconGroup} aria-label="actions">
        {vnode.children}
      </div>
    );
  }
}

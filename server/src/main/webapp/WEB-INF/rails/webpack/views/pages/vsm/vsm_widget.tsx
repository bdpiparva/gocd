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

import {MithrilViewComponent} from "jsx/mithril-component";
import m from "mithril";
import Stream from "mithril/stream";
import {Level, Node, Vsm} from "models/vsm/vsm";
import {NodeWidget} from "./node_widget";

interface Attrs {
  vsm: Stream<Vsm>;
}

export class VsmWidget extends MithrilViewComponent<Attrs> {
  view(vnode: m.Vnode<Attrs>) {
    if (!vnode.attrs.vsm()) {
      return <div>Handle this</div>;
    }

    return vnode.attrs.vsm().levels.map((level: Level, index: number) => {
      return <LevelWidget nodes={level.nodes}/>;
    });
  }
}

interface LevelAttrs {
  nodes: Node[];
}

export class LevelWidget extends MithrilViewComponent<Level> {
  view(vnode: m.Vnode<LevelAttrs>) {
    if (vnode.attrs.nodes.length === 0) {
      return <div>No levels: Handle this</div>;
    }

    return vnode.attrs.nodes.map((node: Node, index: number) => {
      return <NodeWidget node={node}/>;
    });
  }
}

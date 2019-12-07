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

import uuid4 from "uuid/v4";
import {VSM} from "../vsm_json";

class BaseTestData {
  protected static randomRevision() {
    return this.randomId();
  }

  protected static randomIntInRange(min: number, max: number) {
    return Math.ceil(Math.random() * (max - min) + min);
  }

  protected static randomId() {
    return uuid4();
  }
}

export class VsmTestData extends BaseTestData {
  static modification(id = this.randomId(), revision = this.randomRevision()) {
    return {
      modified_time: `about ${this.randomIntInRange(1, 24)} hours ago`,
      locator: `/go/materials/value_stream_map/${this.randomId()}/${revision}`,
      revision,
      comment: "Check if user is authorized ti view pipeline activities",
      user: "Bob Ford <bford@go.cd>"
    } as VSM.ModificationJSON;
  }

  static materialRevision(...modifications: VSM.ModificationJSON[]) {
    return {modifications} as VSM.MaterialRevisionJSON;
  }

  static node(id = this.randomId(), ...materialRevisions: VSM.MaterialRevisionJSON[]) {
    return {
      id,
      locator: "/foo/bar",
      depth: 1,
      name: "https://some.material/org/repo",
      node_type: "GIT",
      material_revisions: materialRevisions,
      instances: [],
      parents: [],
      dependents: ["build-linux"]
    } as VSM.NodeJSON;
  }

  static vsm(isPipelineVSM = true, current = "build-linux", ...nodes: VSM.NodeJSON[]) {
    if (isPipelineVSM) {
      return {
        current_pipeline: current,
        levels: [{nodes}]
      } as VSM.VsmJSON;
    }

    return {
      current_material: current,
      levels: [{nodes}]
    } as VSM.VsmJSON;
  }
}

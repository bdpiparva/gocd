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
export namespace VSM {
  export interface MaterialMetaJSON {
    name: string;
    fingerprint: string;
    revision: string;
  }

  export interface PipelineMetaJSON {
    name: string;
    counter: number;
  }

  export interface PageMetaJSON {
    type: "material" | "pipeline";
    entity: MaterialMetaJSON | PipelineMetaJSON;
  }

  export interface VsmJSON {
    current_material: string;
    current_pipeline: string;
    levels: LevelJSON[];
  }

  export interface LevelJSON {
    nodes: NodeJSON[];
  }

  export interface NodeJSON {
    id: string;
    locator: string;
    name: string;
    depth: number;
    instances: InstanceJSON[];
    node_type: string;
    parents: [];
    material_revisions: MaterialRevisionJSON[];
    dependents: string[];
  }

  export interface InstanceJSON {
    label: string;
    locator: string;
    counter: number;
    stages: StageJSON[];
  }

  export interface StageJSON {
    locator: string;
    name: string;
    duration: number;
    status: string;
  }

  export interface MaterialRevisionJSON {
    modifications: ModificationJSON[];
  }

  export interface ModificationJSON {
    modified_time: string;
    locator: string;
    revision: string;
    comment: string;
    user: string;
  }

}

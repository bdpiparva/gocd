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

import {VSM} from "./vsm_json";

export class MaterialMeta {
  readonly fingerprint: string;
  readonly revision: string;

  constructor(fingerprint: string, revision: string) {
    this.fingerprint = fingerprint;
    this.revision    = revision;
  }

  static fromJSON(json: VSM.MaterialMetaJSON) {
    return new MaterialMeta(json.material_fingerprint,
      json.material_revision);
  }
}

export class PipelineMeta {
  name: string;
  counter: number;

  constructor(name: string, counter: number) {
    this.name    = name;
    this.counter = counter;
  }

  static fromJSON(json: VSM.PipelineMetaJSON) {
    return new PipelineMeta(json.pipeline_name, json.pipeline_counter);
  }
}

export class PageMeta {
  readonly entity: MaterialMeta | PipelineMeta;

  constructor(entity: MaterialMeta | PipelineMeta) {
    this.entity = entity;
  }

  static fromJSON(json: VSM.PageMetaJSON) {
    switch (json.type) {
      case "material":
        return new PageMeta(MaterialMeta.fromJSON(json.entity as VSM.MaterialMetaJSON));
      case "pipeline":
        return new PageMeta(PipelineMeta.fromJSON(json.entity as VSM.PipelineMetaJSON));
      default:
        throw Error(`Entity type ${json.type} is unknown.`);
    }
  }
}

export class Current {
  readonly name: string;
  readonly isPipeline: boolean;

  constructor(name: string, isPipeline: boolean) {
    this.name       = name;
    this.isPipeline = isPipeline;
  }

  static from(pipeline: string, material: string) {
    const name       = pipeline || material;
    const isPipeline = name === pipeline;
    return new Current(name, isPipeline);
  }
}

export class Vsm {
  readonly current: Current;
  readonly levels: Level[];

  constructor(current: Current, levels: Level[]) {
    this.current = current;
    this.levels  = levels;
  }

  static fromJSON(json: VSM.VsmJSON) {
    return new Vsm(Current.from(json.current_pipeline, json.current_material), json.levels.map(Level.fromJSON));
  }
}

export class Level {
  nodes: Node[];

  constructor(...nodes: Node[]) {
    this.nodes = nodes;
  }

  static fromJSON(json: VSM.LevelJSON) {
    return new Level(...json.nodes.map(Node.fromJSON));
  }
}

export class Node {
  id: string;
  name: string;
  nodeType: string;
  locator: string;
  depth: number;
  parents: [];
  dependents: string[];
  instances?: Instance[];
  materialRevisions?: MaterialRevision[];

  constructor(id: string,
              name: string,
              nodeType: string,
              locator: string,
              depth: number,
              parents: [],
              dependents: string[],
              instances?: Instance[],
              materialRevisions?: MaterialRevision[]) {
    this.id                = id;
    this.name              = name;
    this.nodeType          = nodeType;
    this.locator           = locator;
    this.depth             = depth;
    this.parents           = parents;
    this.dependents        = dependents;
    this.instances         = instances;
    this.materialRevisions = materialRevisions;
  }

  static fromJSON(json: VSM.NodeJSON) {
    return new Node(json.id, json.name, json.node_type, json.locator, json.depth,
      json.parents,
      json.dependents,
      Instance.fromJSON(json.instances),
      MaterialRevision.fromJSON(json.material_revisions));
  }
}

export class Instance {
  label: string;
  locator: string;
  counter: number;
  stages: Stage[];

  constructor(label: string, counter: number, locator: string, stages: Stage[]) {
    this.label   = label;
    this.locator = locator;
    this.counter = counter;
    this.stages  = stages;
  }

  static fromJSON(instances: VSM.InstanceJSON[]) {
    if (!instances) {
      return;
    }
    return instances.map((instance) => new Instance(instance.label, instance.counter, instance.locator,
      Stage.fromJSON(instance.stages)));
  }
}

export class Stage {
  name: string;
  status: string;
  duration: number;
  locator: string;

  constructor(name: string, status: string, duration: number, locator: string) {
    this.name     = name;
    this.status   = status;
    this.duration = duration;
    this.locator  = locator;
  }

  static fromJSON(stages: VSM.StageJSON[]) {
    return stages.map((stage) => new Stage(stage.name, stage.status, stage.duration, stage.locator));
  }
}

export class MaterialRevision {
  modifications: Modification[];

  constructor(modifications: Modification[]) {
    this.modifications = modifications;
  }

  static fromJSON(array: VSM.MaterialRevisionJSON[]) {
    if (!array) {
      return;
    }
    return array.map((materialRev) => new MaterialRevision(Modification.fromJSON(materialRev.modifications)));
  }
}

export class Modification {
  user: string;
  revision: string;
  modifiedTime: string;
  comment: string;
  locator: string;

  constructor(user: string, revision: string, modifiedTime: string, comment: string, locator: string) {
    this.user         = user;
    this.revision     = revision;
    this.modifiedTime = modifiedTime;
    this.comment      = comment;
    this.locator      = locator;
  }

  static fromJSON(modifications: VSM.ModificationJSON[]) {
    return modifications.map((mod) => new Modification(mod.user,
      mod.revision,
      mod.modified_time,
      mod.comment,
      mod.locator));
  }
}

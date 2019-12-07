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

import {Instance, MaterialRevision, Modification, Node, Vsm} from "../vsm";
import {VsmTestData} from "./vsm_test_data";

describe("VSM", () => {
  describe("Modifications", () => {
    it("should parse modification json", () => {
      const modificationJSON = VsmTestData.modification();
      const modifications    = Modification.fromJSON([modificationJSON]);

      expect(modifications).toHaveLength(1);
      expect(modifications[0].comment).toEqual(modificationJSON.comment);
      expect(modifications[0].revision).toEqual(modificationJSON.revision);
      expect(modifications[0].user).toEqual(modificationJSON.user);
      expect(modifications[0].locator).toEqual(modificationJSON.locator);
      expect(modifications[0].modifiedTime).toEqual(modificationJSON.modified_time);
    });
  });

  describe("MaterialRevisions", () => {
    it("should parse material revision json", () => {
      const modificationJSON     = VsmTestData.modification();
      const materialRevisionJSON = VsmTestData.materialRevision(modificationJSON);
      const materialRevisions    = MaterialRevision.fromJSON([materialRevisionJSON]);

      expect(materialRevisions).toHaveLength(1);
      expect(materialRevisions![0].modifications).toEqual(Modification.fromJSON([modificationJSON]));
    });
  });

  describe("Node", () => {
    it("should parse node json", () => {
      const modificationJSON     = VsmTestData.modification("1");
      const materialRevisionJSON = VsmTestData.materialRevision(modificationJSON);
      const nodeJSON             = VsmTestData.node("1", materialRevisionJSON);
      const node                 = Node.fromJSON(nodeJSON);

      expect(node.id).toEqual("1");
      expect(node.name).toEqual(nodeJSON.name);
      expect(node.dependents).toEqual(nodeJSON.dependents);
      expect(node.depth).toEqual(nodeJSON.depth);
      expect(node.locator).toEqual(nodeJSON.locator);
      expect(node.nodeType).toEqual(nodeJSON.node_type);
      expect(node.parents).toEqual(nodeJSON.parents);
      expect(node.instances).toEqual(Instance.fromJSON(nodeJSON.instances));
      expect(node.materialRevisions).toEqual(MaterialRevision.fromJSON([materialRevisionJSON]));
    });
  });

  describe("VSM", () => {
    it("should parse pipeline vsm json", () => {
      const modificationJSON     = VsmTestData.modification("1");
      const materialRevisionJSON = VsmTestData.materialRevision(modificationJSON);
      const nodeJSON             = VsmTestData.node("1", materialRevisionJSON);
      const vsmJSON              = VsmTestData.vsm(true, "Installers", nodeJSON);
      const vsm                  = Vsm.fromJSON(vsmJSON);

      expect(vsm.current.isPipeline).toEqual(true);
      expect(vsm.current.name).toEqual("Installers");
      expect(vsm.levels).toHaveLength(1);
      expect(vsm.levels[0].nodes).toHaveLength(1);
      expect(vsm.levels[0].nodes[0]).toEqual(Node.fromJSON(nodeJSON));
    });

    it("should parse material vsm json", () => {
      const modificationJSON     = VsmTestData.modification("1");
      const materialRevisionJSON = VsmTestData.materialRevision(modificationJSON);
      const nodeJSON             = VsmTestData.node("1", materialRevisionJSON);
      const vsmJSON              = VsmTestData.vsm(false, "https://some.material/org/repo", nodeJSON);
      const vsm                  = Vsm.fromJSON(vsmJSON);

      expect(vsm.current.isPipeline).toEqual(false);
      expect(vsm.current.name).toEqual("https://some.material/org/repo");
      expect(vsm.levels).toHaveLength(1);
      expect(vsm.levels[0].nodes).toHaveLength(1);
      expect(vsm.levels[0].nodes[0]).toEqual(Node.fromJSON(nodeJSON));
    });
  });
});

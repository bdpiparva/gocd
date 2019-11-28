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
import m from "mithril";
import {MithrilViewComponent} from "jsx/mithril-component";
import * as Icons from "views/components/icons";
import {Group, StageConfig} from "models/pipeline_activity/pipeline_activity";
import styles from "./index.scss";
import Stream from "mithril/stream";

const classnames = bind(styles);

interface ShowForceBuildActionAttrs {
  group: Group
  canForce: Stream<boolean>;
  pipelineName: string;
  runPipeline: (name: string) => void;
}

export class ShowForceBuildActionWidget extends MithrilViewComponent<ShowForceBuildActionAttrs> {
  view(vnode: m.Vnode<ShowForceBuildActionAttrs, this>): m.Children {
    return <div class={classnames(styles.pipelineRun, styles.forceBuild)}>
      <div class={styles.runInfoSection}>
        <Icons.Trigger onclick={() => vnode.attrs.runPipeline(vnode.attrs.pipelineName)}
                       disabled={!vnode.attrs.canForce()}/>
      </div>
      <div class={styles.stagesSection}>
        {vnode.attrs.group.config().stages().map((stage, index) => {
          return <div class={classnames(styles.stage, styles.disabledIcon)}>
            {ShowForceBuildActionWidget.getStageApprovalIcon(index, stage)}
            <span class={styles.unknown}/>
          </div>;
        })}
      </div>
    </div>;
  }

  private static getStageApprovalIcon(index: number, stage: StageConfig): m.Children {
    if (index === 0) {
      return;
    }

    if (stage.isAutoApproved()) {
      return <Icons.Forward iconOnly={true} disabled={true}/>;
    }

    return <Icons.StepForward iconOnly={true} disabled={true}/>;
  }
}

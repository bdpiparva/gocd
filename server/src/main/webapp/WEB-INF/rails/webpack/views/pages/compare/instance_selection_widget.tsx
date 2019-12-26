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
import _ from "lodash";
import m from "mithril";
import Stream from "mithril/stream";
import {PipelineInstance, PipelineInstances, Stages} from "models/compare/pipeline_instance";
import {PipelineInstanceCRUD} from "models/compare/pipeline_instance_crud";
import s from "underscore.string";
import {Dropdown, DropdownAttrs} from "views/components/buttons";
import {TextField} from "views/components/forms/input_fields";
import {Link} from "views/components/link";
import {Spinner} from "views/components/spinner";
import spinnerCss from "views/pages/agents/spinner.scss";
import styles from "./index.scss";
import {TimelineModal} from "./timeline_modal";
import {PipelineInstanceWidget} from "./pipeline_instance_widget";

const classnames = bind(styles);

type StringOrNumber = string | number;

interface InstanceAttrs {
  instance: PipelineInstance;
  onInstanceChange: (counter: number) => void;
}

export class InstanceSelectionWidget extends MithrilViewComponent<InstanceAttrs> {

  static dataTestId(...parts: StringOrNumber[]) {
    return s.slugify(parts.join("-").trim().toLowerCase());
  }

  static stageStatusClass(status: string) {
    if (!status) {
      return;
    }
    switch (status.trim().toLowerCase()) {
      case "building":
        return styles.building;
      case "failed":
        return styles.failed;
      case "failing":
        return styles.failing;
      case "cancelled":
        return styles.cancelled;
      case "passed":
        return styles.passed;
      case "waiting":
        return styles.waiting;
      default:
        return styles.unknown;
    }
  }

  view(vnode: m.Vnode<InstanceAttrs, this>): m.Children | void | null {
    const rows = this.getStages(vnode.attrs.instance.stages);
    return <div
      data-test-id={InstanceSelectionWidget.dataTestId("instance", "selection", "widget", vnode.attrs.instance.counter())}
      class={styles.instanceWrapper}>
      <SelectInstanceWidget show={Stream(false)} {...vnode.attrs}/>
      <table data-test-id="stages">
        {rows}
      </table>
    </div>;
  }

  private getStages(stages: Stream<Stages>) {
    const cells: m.Children = [];
    const rows              = stages().map((stage, index) => {
      cells.push(<td>
            <span data-test-id={InstanceSelectionWidget.dataTestId("stage-status", stage.name())}
                  className={classnames(styles.stage, InstanceSelectionWidget.stageStatusClass(stage.status()))}/>
      </td>);
      if (index !== 0 && (index + 1) % 5 === 0) {
        const temp   = _.clone(cells);
        cells.length = 0;
        return <tr>{temp}</tr>;
      }
    });
    if (cells.length > 0) {
      rows.push(<tr>{cells}</tr>);
    }
    return rows;
  }
}

class SelectInstanceWidget extends Dropdown<InstanceAttrs> {
  private pattern: Stream<string>                      = Stream();
  private operationInProgress: Stream<boolean>         = Stream();
  private show: Stream<boolean>                        = Stream();
  private matchingInstances: Stream<PipelineInstances> = Stream();

  oncreate(vnode: m.VnodeDOM<InstanceAttrs, {}>): any {
    this.pattern(vnode.attrs.instance.counter() + "");
  }

  protected doRenderButton(vnode: m.Vnode<DropdownAttrs & InstanceAttrs>): m.Children {
    const placeholder = "Search for a pipeline instance by label, committer, date, etc.";
    const helpText    = <span>{placeholder} <br/> or <br/> <Link onclick={this.browse.bind(this, vnode)}>Browse the timeline</Link></span>;
    return <TextField
      placeholder={placeholder}
      helpText={helpText}
      property={this.pattern}
      onchange={() => this.onPatternChange(vnode)}/>;
  }

  protected doRenderDropdownContent(vnode: m.Vnode<DropdownAttrs & InstanceAttrs>): m.Children {
    if (this.show() === undefined || this.show() === false) {
      return;
    }
    if (this.operationInProgress()) {
      return <div class={styles.dropdownContent}>
        <Spinner small={true} css={spinnerCss}/>
      </div>;
    }
    return <div class={styles.dropdownContent}>
      <ul class={styles.instancesList}>
        {this.matchingInstances().map((instance) => {
          return <li class={styles.listItem}>
            <div>
              <h3>{instance.label()}</h3>
              <div>
                {/*<table>*/}
                {/*<tr>*/}
                {/*{vnode.attrs.instance.stages().map((stage) => {*/}
                {/*return <td><span className={TimelineModal.stageStatusClass(stage.status())}/></td>;*/}
                {/*})}*/}
                {/*</tr>*/}
                {/*</table>*/}
                <div data-test-id="triggered-by">
                  Triggered
                  by {instance.buildCause().getApprover()} on {PipelineInstanceWidget.getTimeToDisplay(instance.stages().getScheduledDate())}
                </div>
              </div>
            </div>
          </li>;
        })}
      </ul>
    </div>;
  }

  private onPatternChange(vnode: m.Vnode<DropdownAttrs & InstanceAttrs>): any {
    if (_.isEmpty(this.pattern())) {
      this.show(false);
      return;
    }
    this.show(true);
    this.operationInProgress(true);

    PipelineInstanceCRUD.matchingInstances(vnode.attrs.instance.name(), this.pattern())
                        .then((result) => {
                          result.do((successResponse) => {
                            this.matchingInstances(successResponse.body);
                          }, (errorResponse) => {
                            console.log(errorResponse);
                          });
                        })
                        .finally(() => this.operationInProgress(false));
  }

  private browse(vnode: m.Vnode<DropdownAttrs & InstanceAttrs>, e: MouseEvent) {
    e.stopPropagation();
    new TimelineModal(vnode.attrs.instance.name(), vnode.attrs.onInstanceChange).render();
  }

}

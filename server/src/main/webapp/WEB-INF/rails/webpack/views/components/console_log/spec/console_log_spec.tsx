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

import m from "mithril";
import {TestHelper} from "../../../pages/spec/test_helper";
import {ConsoleLog} from "../index";
import styles from "../index.scss";

describe("ConsoleLog", () => {
  const helper = new TestHelper();
  afterEach(helper.unmount.bind(helper));

  describe("Header", () => {
    it("should render the header", () => {
      mount();

      expect(helper.byTestId("console-log")).toBeInDOM();
      expect(helper.byTestId("header")).toBeInDOM();
    });

    it("should have fullscreen button", () => {
      mount();

      const header = helper.byTestId("header");
      expect(header).toBeInDOM();
      expect(helper.byTestId("fullscreen", header)).toBeInDOM();
    });

    it("should add class fullscreen to main container when fullscreen button is clicked", () => {
      mount();
      const header = helper.byTestId("header");
      expect(header).toBeInDOM();

      helper.clickByTestId("fullscreen");

      expect(helper.byTestId("console-log")).toHaveClass(styles.fullscreen);
    });
  });

  function mount() {
    helper.mount(() => <ConsoleLog lines={["Something"]}/>);
  }
});

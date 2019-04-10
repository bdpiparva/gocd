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

package com.thoughtworks.go.api.agentservices.representers;

import com.thoughtworks.go.api.agentservices.Handler;
import com.thoughtworks.go.api.base.OutputWriter;

import java.util.Map;

public class AgentManifestRepresenter {
    public static void toJSON(OutputWriter outputWriter, Map<String, Handler> handlers) {
        handlers.forEach((s, handler) -> {
            outputWriter.addChild(s, child -> child.add("sha256", handler.getChecksum()));
        });
    }
}

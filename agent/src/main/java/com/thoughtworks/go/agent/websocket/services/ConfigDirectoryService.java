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

package com.thoughtworks.go.agent.websocket.services;

import com.thoughtworks.go.util.SystemEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ConfigDirectoryService {
    private final File ROOT_DIR;

    @Autowired
    public ConfigDirectoryService(SystemEnvironment systemEnvironment) {
        ROOT_DIR = new File(systemEnvironment.getConfigDir()).getParentFile();
    }

    public File webSocketConfigDir() {
        return new File(ROOT_DIR, "websocket-config");
    }
}

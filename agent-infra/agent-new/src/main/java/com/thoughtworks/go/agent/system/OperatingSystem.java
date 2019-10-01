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
package com.thoughtworks.go.agent.system;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class OperatingSystem {
    @Getter(lazy = true)
    private static final String completeName = detectCompleteName();
    private static final String OS_FAMILY_NAME = System.getProperty("os.name");
    private static final String WINDOWS = "Windows";

    private static String readFromOsRelease() throws Exception {
        try (FileReader fileReader = new FileReader(new File("/etc/os-release"))) {
            Properties properties = new Properties();
            properties.load(fileReader);
            return unQuote(properties.getProperty("PRETTY_NAME"));
        }
    }

    private static String cleanUpPythonOutput(String str) {
        String output = str.replaceAll("[()',]+", "");
        if (StringUtils.isBlank(output)) {
            throw new RuntimeException("The linux distribution string is empty");
        }
        return output;
    }

    public static String getFamilyName() {
        if (OS_FAMILY_NAME.startsWith(WINDOWS)) {
            return WINDOWS;
        }
        return OS_FAMILY_NAME;
    }

    public static boolean isFamily(String familyName) {
        return getFamilyName().equals(familyName);
    }

    private static String unQuote(String string) {
        return string == null ? null : string.replaceAll("^\"|\"$", "");
    }

    private static String detectCompleteName() {
        String[] command = {"python", "-c", "import platform;print(platform.linux_distribution())"};
        try {
            return cleanUpPythonOutput(new ProcessExecutor()
                    .command(command)
                    .readOutput(true)
                    .execute().outputUTF8());
        } catch (Exception e) {
            try {
                return readFromOsRelease();
            } catch (Exception ignored) {
                return OS_FAMILY_NAME;
            }
        }
    }
}

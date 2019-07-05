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
package com.thoughtworks.go.server.service.support;

import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

@Component
public class RuntimeInformationProvider implements ServerInfoProvider {
    @Override
    public double priority() {
        return 5.0;
    }

    private Map<String, Object> asIndentedMultilineValuesAsJson(Map<String, String> inputArguments) {
        LinkedHashMap<String, Object> json = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : inputArguments.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        return json;
    }

    @Override
    public Map<String, Object> asJson() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeMXBean.getUptime();
        long uptimeInSeconds = uptime / 1000;
        long numberOfHours = uptimeInSeconds / (60 * 60);
        long numberOfMinutes = (uptimeInSeconds / 60) - (numberOfHours * 60);
        long numberOfSeconds = uptimeInSeconds % 60;

        LinkedHashMap<String, Object> json = new LinkedHashMap<>();
        json.put("Name", runtimeMXBean.getName());
        json.put("Uptime", runtimeMXBean.getUptime());
        json.put("Uptime (in Time Format)", "[About " + numberOfHours + " hours, " + numberOfMinutes + " minutes, " + numberOfSeconds + " seconds]");
        json.put("Spec Name", runtimeMXBean.getSpecName());
        json.put("Spec Vendor", runtimeMXBean.getSpecVendor());
        json.put("Spec Version", runtimeMXBean.getSpecVersion());

        json.put("Input Arguments", runtimeMXBean.getInputArguments());
        json.put("System Properties", new TreeMap<>(asIndentedMultilineValuesAsJson(runtimeMXBean.getSystemProperties())));
        json.put("Environment Variables", new TreeMap<>(asIndentedMultilineValuesAsJson(System.getenv())));

        return json;
    }

    @Override
    public String name() {
        return "Runtime Information";
    }

    @Override
    public void write(ServerInfoWriter serverInfoWriter) {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeMXBean.getUptime();
        long uptimeInSeconds = uptime / 1000;
        long numberOfHours = uptimeInSeconds / (60 * 60);
        long numberOfMinutes = (uptimeInSeconds / 60) - (numberOfHours * 60);
        long numberOfSeconds = uptimeInSeconds % 60;

        serverInfoWriter.add("Name", runtimeMXBean.getName());
        serverInfoWriter.add("Uptime", runtimeMXBean.getUptime());
        serverInfoWriter.add("Uptime (in Time Format)", "[About " + numberOfHours + " hours, " + numberOfMinutes + " minutes, " + numberOfSeconds + " seconds]");
        serverInfoWriter.add("Spec Name", runtimeMXBean.getSpecName());
        serverInfoWriter.add("Spec Vendor", runtimeMXBean.getSpecVendor());
        serverInfoWriter.add("Spec Version", runtimeMXBean.getSpecVersion());

        serverInfoWriter.addChildList("Input Arguments", runtimeMXBean.getInputArguments());
        serverInfoWriter.addChild("System Properties", addMapProperties(runtimeMXBean.getSystemProperties()));
        serverInfoWriter.addChild("Environment Variables", addMapProperties(System.getenv()));
    }

    private Consumer<ServerInfoWriter> addMapProperties(Map<String, String> map) {
        return writer -> map.forEach((key, value) -> writer.add(key, value.toString()));
    }
}

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

package com.thoughtworks.go.websocket;

public class AgentRuntimeInfoMessage {
    private String guid;
    private String hostname;
    private String location;
    private String usablespace;
    private String operatingSystem;
    private String agentAutoRegisterKey;
    private String agentAutoRegisterResources;
    private String agentAutoRegisterEnvironments;
    private String agentAutoRegisterHostname;
    private String elasticAgentId;
    private String elasticPluginId;

    public AgentRuntimeInfoMessage() {
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUsablespace() {
        return usablespace;
    }

    public void setUsablespace(String usablespace) {
        this.usablespace = usablespace;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getAgentAutoRegisterKey() {
        return agentAutoRegisterKey;
    }

    public void setAgentAutoRegisterKey(String agentAutoRegisterKey) {
        this.agentAutoRegisterKey = agentAutoRegisterKey;
    }

    public String getAgentAutoRegisterResources() {
        return agentAutoRegisterResources;
    }

    public void setAgentAutoRegisterResources(String agentAutoRegisterResources) {
        this.agentAutoRegisterResources = agentAutoRegisterResources;
    }

    public String getAgentAutoRegisterEnvironments() {
        return agentAutoRegisterEnvironments;
    }

    public void setAgentAutoRegisterEnvironments(String agentAutoRegisterEnvironments) {
        this.agentAutoRegisterEnvironments = agentAutoRegisterEnvironments;
    }

    public String getAgentAutoRegisterHostname() {
        return agentAutoRegisterHostname;
    }

    public void setAgentAutoRegisterHostname(String agentAutoRegisterHostname) {
        this.agentAutoRegisterHostname = agentAutoRegisterHostname;
    }

    public String getElasticAgentId() {
        return elasticAgentId;
    }

    public void setElasticAgentId(String elasticAgentId) {
        this.elasticAgentId = elasticAgentId;
    }

    public String getElasticPluginId() {
        return elasticPluginId;
    }

    public void setElasticPluginId(String elasticPluginId) {
        this.elasticPluginId = elasticPluginId;
    }

    public AgentRuntimeInfoMessage guid(String guid) {
        this.guid = guid;
        return this;
    }

    public AgentRuntimeInfoMessage hostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public AgentRuntimeInfoMessage location(String location) {
        this.location = location;
        return this;
    }

    public AgentRuntimeInfoMessage usablespace(String usablespace) {
        this.usablespace = usablespace;
        return this;
    }

    public AgentRuntimeInfoMessage operatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    public AgentRuntimeInfoMessage agentAutoRegisterKey(String agentAutoRegisterKey) {
        this.agentAutoRegisterKey = agentAutoRegisterKey;
        return this;
    }

    public AgentRuntimeInfoMessage agentAutoRegisterResources(String agentAutoRegisterResources) {
        this.agentAutoRegisterResources = agentAutoRegisterResources;
        return this;
    }

    public AgentRuntimeInfoMessage agentAutoRegisterEnvironments(String agentAutoRegisterEnvironments) {
        this.agentAutoRegisterEnvironments = agentAutoRegisterEnvironments;
        return this;
    }

    public AgentRuntimeInfoMessage agentAutoRegisterHostname(String agentAutoRegisterHostname) {
        this.agentAutoRegisterHostname = agentAutoRegisterHostname;
        return this;
    }

    public AgentRuntimeInfoMessage elasticAgentId(String elasticAgentId) {
        this.elasticAgentId = elasticAgentId;
        return this;
    }

    public AgentRuntimeInfoMessage elasticPluginId(String elasticPluginId) {
        this.elasticPluginId = elasticPluginId;
        return this;
    }
}

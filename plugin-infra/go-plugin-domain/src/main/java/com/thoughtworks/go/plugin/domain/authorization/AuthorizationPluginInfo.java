/*
 * Copyright 2017 ThoughtWorks, Inc.
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

package com.thoughtworks.go.plugin.domain.authorization;

import com.thoughtworks.go.plugin.api.info.PluginDescriptor;
import com.thoughtworks.go.plugin.domain.common.Image;
import com.thoughtworks.go.plugin.domain.common.PluggableInstanceSettings;
import com.thoughtworks.go.plugin.domain.common.PluginConstants;
import com.thoughtworks.go.plugin.domain.common.PluginInfo;

import java.util.Objects;

public class AuthorizationPluginInfo extends PluginInfo {
    private final PluggableInstanceSettings authConfigSettings;
    private final PluggableInstanceSettings roleSettings;
    private final PluggableInstanceSettings userAddSettings;
    private final Capabilities capabilities;

    public AuthorizationPluginInfo(PluginDescriptor descriptor, PluggableInstanceSettings authConfigSettings,
                                   PluggableInstanceSettings roleSettings, PluggableInstanceSettings userAddSettings, Image image, Capabilities capabilities) {
        super(descriptor, PluginConstants.AUTHORIZATION_EXTENSION, null, image);
        this.authConfigSettings = authConfigSettings;
        this.roleSettings = roleSettings;
        this.userAddSettings = userAddSettings;
        this.capabilities = capabilities;
    }

    public PluggableInstanceSettings getAuthConfigSettings() {
        return authConfigSettings;
    }

    public PluggableInstanceSettings getRoleSettings() {
        return roleSettings;
    }

    public PluggableInstanceSettings getUserAddSettings() {
        return userAddSettings;
    }

    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AuthorizationPluginInfo that = (AuthorizationPluginInfo) o;
        return Objects.equals(authConfigSettings, that.authConfigSettings) &&
                Objects.equals(roleSettings, that.roleSettings) &&
                Objects.equals(userAddSettings, that.userAddSettings) &&
                Objects.equals(capabilities, that.capabilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), authConfigSettings, roleSettings, userAddSettings, capabilities);
    }
}

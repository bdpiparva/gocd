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

package com.thoughtworks.go.plugin.access.authorization.v3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.domain.authorization.Capabilities;
import com.thoughtworks.go.plugin.domain.authorization.SupportedAuthType;

import java.util.Objects;

class CapabilitiesDTO {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Expose
    @SerializedName("supported_auth_type")
    private final SupportedAuthTypeDTO supportedAuthType;

    @Expose
    @SerializedName("can_search")
    private final boolean canSearch;

    @Expose
    @SerializedName("can_authorize")
    private final boolean canAuthorize;

    @Expose
    @SerializedName("can_get_user_roles")
    private final boolean canGetUserRoles;

    @Expose
    @SerializedName("supports_add_user")
    private final boolean supportsAddUser;

    public CapabilitiesDTO(SupportedAuthTypeDTO supportedAuthType, boolean canSearch, boolean canAuthorize, boolean canGetUserRoles, boolean supportsAddUser) {
        this.supportedAuthType = supportedAuthType;
        this.canSearch = canSearch;
        this.canAuthorize = canAuthorize;
        this.canGetUserRoles = canGetUserRoles;
        this.supportsAddUser = supportsAddUser;
    }

    public SupportedAuthTypeDTO getSupportedAuthType() {
        return supportedAuthType;
    }

    public boolean canSearch() {
        return canSearch;
    }

    public String toJSON() {
        return GSON.toJson(this);
    }

    public static CapabilitiesDTO fromJSON(String json) {
        return GSON.fromJson(json, CapabilitiesDTO.class);
    }

    public boolean canAuthorize() {
        return canAuthorize;
    }

    public boolean supportsAddUser() {
        return supportsAddUser;
    }

    public Capabilities toDomainModel() {
        SupportedAuthType supportedAuthType = SupportedAuthType.valueOf(this.supportedAuthType.name());
        return new Capabilities(supportedAuthType, canSearch, canAuthorize, canGetUserRoles, supportsAddUser);
    }

    public boolean canFetchUserRoles() {
        return canGetUserRoles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CapabilitiesDTO that = (CapabilitiesDTO) o;
        return canSearch == that.canSearch &&
                canAuthorize == that.canAuthorize &&
                canGetUserRoles == that.canGetUserRoles &&
                supportsAddUser == that.supportsAddUser &&
                supportedAuthType == that.supportedAuthType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(supportedAuthType, canSearch, canAuthorize, canGetUserRoles, supportsAddUser);
    }
}

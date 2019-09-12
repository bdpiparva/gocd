/*
 * Copyright 2020 ThoughtWorks, Inc.
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
package com.thoughtworks.go.domain;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

@EqualsAndHashCode(doNotUseGetters = true, callSuper = false)
@ToString(callSuper = true)
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@Entity
public class StageArtifactCleanupProhibited extends HibernatePersistedObject {
    private String pipelineName;
    private String stageName;
    private boolean prohibited;

    public StageArtifactCleanupProhibited(String pipelineName, String stageName, boolean prohibited) {
        this();
        this.pipelineName = pipelineName;
        this.stageName = stageName;
        this.prohibited = prohibited;
    }

    public StageArtifactCleanupProhibited(String pipelineName, String stageName) {
        this(pipelineName, stageName, false);
    }
}

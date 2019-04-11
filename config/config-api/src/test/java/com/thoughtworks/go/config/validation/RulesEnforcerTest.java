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

package com.thoughtworks.go.config.validation;

import com.thoughtworks.go.config.PipelineConfigs;
import com.thoughtworks.go.config.rules.Allow;
import com.thoughtworks.go.config.rules.Rules;
import com.thoughtworks.go.config.rules.RulesAware;
import com.thoughtworks.go.helper.PipelineConfigMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RulesEnforcerTest {
    private RulesAware rulesAware;
    private RulesEnforcer rulesEnforcer;

    @BeforeEach
    void setup() {
        rulesAware = mock(RulesAware.class);
        rulesEnforcer = new RulesEnforcer();
    }

    @Nested
    class canRefer {
        @Test
        void shouldBeFalseIfNoRulesDefinedThisEntityType() {
            final Rules rules = new Rules();
            final PipelineConfigs pipelineConfigs = PipelineConfigMother.createGroup("group1", "");

            when(rulesAware.rules()).thenReturn(rules);

            assertThat(rulesEnforcer.canRefer(rulesAware, pipelineConfigs)).isFalse();
        }

        @Test
        void shouldBeTrueIfRulesAllowsThisEntityType() {
            final Rules rules = new Rules(new Allow("refer", "pipeline_group", "group1"));
            final PipelineConfigs pipelineConfigs = PipelineConfigMother.createGroup("group1", "");

            when(rulesAware.rules()).thenReturn(rules);

            assertThat(rulesEnforcer.canRefer(rulesAware, pipelineConfigs)).isTrue();
        }
    }
}
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

import com.thoughtworks.go.config.Directive;
import com.thoughtworks.go.config.Validatable;
import com.thoughtworks.go.config.rules.Result;
import com.thoughtworks.go.config.rules.RulesAware;

public class RulesEnforcer {
    boolean canRefer(RulesAware rulesAwareEntity, Validatable fromEntity) {
        if (rulesAwareEntity.rules().isEmpty()) {
            return false;
        }

        for (Directive directive : rulesAwareEntity.rules()) {
            final Result result = directive.apply("refer", fromEntity.getClass(), "");
            if (result != Result.SKIP) {
                return result == Result.ALLOW;
            }

        }

        return false;
    }
}

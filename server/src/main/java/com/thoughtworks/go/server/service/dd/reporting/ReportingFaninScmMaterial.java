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
package com.thoughtworks.go.server.service.dd.reporting;

import com.thoughtworks.go.domain.PipelineTimelineEntry;

public class ReportingFaninScmMaterial {
    String fingerprint;
    PipelineTimelineEntry.Revision revision;

    public ReportingFaninScmMaterial(String fingerprint, PipelineTimelineEntry.Revision revision) {
        this.fingerprint = fingerprint;
        this.revision = revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReportingFaninScmMaterial that = (ReportingFaninScmMaterial) o;

        if (fingerprint != null ? !fingerprint.equals(that.fingerprint) : that.fingerprint != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return fingerprint != null ? fingerprint.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "" + revision;
    }
}
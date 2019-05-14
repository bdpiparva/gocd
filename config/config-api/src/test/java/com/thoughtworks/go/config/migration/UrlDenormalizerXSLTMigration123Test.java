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

package com.thoughtworks.go.config.migration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlDenormalizerXSLTMigration123Test extends UrlDenormalizerXSLTMigration121Test {

    @Nested
    class urlWithoutCredentialsAndBranch {
        @Test
        void shouldGiveUrlWithoutCredentials() {
            final String urlWithoutCredentials = UrlDenormalizerXSLTMigration123.urlWithoutCredentialsAndBranch("https://bob:pass@domain.com");

            assertThat(urlWithoutCredentials).isEqualTo("https://domain.com");
        }

        @Test
        void shouldGiveUrlWithoutBranch() {
            final String urlWithoutBranch = UrlDenormalizerXSLTMigration123.urlWithoutCredentialsAndBranch("https://domain.com##some-branch");

            assertThat(urlWithoutBranch).isEqualTo("https://domain.com");
        }

        @Test
        void shouldNotRemoveUrlFragment() {
            final String urlWithoutBranch = UrlDenormalizerXSLTMigration123.urlWithoutCredentialsAndBranch("https://domain.com#some-fragment");

            assertThat(urlWithoutBranch).isEqualTo("https://domain.com#some-fragment");
        }

        @Test
        void shouldNotRemoveTheTemplateParameter() {
            final String urlWithoutBranch = UrlDenormalizerXSLTMigration123.urlWithoutCredentialsAndBranch("https://#{rest-of-the-url}");

            assertThat(urlWithoutBranch).isEqualTo("https://#{rest-of-the-url}");
        }

        @Test
        void shouldRemoveBranchNameFromANonHttpUrl() {
            final String urlWithoutBranch = UrlDenormalizerXSLTMigration123.urlWithoutCredentialsAndBranch("/user/foo/repo##some-branch");

            assertThat(urlWithoutBranch).isEqualTo("/user/foo/repo");
        }
    }

    @Nested
    class GetUsername {
        @Test
        void shouldReturnUsername() {
            final String username = UrlDenormalizerXSLTMigration123.getUsername("https://bob:pass@domain.com");

            assertThat(username).isEqualTo("bob");
        }

        @Test
        void shouldReturnUsernameWhenBranchNameIsPresent() {
            final String username = UrlDenormalizerXSLTMigration123.getUsername("https://bob:pass@domain.com##some-branch");

            assertThat(username).isEqualTo("bob");
        }
    }

    @Nested
    class GetPassword {
        @Test
        void shouldReturnPassword() {
            final String username = UrlDenormalizerXSLTMigration123.getPassword("https://bob:pass@domain.com");

            assertThat(username).isEqualTo("pass");
        }

        @Test
        void shouldReturnPasswordWhenBranchNameIsPresent() {
            final String username = UrlDenormalizerXSLTMigration123.getPassword("https://bob:pass@domain.com##some-branch");

            assertThat(username).isEqualTo("pass");
        }
    }

    @Nested
    class GetBranch {
        @Test
        void shouldReturnBranchIfPresent() {
            final String branch = UrlDenormalizerXSLTMigration123.getBranch("https://bob:pass@domain.com##some-branch");

            assertThat(branch).isEqualTo("some-branch");
        }

        @Test
        void shouldReturnBranchFromANonHttpUrl() {
            final String branch = UrlDenormalizerXSLTMigration123.getBranch("file://bob:pass@domain.com##some-branch");

            assertThat(branch).isEqualTo("some-branch");
        }

        @Test
        void shouldIgnoreBranchNameWhenHashesAreEscaped() {
            final String branch = UrlDenormalizerXSLTMigration123.getBranch("file://bob:pass@domain.com%23%23some-branch");

            assertThat(branch).isNull();
        }

        @Test
        void shouldNotConsiderFragmentAsBranch() {
            final String branch = UrlDenormalizerXSLTMigration123.getBranch("https://bob:pass@domain.com%23some-fragment");

            assertThat(branch).isNull();
        }
    }
}
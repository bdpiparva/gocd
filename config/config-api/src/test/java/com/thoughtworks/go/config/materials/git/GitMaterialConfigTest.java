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

package com.thoughtworks.go.config.materials.git;

import com.thoughtworks.go.config.CaseInsensitiveString;
import com.thoughtworks.go.config.ConfigSaveValidationContext;
import com.thoughtworks.go.config.materials.AbstractMaterialConfig;
import com.thoughtworks.go.config.materials.Filter;
import com.thoughtworks.go.config.materials.IgnoredFiles;
import com.thoughtworks.go.config.materials.ScmMaterialConfig;
import com.thoughtworks.go.security.GoCipher;
import com.thoughtworks.go.util.ReflectionUtil;
import com.thoughtworks.go.util.command.UrlArgument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

class GitMaterialConfigTest {
    @Test
    void shouldSetConfigAttributes() {
        GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("");

        Map<String, String> map = new HashMap<>();
        map.put(GitMaterialConfig.URL, "url");
        map.put(GitMaterialConfig.BRANCH, "some-branch");
        map.put(GitMaterialConfig.SHALLOW_CLONE, "true");
        map.put(ScmMaterialConfig.FOLDER, "folder");
        map.put(ScmMaterialConfig.AUTO_UPDATE, null);
        map.put(ScmMaterialConfig.FILTER, "/root,/**/*.help");
        map.put(AbstractMaterialConfig.MATERIAL_NAME, "material-name");

        gitMaterialConfig.setConfigAttributes(map);

        assertThat(gitMaterialConfig.getUrl()).isEqualTo("url");
        assertThat(gitMaterialConfig.getFolder()).isEqualTo("folder");
        assertThat(gitMaterialConfig.getBranch()).isEqualTo("some-branch");
        assertThat(gitMaterialConfig.getName()).isEqualTo(new CaseInsensitiveString("material-name"));
        assertThat(gitMaterialConfig.isAutoUpdate()).isFalse();
        assertThat(gitMaterialConfig.isShallowClone()).isTrue();
        assertThat(gitMaterialConfig.filter()).isEqualTo(new Filter(new IgnoredFiles("/root"), new IgnoredFiles("/**/*.help")));
    }

    @Test
    void setConfigAttributes_shouldUpdatePasswordWhenPasswordChangedBooleanChanged() throws Exception {
        GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("");
        Map<String, String> map = new HashMap<>();
        map.put(GitMaterialConfig.PASSWORD, "secret");
        map.put(GitMaterialConfig.PASSWORD_CHANGED, "1");

        gitMaterialConfig.setConfigAttributes(map);
        assertThat(ReflectionUtil.getField(gitMaterialConfig, "password")).isNull();
        assertThat(gitMaterialConfig.getPassword()).isEqualTo("secret");
        assertThat(gitMaterialConfig.getEncryptedPassword()).isEqualTo(new GoCipher().encrypt("secret"));

        //Dont change
        map.put(GitMaterialConfig.PASSWORD, "Hehehe");
        map.put(GitMaterialConfig.PASSWORD_CHANGED, "0");
        gitMaterialConfig.setConfigAttributes(map);

        assertThat(ReflectionUtil.getField(gitMaterialConfig, "password")).isNull();
        assertThat(gitMaterialConfig.getPassword()).isEqualTo("secret");
        assertThat(gitMaterialConfig.getEncryptedPassword()).isEqualTo(new GoCipher().encrypt("secret"));

        map.put(GitMaterialConfig.PASSWORD, "");
        map.put(GitMaterialConfig.PASSWORD_CHANGED, "1");
        gitMaterialConfig.setConfigAttributes(map);

        assertThat(gitMaterialConfig.getPassword()).isNull();
        assertThat(gitMaterialConfig.getEncryptedPassword()).isNull();
    }

    @Test
    void byDefaultShallowCloneShouldBeOff() {
        assertThat(new GitMaterialConfig("http://url", "foo").isShallowClone()).isFalse();
        assertThat(new GitMaterialConfig("http://url", "foo", false).isShallowClone()).isFalse();
        assertThat(new GitMaterialConfig("http://url", "foo", null).isShallowClone()).isFalse();
        assertThat(new GitMaterialConfig("http://url", "foo", true).isShallowClone()).isTrue();
    }

    @Test
    void shouldReturnIfAttributeMapIsNull() {
        GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("");
        gitMaterialConfig.setConfigAttributes(null);
        assertThat(gitMaterialConfig).isEqualTo(new GitMaterialConfig(""));
    }

    @Test
    void shouldReturnTheUrl() {
        String url = "git@github.com/my/repo";
        GitMaterialConfig config = new GitMaterialConfig(url);

        assertThat(config.getUrl()).isEqualTo(url);
    }

    @Test
    void shouldReturnNullIfUrlForMaterialNotSpecified() {
        GitMaterialConfig config = new GitMaterialConfig();

        assertThat(config.getUrl()).isNull();
    }

    @Test
    void shouldSetUrlForAMaterial() {
        String url = "git@github.com/my/repo";
        GitMaterialConfig config = new GitMaterialConfig();

        config.setUrl(url);

        assertThat(config.getUrl()).isEqualTo(url);
    }

    @Test
    void shouldHandleNullWhenSettingUrlForAMaterial() {
        GitMaterialConfig config = new GitMaterialConfig();

        config.setUrl(null);

        assertThat(config.getUrl()).isNull();
    }

    @Test
    void shouldHandleNullUrlAtTheTimeOfGitMaterialConfigCreation() {
        GitMaterialConfig config = new GitMaterialConfig(null);

        assertThat(config.getUrl()).isNull();
    }

    @Test
    void shouldHandleNullBranchAtTheTimeOfMaterialConfigCreation() {
        GitMaterialConfig config1 = new GitMaterialConfig("http://url", null);
        GitMaterialConfig config2 = new GitMaterialConfig(new UrlArgument("http://url"), "bob", "pass", null, "sub1", true, new Filter(), false, "folder", new CaseInsensitiveString("git"), false);

        assertThat(config1.getBranch()).isEqualTo("master");
        assertThat(config2.getBranch()).isEqualTo("master");
    }

    @Test
    void shouldHandleNullBranchWhileSettingConfigAttributes() {
        GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("http://url", "foo");
        gitMaterialConfig.setConfigAttributes(Collections.singletonMap(GitMaterialConfig.BRANCH, null));
        assertThat(gitMaterialConfig.getBranch()).isEqualTo("master");
    }

    @Test
    void shouldHandleEmptyBranchWhileSettingConfigAttributes() {
        GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("http://url", "foo");
        gitMaterialConfig.setConfigAttributes(Collections.singletonMap(GitMaterialConfig.BRANCH, "     "));
        assertThat(gitMaterialConfig.getBranch()).isEqualTo("master");
    }

    @Nested
    class Validate {
        @Test
        void shouldEnsureUrlIsNotBlank() {
            GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("");
            gitMaterialConfig.validate(new ConfigSaveValidationContext(null));
            assertThat(gitMaterialConfig.errors().on(GitMaterialConfig.URL)).isEqualTo("URL cannot be blank");
        }
    }

    @Nested
    @TestInstance(PER_CLASS)
    class Equals {
        @Test
        void shouldBeEqualIfObjectsHaveSameUrlBranchUserNamePasswordAndSubModuleFolder() {
            final GitMaterialConfig material_1 = new GitMaterialConfig("http://example.com", "master");
            material_1.setUserName("bob");
            material_1.setSubmoduleFolder("/var/lib/git");
            material_1.setPassword("badger");

            final GitMaterialConfig material_2 = new GitMaterialConfig("http://example.com", "master");
            material_2.setUserName("bob");
            material_2.setSubmoduleFolder("/var/lib/git");
            material_2.setPassword("badger");

            assertThat(material_1.equals(material_2)).isTrue();
        }

        @ParameterizedTest
        @DisplayName("should not be equal when one the attribute from (url, branch, username, password, submodule-folder) does not match")
        @MethodSource("materialsToCompare")
        void shouldNotBeEqual(GitMaterialConfig materialConfigToCompare) {
            final GitMaterialConfig originalConfig = new GitMaterialConfig("http://example.com", "master");
            originalConfig.setUserName("bob");
            originalConfig.setSubmoduleFolder("/var/lib/git");
            originalConfig.setPassword("badger");

            assertThat(originalConfig.equals(materialConfigToCompare))
                    .as("Material config should not match when one of the material attribute from (url, branch, username, password, submodule-folder) does not match")
                    .isFalse();
        }

        private Stream<Arguments> materialsToCompare() {
            return Stream.of(
                    Arguments.of(with("http://example.com", "master", "bob", "badger", "folder")),
                    Arguments.of(with(null, "master", "bob", "badger", "folder")),
                    Arguments.of(with("http://example.com", null, "bob", "badger", "folder")),
                    Arguments.of(with("http://example.com", "master", null, "badger", "folder")),
                    Arguments.of(with("http://example.com", "master", "bob", null, "folder")),
                    Arguments.of(with("http://example.com", "master", "bob", "badger", null))
            );
        }
    }

    @Nested
    class Fingerprint {
        @Test
        void shouldGenerateFingerprintForGivenMaterialUrl() {
            GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("https://github.com/gocd");

            assertThat(gitMaterialConfig.getFingerprint()).isEqualTo("8145208f7e2161cee935b178bc4572bac883819a999cc6f449b529067ed0c63b");
        }

        @Test
        void shouldGenerateFingerprintForGivenMaterialUrlAndBranch() {
            GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("https://github.com/gocd", "feature");

            assertThat(gitMaterialConfig.getFingerprint()).isEqualTo("0addfe6e5645f786114cce8140a0b8dc51368662158a843407d6da0e17f09c05");
        }

        @Test
        void shouldIncludeUsernameAttributeInFingerprintIfPresent() {
            GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("https://github.com/gocd");
            gitMaterialConfig.setUserName("bob");

            assertThat(gitMaterialConfig.getFingerprint()).isEqualTo("83209981b57bd96565f73d3da35aab4bbafb762dd079f99171b94f9ca8634ae6");
        }

        @Test
        void shouldConsiderPasswordAttributesForGeneratingFingerPrint() {
            GitMaterialConfig withPassword = new GitMaterialConfig("https://github.com/gocd");
            withPassword.setUserName("bob");
            withPassword.setPassword("pass");

            GitMaterialConfig withoutPassword = new GitMaterialConfig("https://github.com/gocd");
            withoutPassword.setUserName("bob");

            assertThat(withPassword.getFingerprint()).isEqualTo("54588865cd963e372ea59c5970380657c5dd6d901c101951bcfc73db603dcb69");
            assertThat(withoutPassword.getFingerprint()).isEqualTo("83209981b57bd96565f73d3da35aab4bbafb762dd079f99171b94f9ca8634ae6");
            assertThat(withPassword.getFingerprint()).isNotEqualTo(withoutPassword.getFingerprint());
        }
    }

    private GitMaterialConfig with(String url, String branch, String username, String password, String submoduleFolder) {
        final GitMaterialConfig gitMaterialConfig = new GitMaterialConfig(url, branch);
        gitMaterialConfig.setUserName(username);
        gitMaterialConfig.setPassword(password);
        gitMaterialConfig.setSubmoduleFolder(submoduleFolder);
        return gitMaterialConfig;
    }
}

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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
    class Equals {
        @Test
        void shouldBeEqualIfObjectsHaveSameUrlBranchUserNameAndSubModuleFolder() {
            final GitMaterialConfig material_1 = new GitMaterialConfig("http://example.com", "master");
            material_1.setUserName("bob");
            material_1.setSubmoduleFolder("/var/lib/git");

            final GitMaterialConfig material_2 = new GitMaterialConfig("http://example.com", "master");
            material_2.setUserName("bob");
            material_2.setSubmoduleFolder("/var/lib/git");

            assertThat(material_1.equals(material_2)).isTrue();
        }

        @Test
        void shouldBeEqualIfObjectsHaveSameUrlBranchSubModuleFolder_ButNoUserName() {
            final GitMaterialConfig material_1 = new GitMaterialConfig("http://example.com", "master");
            material_1.setSubmoduleFolder("/var/lib/git");

            final GitMaterialConfig material_2 = new GitMaterialConfig("http://example.com", "master");
            material_2.setSubmoduleFolder("/var/lib/git");

            assertThat(material_1.equals(material_2)).isTrue();
        }
    }

    @Nested
    class Fingerprint {
        @Test
        void shouldGenerateFingerprintForGivenMaterialUrl() {
            GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("https://bob:pass@github.com/gocd");

            assertThat(gitMaterialConfig.getFingerprint()).isEqualTo("027675d28ace7ecc7195d3dd17002a152a6d93721c3793c769171fadef13b7a1");
        }

        @Test
        void shouldGenerateFingerprintForGivenMaterialUrlAndBranch() {
            GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("https://bob:pass@github.com/gocd", "feature");

            assertThat(gitMaterialConfig.getFingerprint()).isEqualTo("755da7fb7415c8674bdf5f8a4ba48fc3e071e5de429b1308ccf8949d215bdb08");
        }

        @Test
        void shouldIncludeUsernameAttributeInFingerprintIfPresent() {
            GitMaterialConfig gitMaterialConfig = new GitMaterialConfig("https://github.com/gocd");
            gitMaterialConfig.setUserName("bob");

            assertThat(gitMaterialConfig.getFingerprint()).isEqualTo("6c5c5ef53a996d982bff772eb0ffbe7cb7f626d54407cad1440f50e6711c4272");
        }

        @Test
        void shouldConsiderPasswordAttributesForGeneratingFingerPrint() {
            GitMaterialConfig withPassword = new GitMaterialConfig("https://github.com/gocd");
            withPassword.setUserName("bob");
            withPassword.setPassword("pass");

            GitMaterialConfig withoutPassword = new GitMaterialConfig("https://github.com/gocd");
            withoutPassword.setUserName("bob");

            assertThat(withPassword.getFingerprint()).isEqualTo("54588865cd963e372ea59c5970380657c5dd6d901c101951bcfc73db603dcb69");
            assertThat(withoutPassword.getFingerprint()).isEqualTo("6c5c5ef53a996d982bff772eb0ffbe7cb7f626d54407cad1440f50e6711c4272");
            assertThat(withPassword.getFingerprint()).isNotEqualTo(withoutPassword.getFingerprint());
        }

        @Test
        void shouldGenerateDifferentFingerprintWhenCredentialsArePartOfURLOrSpecifiedAsAttribute() {
            GitMaterialConfig gitMaterialWithCredentialsInUrl = new GitMaterialConfig("https://bob@github.com/gocd");

            GitMaterialConfig gitMaterialWithCredentialsAsAttribute = new GitMaterialConfig("https://github.com/gocd");
            gitMaterialWithCredentialsAsAttribute.setUserName("bob");

            assertThat(gitMaterialWithCredentialsInUrl.getFingerprint())
                    .isNotEqualTo(gitMaterialWithCredentialsAsAttribute.getFingerprint());

        }
    }
}

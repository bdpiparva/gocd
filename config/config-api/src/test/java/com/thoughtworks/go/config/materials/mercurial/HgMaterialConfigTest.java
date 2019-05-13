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

package com.thoughtworks.go.config.materials.mercurial;

import com.thoughtworks.go.config.CaseInsensitiveString;
import com.thoughtworks.go.config.ConfigSaveValidationContext;
import com.thoughtworks.go.config.materials.AbstractMaterialConfig;
import com.thoughtworks.go.config.materials.Filter;
import com.thoughtworks.go.config.materials.IgnoredFiles;
import com.thoughtworks.go.config.materials.ScmMaterialConfig;
import com.thoughtworks.go.security.GoCipher;
import com.thoughtworks.go.util.ReflectionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.thoughtworks.go.config.materials.AbstractMaterialConfig.MATERIAL_NAME;
import static com.thoughtworks.go.config.materials.ScmMaterialConfig.FOLDER;
import static com.thoughtworks.go.config.materials.ScmMaterialConfig.URL;
import static org.assertj.core.api.Assertions.assertThat;

class HgMaterialConfigTest {
    private HgMaterialConfig hgMaterialConfig;

    @BeforeEach
    void setUp() {
        hgMaterialConfig = new HgMaterialConfig("", null);
    }

    @Test
    void shouldSetConfigAttributes() {
        HgMaterialConfig hgMaterialConfig = new HgMaterialConfig("", null);

        Map<String, String> map = new HashMap<>();
        map.put(HgMaterialConfig.URL, "url");
        map.put(ScmMaterialConfig.FOLDER, "folder");
        map.put(ScmMaterialConfig.AUTO_UPDATE, "0");
        map.put(ScmMaterialConfig.FILTER, "/root,/**/*.help");
        map.put(AbstractMaterialConfig.MATERIAL_NAME, "material-name");

        hgMaterialConfig.setConfigAttributes(map);

        assertThat(hgMaterialConfig.getUrl()).isEqualTo("url");
        assertThat(hgMaterialConfig.getFolder()).isEqualTo("folder");
        assertThat(hgMaterialConfig.getName()).isEqualTo(new CaseInsensitiveString("material-name"));
        assertThat(hgMaterialConfig.isAutoUpdate()).isFalse();
        assertThat(hgMaterialConfig.filter()).isEqualTo(new Filter(new IgnoredFiles("/root"), new IgnoredFiles("/**/*.help")));
    }

    @Test
    void setConfigAttributes_shouldUpdatePasswordWhenPasswordChangedBooleanChanged() throws Exception {
        HgMaterialConfig hgMaterialConfig = new HgMaterialConfig();
        Map<String, String> map = new HashMap<>();
        map.put(HgMaterialConfig.PASSWORD, "secret");
        map.put(HgMaterialConfig.PASSWORD_CHANGED, "1");

        hgMaterialConfig.setConfigAttributes(map);
        assertThat(ReflectionUtil.getField(hgMaterialConfig, "password")).isNull();
        assertThat(hgMaterialConfig.getPassword()).isEqualTo("secret");
        assertThat(hgMaterialConfig.getEncryptedPassword()).isEqualTo(new GoCipher().encrypt("secret"));

        //Dont change
        map.put(HgMaterialConfig.PASSWORD, "Hehehe");
        map.put(HgMaterialConfig.PASSWORD_CHANGED, "0");
        hgMaterialConfig.setConfigAttributes(map);

        assertThat(ReflectionUtil.getField(hgMaterialConfig, "password")).isNull();
        assertThat(hgMaterialConfig.getPassword()).isEqualTo("secret");
        assertThat(hgMaterialConfig.getEncryptedPassword()).isEqualTo(new GoCipher().encrypt("secret"));

        map.put(HgMaterialConfig.PASSWORD, "");
        map.put(HgMaterialConfig.PASSWORD_CHANGED, "1");
        hgMaterialConfig.setConfigAttributes(map);

        assertThat(hgMaterialConfig.getPassword()).isNull();
        assertThat(hgMaterialConfig.getEncryptedPassword()).isNull();
    }


    @Test
    void validate_shouldEnsureUrlIsNotBlank() {
        HgMaterialConfig hgMaterialConfig = new HgMaterialConfig("", null);
        hgMaterialConfig.validate(new ConfigSaveValidationContext(null));
        assertThat(hgMaterialConfig.errors().on(HgMaterialConfig.URL)).isEqualTo("URL cannot be blank");
    }

    @Test
    void shouldReturnIfAttributeMapIsNull() {
        HgMaterialConfig hgMaterialConfig = new HgMaterialConfig("", null);

        hgMaterialConfig.setConfigAttributes(null);

        assertThat(hgMaterialConfig).isEqualTo(new HgMaterialConfig("", null));
    }

    @Test
    void shouldReturnTheUrl() {
        String url = "git@github.com/my/repo";
        HgMaterialConfig config = new HgMaterialConfig(url, null);

        assertThat(config.getUrl()).isEqualTo(url);
    }

    @Test
    void shouldReturnNullIfUrlForMaterialNotSpecified() {
        HgMaterialConfig config = new HgMaterialConfig();

        assertThat(config.getUrl()).isNull();
    }

    @Test
    void shouldSetUrlForAMaterial() {
        String url = "git@github.com/my/repo";
        HgMaterialConfig config = new HgMaterialConfig();

        config.setUrl(url);

        assertThat(config.getUrl()).isEqualTo(url);
    }

    @Test
    void shouldHandleNullWhenSettingUrlForAMaterial() {
        HgMaterialConfig config = new HgMaterialConfig();

        config.setUrl(null);

        assertThat(config.getUrl()).isNull();
    }

    @Nested
    class Equals {
        @Test
        void shouldBeEqualIfObjectsHaveSameUrlBranchAndUserName() {
            final HgMaterialConfig material_1 = new HgMaterialConfig("http://example.com", "master");
            material_1.setUserName("bob");
            material_1.setBranch("feature");

            final HgMaterialConfig material_2 = new HgMaterialConfig("http://example.com", "master");
            material_2.setUserName("bob");
            material_2.setBranch("feature");

            assertThat(material_1.equals(material_2)).isTrue();
        }

        @Test
        void shouldBeEqualIfObjectsHaveSameUrl_ButNoUserNameAndBranch() {
            final HgMaterialConfig material_1 = new HgMaterialConfig("http://example.com", "master");

            final HgMaterialConfig material_2 = new HgMaterialConfig("http://example.com", "master");

            assertThat(material_1.equals(material_2)).isTrue();
        }
    }

    @Nested
    class Fingerprint {
        @Test
        void shouldGenerateFingerprintForGivenMaterialUrl() {
            HgMaterialConfig hgMaterialConfig = new HgMaterialConfig("https://bob:pass@github.com/gocd##feature", "dest");

            assertThat(hgMaterialConfig.getFingerprint()).isEqualTo("e47f18ffc2dba81e6ec75bfa50f95936415d4f4f0efb4ca285f04fce7a0310cb");
        }

        @Test
        void shouldConsiderBranchWhileGeneratingFingerprint_IfBranchSpecifiedAsAnAttribute() {
            HgMaterialConfig hgMaterialConfig = new HgMaterialConfig("https://bob:pass@github.com/gocd", "dest");
            hgMaterialConfig.setBranch("feature");

            assertThat(hgMaterialConfig.getFingerprint()).isEqualTo("db13278ed2b804fc5664361103bcea3d7f5106879683085caed4311aa4d2f888");
        }

        @Test
        void shouldConsiderUserNameWhileGeneratingFingerprint_IfUserNameSpecifiedAsAnAttribute() {
            HgMaterialConfig hgMaterialConfig = new HgMaterialConfig("https://github.com/gocd", "dest");
            hgMaterialConfig.setUserName("bob");

            assertThat(hgMaterialConfig.getFingerprint()).isEqualTo("aac496fb490cf99dcdb9a88c8bac5fb9cad5774a1ee074f74bd9e85ff2084685");
        }

        @Test
        void shouldNotConsiderPasswordAttributesForGeneratingFingerPrint() {
            HgMaterialConfig withoutPassword = new HgMaterialConfig("https://github.com/gocd", "dest");
            withoutPassword.setUserName("bob");

            HgMaterialConfig withPassword = new HgMaterialConfig("https://github.com/gocd", "dest");
            withPassword.setUserName("bob");
            withPassword.setPassword("pass");

            assertThat(withoutPassword.getFingerprint()).isEqualTo(withPassword.getFingerprint());
        }

        @Test
        void userNameInUrlShouldGenerateFingerprintWhichIsOtherFromUserNameInAttribute() {
            HgMaterialConfig hgMaterialConfigWithCredentialsInUrl = new HgMaterialConfig("https://bob@github.com/gocd", "dest");

            HgMaterialConfig hgMaterialConfigWithCredentialsAsAttribute = new HgMaterialConfig("https://github.com/gocd", "dest");
            hgMaterialConfigWithCredentialsAsAttribute.setUserName("bob");

            assertThat(hgMaterialConfigWithCredentialsInUrl.getFingerprint())
                    .isNotEqualTo(hgMaterialConfigWithCredentialsAsAttribute.getFingerprint());

        }

        @Test
        void branchInUrlShouldGenerateFingerprintWhichIsOtherFromBranchInAttribute() {
            HgMaterialConfig hgMaterialConfigWithBranchInUrl = new HgMaterialConfig("https://github.com/gocd##feature", "dest");

            HgMaterialConfig hgMaterialConfigWithBranchAsAttribute = new HgMaterialConfig("https://github.com/gocd", "dest");
            hgMaterialConfigWithBranchAsAttribute.setBranch("feature");

            assertThat(hgMaterialConfigWithBranchInUrl.getFingerprint())
                    .isNotEqualTo(hgMaterialConfigWithBranchAsAttribute.getFingerprint());
        }
    }

    @Nested
    class ValidateURL {
        @Test
        void shouldEnsureUrlIsNotBlank() {
            hgMaterialConfig.setUrl("");
            hgMaterialConfig.validate(new ConfigSaveValidationContext(null));

            assertThat(hgMaterialConfig.errors().on(ScmMaterialConfig.URL)).isEqualTo("URL cannot be blank");
        }

        @Test
        void shouldEnsureUrlIsNotNull() {
            hgMaterialConfig.setUrl(null);

            hgMaterialConfig.validate(new ConfigSaveValidationContext(null));

            assertThat(hgMaterialConfig.errors().on(URL)).isEqualTo("URL cannot be blank");
        }

        @Test
        void shouldEnsureMaterialNameIsValid() {
            hgMaterialConfig.validate(new ConfigSaveValidationContext(null));
            assertThat(hgMaterialConfig.errors().on(MATERIAL_NAME)).isNull();

            hgMaterialConfig.setName(new CaseInsensitiveString(".bad-name-with-dot"));
            hgMaterialConfig.validate(new ConfigSaveValidationContext(null));
            assertThat(hgMaterialConfig.errors().on(MATERIAL_NAME)).isEqualTo("Invalid material name '.bad-name-with-dot'. This must be alphanumeric and can contain underscores and periods (however, it cannot start with a period). The maximum allowed length is 255 characters.");
        }

        @Test
        void shouldEnsureDestFilePathIsValid() {
            hgMaterialConfig.setConfigAttributes(Collections.singletonMap(FOLDER, "../a"));
            hgMaterialConfig.validate(new ConfigSaveValidationContext(null));
            assertThat(hgMaterialConfig.errors().on(FOLDER)).isEqualTo("Dest folder '../a' is not valid. It must be a sub-directory of the working folder.");
        }
    }

    @Nested
    class FingerPrintShouldNotChangeBecauseOfUrlDenormalize {
        @Test
        void shouldNotChangeFingerprintForHttpUrlWithCredentials() {
            HgMaterialConfig migratedConfig = new HgMaterialConfig("http://github.com/gocd/gocd", "my-branch");
            migratedConfig.setUserName("bobfoo@example.com");
            migratedConfig.setPassword("p@ssw&rd:");
            assertThat(migratedConfig.getFingerprint()).isEqualTo("ff407f3ab9623d2a87c7c7037388863e30711ccda837fee54685ae490cea9b1b");

        }

        @Test
        void shouldNotChangeFingerprintForHttpsUrlWithCredentials() {
            HgMaterialConfig migratedConfig = new HgMaterialConfig("https://github.com/gocd/gocd", "my-branch");
            migratedConfig.setUserName("bobfoo@example.com");
            migratedConfig.setPassword("p@ssw&rd:");
            assertThat(migratedConfig.getFingerprint()).isEqualTo("0128b4baa42f594edebf0aa8b03accb775437f87e24c091df43f7089d9273379");

        }

        @Test
        void shouldNotChangeFingerprintForHttpUrlWithUsername() {
            HgMaterialConfig migratedConfig = new HgMaterialConfig("https://github.com/gocd/gocd", "my-branch");
            migratedConfig.setUserName("some-hex-key");

            assertThat(migratedConfig.getFingerprint()).isEqualTo("740752da427d67093b8e41d2484d0408caa7a6e6aa39df670789a35d36a1c4fd");
        }

        @Test
        void shouldChangeFingerprintForHttpUrlWithUsernameAndColonWithNoPassword() {
            HgMaterialConfig config = new HgMaterialConfig("https://some-hex-key:@github.com/gocd/gocd", "my-branch");
            assertThat(config.getFingerprint()).isNotEqualTo("2a8d3901b89ab34c75b5a5a0ce2fccaf1deef76e30e9534c9770e123534813ba");
            assertThat(config.getFingerprint()).isEqualTo("740752da427d67093b8e41d2484d0408caa7a6e6aa39df670789a35d36a1c4fd");

            HgMaterialConfig migratedConfig = new HgMaterialConfig("https://github.com/gocd/gocd", "my-branch");
            migratedConfig.setUserName("some-hex-key");

            assertThat(config.getFingerprint()).isEqualTo(migratedConfig.getFingerprint());
        }

        @Test
        void shouldNotChangeFingerprintForHttpUrlWithPassword() {
            HgMaterialConfig config = new HgMaterialConfig("https://:some-hex-key@github.com/gocd/gocd", "my-branch");
            assertThat(config.getFingerprint()).isEqualTo("a8fa1c0729bd9687f31493e97281339cc8987779264e1f59d741be264c738f53");

            HgMaterialConfig migratedConfig = new HgMaterialConfig("https://github.com/gocd/gocd", "my-branch");
            migratedConfig.setPassword("some-hex-key");

            assertThat(config.getFingerprint()).isEqualTo(migratedConfig.getFingerprint());
        }
    }
}

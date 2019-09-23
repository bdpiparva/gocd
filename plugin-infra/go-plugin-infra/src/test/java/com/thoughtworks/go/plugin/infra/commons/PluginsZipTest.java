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
package com.thoughtworks.go.plugin.infra.commons;

import com.thoughtworks.go.plugin.FileHelper;
import com.thoughtworks.go.plugin.infra.PluginManager;
import com.thoughtworks.go.plugin.infra.plugininfo.GoPluginBundleDescriptor;
import com.thoughtworks.go.plugin.infra.plugininfo.GoPluginDescriptor;
import com.thoughtworks.go.util.SystemEnvironment;
import org.apache.commons.collections4.EnumerationUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.ZipFile;

import static com.thoughtworks.go.util.SystemEnvironment.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class PluginsZipTest {
    private SystemEnvironment systemEnvironment;
    private PluginsZip pluginsZip;
    private String expectedZipPath;
    private File externalPluginsDir;
    private PluginManager pluginManager;
    private GoPluginBundleDescriptor bundledTaskPlugin;
    private GoPluginBundleDescriptor externalTaskPlugin;
    private GoPluginBundleDescriptor externalElasticAgentPlugin;
    private File bundledPluginsDir;
    private FileHelper temporaryFolder;

    @BeforeEach
    void setUp(@TempDir File rootDir) throws Exception {
        temporaryFolder = new FileHelper(rootDir);
        pluginManager = mock(PluginManager.class);
        temporaryFolder.newFolder();
        systemEnvironment = mock(SystemEnvironment.class);
        bundledPluginsDir = temporaryFolder.newFolder("plugins-bundled");
        expectedZipPath = temporaryFolder.newFile("go-plugins-all.zip").getAbsolutePath();
        externalPluginsDir = temporaryFolder.newFolder("plugins-external");

        when(systemEnvironment.get(PLUGIN_GO_PROVIDED_PATH)).thenReturn(bundledPluginsDir.getAbsolutePath());
        when(systemEnvironment.get(PLUGIN_EXTERNAL_PROVIDED_PATH)).thenReturn(externalPluginsDir.getAbsolutePath());
        when(systemEnvironment.get(ALL_PLUGINS_ZIP_PATH)).thenReturn(expectedZipPath);

        pluginsZip = spy(new PluginsZip(systemEnvironment, pluginManager));

        File bundledTask1Jar = createPluginFile(this.bundledPluginsDir, "bundled-task-1.jar", "Bundled1");
        File bundledAuth2Jar = createPluginFile(this.bundledPluginsDir, "bundled-auth-2.jar", "Bundled2");
        File bundledscm3Jar = createPluginFile(this.bundledPluginsDir, "bundled-scm-3.jar", "Bundled3");
        File bundledPackageMaterialJar = createPluginFile(this.bundledPluginsDir, "bundled-package-material-4.jar", "Bundled4");

        File externalTask1Jar = createPluginFile(externalPluginsDir, "external-task-1.jar", "External1");
        File externalElastic1Jar = createPluginFile(externalPluginsDir, "external-elastic-agent-2.jar", "External2");
        File externalscm3Jar = createPluginFile(externalPluginsDir, "external-scm-3.jar", "External3");
        File externalPackageMaterialJar = createPluginFile(externalPluginsDir, "external-package-material-4.jar", "External3");

        bundledTaskPlugin = new GoPluginBundleDescriptor(getPluginDescriptor(bundledTask1Jar, "bundled-task-1"));
        GoPluginBundleDescriptor bundledAuthPlugin = new GoPluginBundleDescriptor(getPluginDescriptor(bundledAuth2Jar, "bundled-auth-2"));
        GoPluginBundleDescriptor bundledSCMPlugin = new GoPluginBundleDescriptor(getPluginDescriptor(bundledscm3Jar, "bundled-scm-3"));
        GoPluginBundleDescriptor bundledPackageMaterialPlugin = new GoPluginBundleDescriptor(getPluginDescriptor(bundledPackageMaterialJar, "bundled-package-material-4"));


        externalTaskPlugin = new GoPluginBundleDescriptor(getPluginDescriptor(externalTask1Jar, "external-task-1"));
        externalElasticAgentPlugin = new GoPluginBundleDescriptor(getPluginDescriptor(externalElastic1Jar, "external-elastic-agent-2"));
        GoPluginBundleDescriptor externalSCMPlugin = new GoPluginBundleDescriptor(getPluginDescriptor(externalscm3Jar, "external-scm-3"));
        GoPluginBundleDescriptor externalPackageMaterialPlugin = new GoPluginBundleDescriptor(getPluginDescriptor(externalPackageMaterialJar, "external-package-material-4"));

        when(pluginManager.plugins()).thenReturn(Arrays.asList(
                bundledTaskPlugin.descriptors().get(0),
                bundledAuthPlugin.descriptors().get(0),
                bundledSCMPlugin.descriptors().get(0),
                bundledPackageMaterialPlugin.descriptors().get(0),
                externalTaskPlugin.descriptors().get(0),
                externalElasticAgentPlugin.descriptors().get(0),
                externalSCMPlugin.descriptors().get(0),
                externalPackageMaterialPlugin.descriptors().get(0)
        ));

        when(pluginManager.isPluginOfType("task", "bundled-task-1")).thenReturn(true);
        when(pluginManager.isPluginOfType("task", "external-task-1")).thenReturn(true);
        when(pluginManager.isPluginOfType("package-repository", "bundled-package-material-4")).thenReturn(true);
        when(pluginManager.isPluginOfType("scm", "bundled-scm-3")).thenReturn(true);
        when(pluginManager.isPluginOfType("scm", "external-scm-3")).thenReturn(true);
        when(pluginManager.isPluginOfType("package-repository", "external-package-material-4")).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        temporaryFolder.delete();
    }

    @Test
    void shouldZipTaskPluginsIntoOneZipEveryTime() throws Exception {
        pluginsZip.create();

        assertThat(new File(expectedZipPath).exists()).as(expectedZipPath + " should exist").isTrue();
        assertThat(new ZipFile(expectedZipPath).getEntry("bundled/bundled-task-1.jar")).isNotNull();
        assertThat(new ZipFile(expectedZipPath).getEntry("bundled/bundled-scm-3.jar")).isNotNull();
        assertThat(new ZipFile(expectedZipPath).getEntry("bundled/bundled-package-material-4.jar")).isNotNull();
        assertThat(new ZipFile(expectedZipPath).getEntry("external/external-task-1.jar")).isNotNull();
        assertThat(new ZipFile(expectedZipPath).getEntry("external/external-scm-3.jar")).isNotNull();
        assertThat(new ZipFile(expectedZipPath).getEntry("external/external-package-material-4.jar")).isNotNull();

        assertThat(new ZipFile(expectedZipPath).getEntry("bundled/bundled-auth-2.jar")).isNull();
        assertThat(new ZipFile(expectedZipPath).getEntry("external/external-elastic-agent-2.jar")).isNull();
    }

    @Test
    void shouldGetChecksumIfFileWasCreated() {
        pluginsZip.create();
        String md5 = pluginsZip.md5();
        assertThat(md5).isNotNull();
    }

    @Test
    void shouldUpdateChecksumIfFileIsReCreated() throws Exception {
        pluginsZip.create();
        String oldMd5 = pluginsZip.md5();
        FileUtils.writeStringToFile(new File(externalPluginsDir, "external-task-1.jar"), UUID.randomUUID().toString(), UTF_8);
        pluginsZip.create();
        assertThat(pluginsZip.md5()).isNotEqualTo(oldMd5);
    }

    @Test
    void shouldFailGracefullyWhenExternalFileCannotBeRead() throws Exception {
        File bundledPluginsDir = temporaryFolder.newFolder("plugins-bundled-ext");
        SystemEnvironment systemEnvironmentFail = mock(SystemEnvironment.class);
        when(systemEnvironmentFail.get(PLUGIN_GO_PROVIDED_PATH)).thenReturn(bundledPluginsDir.getAbsolutePath());
        when(systemEnvironmentFail.get(PLUGIN_EXTERNAL_PROVIDED_PATH)).thenReturn("");
        when(systemEnvironmentFail.get(ALL_PLUGINS_ZIP_PATH)).thenReturn("");
        FileUtils.writeStringToFile(new File(bundledPluginsDir, "bundled-task-1.jar"), "Bundled1", UTF_8);

        PluginsZip pluginsZipFail = new PluginsZip(systemEnvironmentFail, pluginManager);
        assertThatCode(pluginsZipFail::create)
                .isInstanceOf(FileAccessRightsCheckException.class);
    }

    @Test
    void shouldFailGracefullyWhenBundledFileCannotBeRead() throws Exception {
        SystemEnvironment systemEnvironmentFail = mock(SystemEnvironment.class);
        when(systemEnvironmentFail.get(PLUGIN_GO_PROVIDED_PATH)).thenReturn("");
        when(systemEnvironmentFail.get(PLUGIN_EXTERNAL_PROVIDED_PATH)).thenReturn(externalPluginsDir.getAbsolutePath());
        when(systemEnvironmentFail.get(ALL_PLUGINS_ZIP_PATH)).thenReturn("");
        FileUtils.writeStringToFile(new File(externalPluginsDir, "external-task-1.jar"), "External1", UTF_8);

        PluginsZip pluginsZipFail = new PluginsZip(systemEnvironmentFail, pluginManager);
        assertThatCode(pluginsZipFail::create)
                .isInstanceOf(FileAccessRightsCheckException.class);
    }

    @Test
    void fileAccessErrorShouldContainPathToTheFolderInWhichTheErrorOccurred() throws Exception {
        SystemEnvironment systemEnvironmentFail = mock(SystemEnvironment.class);
        when(systemEnvironmentFail.get(PLUGIN_GO_PROVIDED_PATH)).thenReturn("/dummy");
        when(systemEnvironmentFail.get(PLUGIN_EXTERNAL_PROVIDED_PATH)).thenReturn(externalPluginsDir.getAbsolutePath());
        when(systemEnvironmentFail.get(ALL_PLUGINS_ZIP_PATH)).thenReturn("");
        FileUtils.writeStringToFile(new File(externalPluginsDir, "external-task-1.jar"), "External1", UTF_8);

        PluginsZip pluginsZipFail = new PluginsZip(systemEnvironmentFail, pluginManager);
        assertThatCode(pluginsZipFail::create)
                .isInstanceOf(FileAccessRightsCheckException.class)
                .hasMessageContaining("dummy");
    }


    @Test
    void shouldCreatePluginsWhenTaskPluginsAreAdded() {
        GoPluginDescriptor plugin = GoPluginDescriptor.builder().id("curl-task-plugin").build();
        when(pluginManager.isPluginOfType("task", plugin.id())).thenReturn(true);
        pluginsZip.pluginLoaded(plugin);
        verify(pluginsZip, times(1)).create();
    }

    @Test
    void shouldCreatePluginsWhenTaskPluginsAreRemoved() {
        pluginsZip.pluginUnLoaded(externalTaskPlugin.descriptors().get(0));
        verify(pluginsZip, times(1)).create();
    }

    @Test
    void shouldDoNothingWhenAPluginThatIsNotATaskOrScmOrPackageMaterialPluginPluginIsAdded() {
        pluginsZip.pluginLoaded(externalElasticAgentPlugin.descriptors().get(0));
        verify(pluginsZip, never()).create();
    }

    @Test
    void shouldDoNothingWhenAPluginThatIsNotATaskOrScmOrPackageMaterialPluginPluginIsRemoved() {
        pluginsZip.pluginUnLoaded(externalElasticAgentPlugin.descriptors().get(0));
        verify(pluginsZip, never()).create();
    }

    @Test
    void shouldCreateAZipWithOneCopyOfEachJar_ForAPluginBundleWithMultiplePluginsInIt() throws IOException {
        File bundled_plugin = createPluginFile(bundledPluginsDir, "bundled-multi-plugin-1.jar", "Bundled1");
        File external_plugin = createPluginFile(externalPluginsDir, "external-multi-plugin-1.jar", "External1");

        bundledTaskPlugin = new GoPluginBundleDescriptor(
                getPluginDescriptor(bundled_plugin, "bundled-plugin-1"),
                getPluginDescriptor(bundled_plugin, "bundled-plugin-2")
        );

        externalTaskPlugin = new GoPluginBundleDescriptor(
                getPluginDescriptor(external_plugin, "external-plugin-1"),
                getPluginDescriptor(external_plugin, "external-plugin-2")
        );

        when(pluginManager.plugins()).thenReturn(Arrays.asList(
                bundledTaskPlugin.descriptors().get(0),
                bundledTaskPlugin.descriptors().get(1),

                externalTaskPlugin.descriptors().get(0),
                externalTaskPlugin.descriptors().get(1)
        ));

        when(pluginManager.isPluginOfType("task", "bundled-plugin-1")).thenReturn(true);
        when(pluginManager.isPluginOfType("scm", "bundled-plugin-2")).thenReturn(true);
        when(pluginManager.isPluginOfType("task", "external-plugin-1")).thenReturn(true);
        when(pluginManager.isPluginOfType("artifact", "external-plugin-2")).thenReturn(true);


        pluginsZip = spy(new PluginsZip(systemEnvironment, pluginManager));
        pluginsZip.create();


        assertThat(new File(expectedZipPath).exists()).as(expectedZipPath + " should exist").isTrue();
        assertThat(EnumerationUtils.toList(new ZipFile(expectedZipPath).entries()).size()).isEqualTo(2);
        assertThat(new ZipFile(expectedZipPath).getEntry("bundled/bundled-multi-plugin-1.jar")).isNotNull();
        assertThat(new ZipFile(expectedZipPath).getEntry("external/external-multi-plugin-1.jar")).isNotNull();
    }

    private GoPluginDescriptor getPluginDescriptor(File bundled_plugin, String jarFileLocation) {
        return GoPluginDescriptor.builder().id(jarFileLocation).version("1.0").pluginJarFileLocation(bundled_plugin.getAbsolutePath()).build();
    }

    private File createPluginFile(File pluginsDir, String pluginJarFileName, String contents) throws IOException {
        File bundledTask1Jar = new File(pluginsDir, pluginJarFileName);
        FileUtils.writeStringToFile(bundledTask1Jar, contents, UTF_8);
        return bundledTask1Jar;
    }
}


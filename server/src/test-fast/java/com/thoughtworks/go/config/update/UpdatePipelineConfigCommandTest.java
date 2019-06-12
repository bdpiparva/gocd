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
package com.thoughtworks.go.config.update;

import com.thoughtworks.go.config.*;
import com.thoughtworks.go.domain.ConfigErrors;
import com.thoughtworks.go.helper.GoConfigMother;
import com.thoughtworks.go.helper.JobConfigMother;
import com.thoughtworks.go.helper.PipelineConfigMother;
import com.thoughtworks.go.server.domain.Username;
import com.thoughtworks.go.server.exceptions.RulesViolationException;
import com.thoughtworks.go.server.service.EntityHashingService;
import com.thoughtworks.go.server.service.ExternalArtifactsService;
import com.thoughtworks.go.server.service.GoConfigService;
import com.thoughtworks.go.server.service.RulesService;
import com.thoughtworks.go.server.service.result.LocalizedOperationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UpdatePipelineConfigCommandTest {

    private EntityHashingService entityHashingService;
    private GoConfigService goConfigService;
    private Username username;
    private LocalizedOperationResult localizedOperationResult;
    private PipelineConfig pipelineConfig;
    private ExternalArtifactsService externalArtifactsService;
    private RulesService rulesService;


    @BeforeEach
    void setUp() throws Exception {
        entityHashingService = mock(EntityHashingService.class);
        externalArtifactsService = mock(ExternalArtifactsService.class);
        goConfigService = mock(GoConfigService.class);
        username = mock(Username.class);
        localizedOperationResult = mock(LocalizedOperationResult.class);
        rulesService = mock(RulesService.class);
        pipelineConfig = PipelineConfigMother.pipelineConfig("p1");
    }

    @Test
    void shouldDisallowStaleRequest() {
        UpdatePipelineConfigCommand command = new UpdatePipelineConfigCommand(goConfigService, entityHashingService,
                pipelineConfig, username, "stale_md5", localizedOperationResult, externalArtifactsService, rulesService);

        when(goConfigService.findGroupNameByPipeline(pipelineConfig.name())).thenReturn("group1");
        when(goConfigService.canEditPipeline(pipelineConfig.name().toString(), username, localizedOperationResult, "group1")).thenReturn(true);
        when(entityHashingService.md5ForEntity(pipelineConfig)).thenReturn("latest_md5");

        BasicCruiseConfig basicCruiseConfig = new BasicCruiseConfig(new BasicPipelineConfigs(pipelineConfig));
        assertThat(command.canContinue(basicCruiseConfig)).isFalse();
    }

    @Test
    void shouldDisallowUpdateIfPipelineEditIsDisAllowed() {
        UpdatePipelineConfigCommand command = new UpdatePipelineConfigCommand(goConfigService, null,
                pipelineConfig, username, "stale_md5", localizedOperationResult, externalArtifactsService, rulesService);

        when(goConfigService.findGroupNameByPipeline(pipelineConfig.name())).thenReturn("group1");
        when(goConfigService.canEditPipeline(pipelineConfig.name().toString(), username, localizedOperationResult, "group1")).thenReturn(false);
        assertThat(command.canContinue(mock(CruiseConfig.class))).isFalse();
    }

    @Test
    void shouldInvokeUpdateMethodOfCruiseConfig() {
        UpdatePipelineConfigCommand command = new UpdatePipelineConfigCommand(goConfigService, null,
                pipelineConfig, username, "stale_md5", localizedOperationResult, externalArtifactsService, rulesService);

        CruiseConfig cruiseConfig = mock(CruiseConfig.class);
        when(goConfigService.findGroupNameByPipeline(pipelineConfig.name())).thenReturn("group1");

        command.update(cruiseConfig);
        verify(cruiseConfig).update("group1", pipelineConfig.name().toString(), pipelineConfig);
    }


    @Test
    void shouldEncryptSecurePropertiesOfPipelineConfig() {
        PipelineConfig pipelineConfig = mock(PipelineConfig.class);
        UpdatePipelineConfigCommand command = new UpdatePipelineConfigCommand(goConfigService, null,
                pipelineConfig, username, "stale_md5", localizedOperationResult, externalArtifactsService, rulesService);

        when(pipelineConfig.name()).thenReturn(new CaseInsensitiveString("p1"));
        CruiseConfig preprocessedConfig = mock(CruiseConfig.class);
        when(preprocessedConfig.getPipelineConfigByName(new CaseInsensitiveString("p1"))).thenReturn(mock(PipelineConfig.class));

        command.encrypt(preprocessedConfig);

        verify(pipelineConfig).encryptSecureProperties(eq(preprocessedConfig), any(PipelineConfig.class));
    }

    @Nested
    class isValid {
        @Test
        void updatePipelineConfigShouldValidateAllExternalArtifacts() {
            PluggableArtifactConfig s3 = mock(PluggableArtifactConfig.class);
            PluggableArtifactConfig docker = mock(PluggableArtifactConfig.class);
            when(goConfigService.artifactStores()).thenReturn(mock(ArtifactStores.class));
            when(goConfigService.findGroupNameByPipeline(new CaseInsensitiveString("P1"))).thenReturn("group");
            ConfigErrors configErrors = new ConfigErrors();
            when(s3.errors()).thenReturn(configErrors);
            when(docker.errors()).thenReturn(configErrors);
            JobConfig job1 = JobConfigMother.jobWithNoResourceRequirement();
            JobConfig job2 = JobConfigMother.jobWithNoResourceRequirement();

            job1.artifactConfigs().add(s3);
            job2.artifactConfigs().add(docker);

            PipelineConfig pipeline = PipelineConfigMother.pipelineConfig("P1", new StageConfig(new CaseInsensitiveString("S1"), new JobConfigs(job1)),
                    new StageConfig(new CaseInsensitiveString("S2"), new JobConfigs(job2)));

            UpdatePipelineConfigCommand command = new UpdatePipelineConfigCommand(goConfigService, null,
                    pipeline, username, "stale_md5", localizedOperationResult, externalArtifactsService, rulesService);

            BasicCruiseConfig preprocessedConfig = GoConfigMother.defaultCruiseConfig();
            preprocessedConfig.addPipelineWithoutValidation("group", pipeline);
            command.isValid(preprocessedConfig);

            verify(externalArtifactsService).validateExternalArtifactConfig(eq(s3), any(), eq(true));
            verify(externalArtifactsService).validateExternalArtifactConfig(eq(docker), any(), eq(true));
        }

        @Test
        void updatePipelineConfigShouldValidateAllFetchExternalArtifactTasks() {
            JobConfig job1 = JobConfigMother.jobWithNoResourceRequirement();
            JobConfig job2 = JobConfigMother.jobWithNoResourceRequirement();

            when(goConfigService.findGroupNameByPipeline(new CaseInsensitiveString("P1"))).thenReturn("group");

            FetchPluggableArtifactTask fetchS3Task = new FetchPluggableArtifactTask(new CaseInsensitiveString("p0"), new CaseInsensitiveString("s0"), new CaseInsensitiveString("j0"), "s3");
            FetchPluggableArtifactTask fetchDockerTask = new FetchPluggableArtifactTask(new CaseInsensitiveString("p0"), new CaseInsensitiveString("s0"), new CaseInsensitiveString("j0"), "docker");

            job1.addTask(fetchS3Task);
            job2.addTask(fetchDockerTask);

            PipelineConfig pipeline = PipelineConfigMother.pipelineConfig("P1", new StageConfig(new CaseInsensitiveString("S1"), new JobConfigs(job1)),
                    new StageConfig(new CaseInsensitiveString("S2"), new JobConfigs(job2)));

            UpdatePipelineConfigCommand command = new UpdatePipelineConfigCommand(goConfigService, null,
                    pipeline, username, "stale_md5", localizedOperationResult, externalArtifactsService, rulesService);

            BasicCruiseConfig preprocessedConfig = GoConfigMother.defaultCruiseConfig();
            preprocessedConfig.addPipelineWithoutValidation("group", pipeline);
            command.isValid(preprocessedConfig);


            verify(externalArtifactsService, times(2)).validateFetchExternalArtifactTask(any(FetchPluggableArtifactTask.class), any(PipelineConfig.class), eq(preprocessedConfig));
        }

        @Test
        void shouldErrorOutWhenPipelineCannotReferToASecretConfig() {
            PipelineConfig testPipelineConfig = PipelineConfigMother.pipelineConfig("test");
            UpdatePipelineConfigCommand command = new UpdatePipelineConfigCommand(goConfigService, entityHashingService,
                    testPipelineConfig, username, "md5", localizedOperationResult, externalArtifactsService, rulesService);

            BasicCruiseConfig preprocessedConfig = GoConfigMother.defaultCruiseConfig();
            preprocessedConfig.addPipelineWithoutValidation("group", testPipelineConfig);

            doThrow(new RulesViolationException("Some exception")).when(rulesService).validateSecretConfigReferences(any(PipelineConfig.class));
            when(goConfigService.findGroupNameByPipeline(new CaseInsensitiveString("test"))).thenReturn("group");

            boolean result = command.isValid(preprocessedConfig);

            assertThat(result).isFalse();
            verify(localizedOperationResult).unprocessableEntity("Some exception");
        }


        @Test
        void shouldBeValidWhenPipelineCanReferToASecretConfig() {
            PipelineConfig testPipelineConfig = PipelineConfigMother.pipelineConfig("test");
            UpdatePipelineConfigCommand command = new UpdatePipelineConfigCommand(goConfigService, entityHashingService,
                    testPipelineConfig, username, "md5", localizedOperationResult, externalArtifactsService, rulesService);

            BasicCruiseConfig preprocessedConfig = GoConfigMother.defaultCruiseConfig();
            preprocessedConfig.addPipelineWithoutValidation("group", testPipelineConfig);

            when(goConfigService.findGroupNameByPipeline(new CaseInsensitiveString("test"))).thenReturn("group");
            when(localizedOperationResult.isSuccessful()).thenReturn(true);

            boolean result = command.isValid(preprocessedConfig);

            assertThat(result).isTrue();
            verify(localizedOperationResult, never()).unprocessableEntity(anyString());
            verify(rulesService).validateSecretConfigReferences(any(PipelineConfig.class));
        }
    }
}
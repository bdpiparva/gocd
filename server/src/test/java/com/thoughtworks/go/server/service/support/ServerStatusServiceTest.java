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

package com.thoughtworks.go.server.service.support;

import com.thoughtworks.go.config.exceptions.NotAuthorizedException;
import com.thoughtworks.go.server.domain.Username;
import com.thoughtworks.go.server.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class ServerStatusServiceTest {
    @Mock
    private SecurityService securityService;
    @Mock
    private ServerInfoWriter serverInfoWriter;
    @Mock
    private ServerInfoProvider serverInfoProvider;

    private ServerStatusService serverStatusService;
    private Username username;

    @BeforeEach
    void setUp() {
        initMocks(this);
        username = new Username("bob");
        serverStatusService = new ServerStatusService(securityService, serverInfoProvider);
    }

    @Test
    void shouldWriteServerInfo() {
        when(serverInfoProvider.name()).thenReturn("Dummy");
        when(securityService.isUserAdmin(username)).thenReturn(true);

        serverStatusService.serverInfo(username, serverInfoWriter);

        verify(serverInfoWriter).add(eq("Timestamp"), anyString());
        verify(serverInfoWriter).addChild(eq("Dummy"), any());
    }

    @Test
    void shouldThrowNotAuthorizedExceptionWhenUserIsNotAAdmin() {
        when(securityService.isUserAdmin(username)).thenReturn(false);

        assertThatCode(() -> serverStatusService.serverInfo(username, serverInfoWriter))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessage("Does not have permissions to get server info. User have to be an admin in order to see the server info");

        verifyZeroInteractions(serverInfoProvider);
        verifyZeroInteractions(serverInfoWriter);
    }
}
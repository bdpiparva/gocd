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

package com.thoughtworks.go.server.newsecurity.filterchains;

import com.thoughtworks.go.http.mocks.HttpRequestBuilder;
import com.thoughtworks.go.http.mocks.MockHttpServletRequest;
import com.thoughtworks.go.http.mocks.MockHttpServletRequestAssert;
import com.thoughtworks.go.http.mocks.MockHttpServletResponse;
import com.thoughtworks.go.server.newsecurity.filters.SetupSecurityFilter;
import com.thoughtworks.go.server.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import static com.thoughtworks.go.http.mocks.MockHttpServletResponseAssert.*;
import static com.thoughtworks.go.server.newsecurity.filterchains.DenyGoCDAccessForArtifactsFilterChainTest.wrap;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class SetupSecurityFilterChainTest {
    @Mock
    private FilterChain filterChain;
    @Mock
    private SecurityService securityService;

    private SetupSecurityFilter setupSecurityFilter;

    private SetupSecurityFilterChain setupSecurityFilterChain;

    @BeforeEach
    void setUp() {
        initMocks(this);
        setupSecurityFilter = spy(new SetupSecurityFilter(securityService));
        setupSecurityFilterChain = new SetupSecurityFilterChain(setupSecurityFilter);
    }

    @Test
    void shouldContinueIfRequestIsToSetupSecurity() throws IOException, ServletException {
        MockHttpServletRequest request = HttpRequestBuilder.GET("/setup_security").build();
        MockHttpServletResponse response = new MockHttpServletResponse();

        setupSecurityFilterChain.doFilter(request, response, filterChain);

        verifyZeroInteractions(setupSecurityFilter);
        assertThat(response).isOk();
    }

    @Test
    void shouldCallSetupSecurityFilterIfRequestIsNotForSecuritySetup() throws IOException, ServletException {
        MockHttpServletRequest request = HttpRequestBuilder.GET("/some-request").build();
        HttpSession session = request.getSession(true);

        MockHttpServletResponse response = new MockHttpServletResponse();

        setupSecurityFilterChain.doFilter(request, response, filterChain);

        assertThat(response).redirectsTo("/setup_security");
        MockHttpServletRequestAssert.assertThat(request)
                .hasSameSession(session);
    }
}

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

package com.thoughtworks.go.server.newsecurity.filters;

import com.thoughtworks.go.config.SecurityAuthConfig;
import com.thoughtworks.go.config.SecurityAuthConfigs;
import com.thoughtworks.go.http.mocks.HttpRequestBuilder;
import com.thoughtworks.go.http.mocks.MockHttpServletRequest;
import com.thoughtworks.go.http.mocks.MockHttpServletResponse;
import com.thoughtworks.go.server.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

import static com.thoughtworks.go.http.mocks.MockHttpServletResponseAssert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class SetupSecurityFilterTest {
    @Mock
    private SecurityService securityService;
    @Mock
    private FilterChain filterChain;

    private SetupSecurityFilter setupSecurityFilter;

    @BeforeEach
    void setUp() {
        initMocks(this);
        setupSecurityFilter = new SetupSecurityFilter(securityService);
    }


    @Test
    void shouldRedirectToSetupSecurityPage() throws ServletException, IOException {
        MockHttpServletRequest request = HttpRequestBuilder.GET("/some-request").build();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(securityService.isSecurityEnabled()).thenReturn(false);

        setupSecurityFilter.doFilter(request, response, filterChain);

        assertThat(response).redirectsTo("/setup_security");
        verifyNoMoreInteractions(filterChain);
    }

    @Test
    void shouldContinueWithRequestWhenSecurityIsInPlace() throws ServletException, IOException {
        MockHttpServletRequest request = HttpRequestBuilder.GET("/some-request").build();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(securityService.isSecurityEnabled()).thenReturn(true);

        setupSecurityFilter.doFilter(request, response, filterChain);

        assertThat(response).isOk();
        verify(filterChain).doFilter(request, response);
    }
}

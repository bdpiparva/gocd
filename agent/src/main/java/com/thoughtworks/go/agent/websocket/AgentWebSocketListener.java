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

package com.thoughtworks.go.agent.websocket;

import com.thoughtworks.go.agent.websocket.annontations.SubscribeTo;
import com.thoughtworks.go.agent.websocket.services.TokenFileService;
import com.thoughtworks.go.security.Registration;
import com.thoughtworks.go.security.RegistrationJSONizer;
import com.thoughtworks.go.websocket.DefaultMessage;
import com.thoughtworks.go.websocket.RegistrationMessage;
import org.springframework.stereotype.Component;

@Component
public class AgentWebSocketListener {
    private final TokenFileService tokenFileService;

    public AgentWebSocketListener(TokenFileService tokenFileService) {
        this.tokenFileService = tokenFileService;
    }

    @SubscribeTo(destination = "/agent/topic/token")
    public void gotToken(DefaultMessage message) {
        tokenFileService.store((String) message.get("token"));
    }

    @SubscribeTo(destination = "/agent/topic/registration", responseType = RegistrationMessage.class)
    public void onRegistration(RegistrationMessage registrationMessage) {
        final Registration registration = RegistrationJSONizer.fromJson(registrationMessage.getRegistrationInfo());
        System.out.println("Is valid: " + registration.isValid());
        System.out.println(registration);
    }
}

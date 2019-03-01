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

package com.thoughtworks.go.websocket;

import com.thoughtworks.go.websocket.services.AgentRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private final AgentRegistrationService agentRegistrationService;

    @Autowired
    public WebSocketController(AgentRegistrationService agentRegistrationService) {
        this.agentRegistrationService = agentRegistrationService;
    }

    @MessageMapping("/token")
    @SendToUser("/topic/token")
    public WebSocketMessage token(GetTokenMessage message) {
        return agentRegistrationService.getToken(message.getGuid());
    }

    @MessageMapping("/registration")
    @SendToUser("/topic/registration")
    public WebSocketMessage registration(@Payload RegisterAgentMessage registerAgentMessage, SimpMessageHeaderAccessor headerAccessor) {
        return agentRegistrationService.registerAgent((String) headerAccessor.getSessionAttributes().get("clientIp"), registerAgentMessage);
    }
}
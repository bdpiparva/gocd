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

import com.thoughtworks.go.util.SystemEnvironment;
import com.thoughtworks.go.websocket.DefaultMessage;
import com.thoughtworks.go.websocket.RegisterAgentMessage;
import com.thoughtworks.go.websocket.WebSocketMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.net.MalformedURLException;
import java.net.URL;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class GoAgentWebSocketClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoAgentWebSocketClient.class);
    private static final String WEBSOCKET_PATH = "foo-websocket";
    private final WebSocketStompClient stompClient;
    private final AgentMessageHandler agentMessageHandler;
    private final SystemEnvironment systemEnvironment;
    private StompSession session;

    @Autowired
    public GoAgentWebSocketClient(AgentMessageHandler agentMessageHandler, SystemEnvironment systemEnvironment) {
        this.agentMessageHandler = agentMessageHandler;
        this.systemEnvironment = systemEnvironment;

        final StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        final WebSocketTransport transport = new WebSocketTransport(standardWebSocketClient);
        final SockJsClient socketClient = new SockJsClient(singletonList(transport));

        stompClient = new WebSocketStompClient(socketClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    void tryEstablishingConnection() {
        try {
            if (session != null && session.isConnected()) {
                LOGGER.debug("Agent is already connected with server {}.", systemEnvironment.getServiceUrl());
                return;
            }

            LOGGER.debug("Connecting to server using url {}.", systemEnvironment.getServiceUrl());
            session = stompClient.connect(websocketUrl(systemEnvironment), agentMessageHandler)
                    .get(1, SECONDS);
            LOGGER.debug("Connected.");

        } catch (Exception e) {
            LOGGER.error("Some error: ", e);
            throw new RuntimeException(e);
        }
    }

    private String websocketUrl(SystemEnvironment systemEnvironment) throws MalformedURLException {
        final URL url = new URL(systemEnvironment.getServiceUrl());
        return String.format("ws://%s:%d/go/" + WEBSOCKET_PATH, url.getHost(), 8153);
    }

    void registerAgent(RegisterAgentMessage registerAgentMessage) {
        send("/app/registration", registerAgentMessage);
    }

    void retrieveToken(String guid) {
        send("/app/token", new DefaultMessage().with("guid", guid));
    }

    private void send(String destination, WebSocketMessage payload) {
        LOGGER.debug("Sending message to {} with payload {}.", destination, payload);
        session.send(destination, payload);
        LOGGER.debug("Payload successfully sent to {}.", destination);
    }

}

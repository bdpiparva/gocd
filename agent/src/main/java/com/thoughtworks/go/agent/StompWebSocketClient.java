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

package com.thoughtworks.go.agent;

import com.google.gson.Gson;
import com.thoughtworks.go.websocket.Ping;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;

public class StompWebSocketClient {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        String url = "ws://localhost:8153/go/foo-websocket";
        final SockJsClient socketClient = new SockJsClient(singletonList(new WebSocketTransport(new StandardWebSocketClient())));
        final WebSocketStompClient client = new WebSocketStompClient(socketClient);
        final StompSession stompSession = client.connect(url, HANDLER).get(1, SECONDS);

        stompSession.subscribe("/app/registration", new DefaultStompFrameHandler());
        stompSession.subscribe("/agent/registration", new DefaultStompFrameHandler());
        stompSession.subscribe("/user/registration", new DefaultStompFrameHandler());
        stompSession.subscribe("/user/queue/registration", new DefaultStompFrameHandler());
        stompSession.subscribe("/agent/queue/registration", new DefaultStompFrameHandler());
        while (stompSession.isConnected()) {
            final Ping ping = new Ping();
            ping.setName("Bhupendra");
            stompSession.send("/app/registration", new Gson().toJson(ping).getBytes());
            System.out.println("Sleeping for 10 secs");
            Thread.sleep(10000);
        }
    }

    static class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            return byte[].class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            System.out.println(new String((byte[]) o, StandardCharsets.UTF_8));
        }
    }

    private static final StompSessionHandler HANDLER = new StompSessionHandler() {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println(session);
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.out.println(exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.out.println(exception);
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return null;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {

        }
    };
}

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

import static com.thoughtworks.go.websocket.ErrorCode.CONFLICT;
import static com.thoughtworks.go.websocket.ErrorCode.FORBIDDEN;

public class ErrorMessage implements WebSocketMessage {
    private ErrorCode code;
    private String message;

    public ErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ErrorMessage unprocessable(String message) {
        return newInstance(ErrorCode.UNPROCESSABLE_ENTITY, message);
    }
    
    public static ErrorMessage forbidden(String message) {
        return newInstance(FORBIDDEN, message);
    }

    public static ErrorMessage conflict(String message) {
        return newInstance(CONFLICT, message);
    }

    private static ErrorMessage newInstance(ErrorCode code, String message) {
        final ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.code = code;
        errorMessage.message = message;
        return errorMessage;
    }
}

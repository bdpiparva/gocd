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

package com.thoughtworks.go.api.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.api.base.OutputWriter;
import com.thoughtworks.go.server.service.support.ServerInfoWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

public class ServerInfoWriterDelegator implements ServerInfoWriter {
    private static final Gson GSON = new GsonBuilder().create();
    private OutputWriter outputWriter;

    public ServerInfoWriterDelegator(OutputWriter outputWriter) {
        this.outputWriter = outputWriter;
    }

    @Override
    public ServerInfoWriter add(String key, String value) {
        outputWriter.add(key, value);
        return this;
    }

    @Override
    public ServerInfoWriter add(String key, long value) {
        outputWriter.add(key, value);
        return this;
    }

    @Override
    public ServerInfoWriter add(String key, boolean value) {
        outputWriter.add(key, value);
        return this;
    }

    @Override
    public ServerInfoWriter add(String key, double value) {
        outputWriter.add(key, String.valueOf(value));
        return this;
    }

    @Override
    public ServerInfoWriter addChild(String key, Consumer<ServerInfoWriter> writerConsumer) {
        outputWriter.addChild(key, outputWriter -> writerConsumer.accept(new ServerInfoWriterDelegator(outputWriter)));
        return this;
    }

    @Override
    public ServerInfoWriter addJsonNode(String key, Object object) {
        try {
            outputWriter.add(key, new ObjectMapper().readTree(GSON.toJson(object)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ServerInfoWriter addChildList(String key, Collection<String> values) {
        outputWriter.addChildList(key, values);
        return this;
    }
}

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

package com.thoughtworks.go.config.migration;

import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import static com.thoughtworks.go.util.command.HgUrlArgument.DOUBLE_HASH;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class UrlDenormalizerXSLTMigration123 {

    public static String urlWithoutCredentialsAndBranch(String originalUrl) {
        try {
            final URIBuilder uriBuilder = new URIBuilder(sanitizeUrl(originalUrl));

            if (hasBranch(originalUrl)) {
                uriBuilder.setFragment(null);
            }

            if (isSupportedUrl(originalUrl)) {
                uriBuilder.setUserInfo(null);
            }

            return uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            return originalUrl;
        }
    }

    public static String getUsername(String originalUrl) {
        try {
            if (isSupportedUrl(originalUrl)) {
                String[] credentials = getCredentials(originalUrl);
                if (credentials != null) {
                    if ("".equals(credentials[0])) {
                        return null;
                    } else {
                        return credentials[0];
                    }
                }
            }
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String getPassword(String originalUrl) {
        try {
            if (isSupportedUrl(originalUrl)) {
                String[] credentials = getCredentials(originalUrl);
                if (credentials != null && credentials.length >= 2) {
                    if ("".equals(credentials[1])) {
                        return null;
                    } else {
                        return credentials[1];
                    }
                }
            }
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static boolean isSupportedUrl(String originalUrl) throws URISyntaxException {
        if (isNotBlank(originalUrl) && (originalUrl.startsWith("http") || originalUrl.startsWith("https"))) {
            new URI(sanitizeUrl(originalUrl));
            return true;
        }

        return false;
    }

    private static boolean hasBranch(String originalUrl) {
        return !originalUrl.equals(sanitizeUrl(originalUrl));
    }

    private static String sanitizeUrl(String originalUrl) {
        return originalUrl.replace(DOUBLE_HASH, "#");
    }

    private static String[] getCredentials(String originalUrl) throws URISyntaxException {
        String userInfo = new URI(sanitizeUrl(originalUrl)).getUserInfo();
        if (isNotBlank(userInfo)) {
            return userInfo.split(":", 2);
        }
        return null;
    }

    public static String getBranch(String originalUrl) {
        try {
            if (hasBranch(originalUrl)) {
                return new URI(sanitizeUrl(originalUrl)).getFragment();
            }
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
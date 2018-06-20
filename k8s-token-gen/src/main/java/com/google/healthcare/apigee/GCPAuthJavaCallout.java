/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.healthcare.apigee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


import java.io.*;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.*;


/**
 * Basic Apigee Java callout for invoking GCP access token generation endpoint.
 * Google OAuth2 API described at <a
 * href="http://code.google.com/apis/accounts/docs/OAuth2Login.html">Using OAuth 2.0 for Login
 * (Experimental)</a>.
 *
 * Portions of this code come from the Google Java Oauth command line sample at
 * <a href="https://github.com/google/google-api-java-client-samples/tree/master/oauth2-cmdline-sample">Google Java Oauth command line sample</a>
 */
public class GCPAuthJavaCallout {
    private static final String MSG_NO_APPLICATION_ID_FOUND = "No application ID found in message context";
    private static final String MSG_NO_GCP_CLIENT_ID_FOUND = "No GCP client ID found in message context";
    private static final String MSG_NO_GCP_CLIENT_SECRET_FOUND = "No GCP client secret found in message context";

    public static final String APPLICATION_NAME_KEY = "applicationName";
    public static final String GRANT_TYPE_KEY = "grant_type";
    public static final String SCOPE_KEY = "scope";
    public static final String GCP_CLIENT_ID_KEY = "private.iam.gcpClientApiKey";
    public static final String GCP_CLIENT_SECRET_KEY = "key";
    public static final String ACCESS_TOKEN_KEY = "private.iam.accessToken";
    public static final String EXPIRATION_INTERVAL_SECONDS_KEY = "private.iam.expirationIntervalSeconds";
    public static final String ERROR_STRING_KEY = "errorString";

    private static final long   EXPIRES_IMMEDIATELY = 0;

    public OAuthTokenResponse getToken(final OAuthTokenRequest tokenRequest) throws IOException {
        /** OAuth 2.0 scopes. */
        final List<String> SCOPES = Arrays.asList(
                "https://www.googleapis.com/auth/cloud.platform",
                "https://www.googleapis.com/auth/cloud-healthcare"
                );

        if (StringUtils.isEmpty(tokenRequest.getApplicationName())) {
            throw new IllegalArgumentException(MSG_NO_APPLICATION_ID_FOUND);
        }

        if (tokenRequest.getServiceAccountKey() == null) {
            throw new IllegalArgumentException(MSG_NO_GCP_CLIENT_SECRET_FOUND);
        }

        final ObjectMapper mapper = new ObjectMapper();
        final String serviceAccountKey = mapper.writeValueAsString(tokenRequest.getServiceAccountKey());


        GoogleCredentials credentials =
                GoogleCredentials.fromStream(new ByteArrayInputStream(serviceAccountKey.getBytes()))
                        .createScoped(Collections.singleton("https://www.googleapis.com/auth/cloud-healthcare"));
        AccessToken accessToken = credentials.getAccessToken();
        if (accessToken == null) {
            accessToken = credentials.refreshAccessToken();
        }

        if (accessToken != null) {
            final Instant now = Instant.now();
            final Instant expiration = accessToken.getExpirationTime().toInstant();
            final long expirationIntervalInSeconds =
                    now.isBefore(expiration)
                            ? expiration.getEpochSecond() - now.getEpochSecond()
                            : EXPIRES_IMMEDIATELY;

            return new OAuthTokenResponse(accessToken.getTokenValue(), expirationIntervalInSeconds);
        } else {
            throw new RuntimeException("No access token generated");
        }
    }


}
/*
 * Copyright (c) 2018 Google Inc.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;


/**
 * Basic class for invoking GCP access token generation endpoint.
 */
public class GCPAuthJavaCallout {
    private static final String MSG_NO_APPLICATION_ID_FOUND = "No application ID found in message context";
    private static final String MSG_NO_GCP_CLIENT_SECRET_FOUND = "No GCP client secret found in message context";
    private static final String MSG_NO_ACCESS_TOKEN_GENERATED = "No access token generated by Google Auth SDK";

    private static final long   EXPIRES_IMMEDIATELY = 0;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public OAuthTokenResponse getToken(final OAuthTokenRequest tokenRequest) throws IOException {
        if (StringUtils.isEmpty(tokenRequest.getApplicationName())) {
            throw new IllegalArgumentException(MSG_NO_APPLICATION_ID_FOUND);
        }

        if (tokenRequest.getServiceAccountKey() == null) {
            throw new IllegalArgumentException(MSG_NO_GCP_CLIENT_SECRET_FOUND);
        }

        final String serviceAccountKey = OBJECT_MAPPER.writeValueAsString(tokenRequest.getServiceAccountKey());


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
            throw new RuntimeException(MSG_NO_ACCESS_TOKEN_GENERATED);
        }
    }


}
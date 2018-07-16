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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthTokenRequest {
    private String applicationName;
    private String grantType;
    private String scopes;
    private GCPServiceAccountKey serviceAccountKey;

    @JsonCreator
    public OAuthTokenRequest(@JsonProperty("applicationName") final String applicationName,
                             @JsonProperty("grantType") final String grantType,
                             @JsonProperty("scopes") final String scopes,
                             @JsonProperty("key") final GCPServiceAccountKey serviceAccountKey)
    {
        this.applicationName = applicationName;
        this.grantType = grantType;
        this.scopes = scopes;
        this.serviceAccountKey = serviceAccountKey;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getGrantType() {
        return grantType;
    }

    public String getScopes() {
        return scopes;
    }

    public GCPServiceAccountKey getServiceAccountKey() {
        return serviceAccountKey;
    }

    public String toString() {
        return String.format("applicationName = \"%s\", grantType = \"%s\", scopes = \"%s\", keyPresent = \"%s\"",
                getApplicationName(), getGrantType(), getScopes(), ((getServiceAccountKey() != null) ? "true" : "false"));
    }

}

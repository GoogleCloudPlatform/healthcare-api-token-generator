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
        return String.format("applicationName = \"%s\", grantType = \"%s\", scopes = \"%s\"",
                getApplicationName(), getGrantType(), getScopes());
    }

}

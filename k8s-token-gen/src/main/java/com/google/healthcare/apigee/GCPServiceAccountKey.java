package com.google.healthcare.apigee;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GCPServiceAccountKey {
    private String type;
    private String project_id;
    private String private_key_id;
    private String private_key;
    private String client_email;
    private String client_id;
    private String auth_uri;
    private String token_uri;
    private String auth_provider_x509_cert_url;
    private String client_x509_cert_url;

    @JsonCreator
    public GCPServiceAccountKey(@JsonProperty("type") final String type,
                                @JsonProperty("project_id") final String project_id,
                                @JsonProperty("private_key_id") final String private_key_id,
                                @JsonProperty("private_key") final String private_key,
                                @JsonProperty("client_email") final String client_email,
                                @JsonProperty("client_id") final String client_id,
                                @JsonProperty("auth_uri") final String auth_uri,
                                @JsonProperty("token_uri") final String token_uri,
                                @JsonProperty("auth_provider_x509_cert_url") final String auth_provider_x509_cert_url,
                                @JsonProperty("client_x509_cert_url") final String client_x509_cert_url) {
        this.type = type;
        this.project_id = project_id;
        this.private_key_id = private_key_id;
        this.private_key = private_key;
        this.client_email = client_email;
        this.client_id = client_id;
        this.auth_uri = auth_uri;
        this.token_uri = token_uri;
        this.auth_provider_x509_cert_url = auth_provider_x509_cert_url;
        this.client_x509_cert_url = client_x509_cert_url;
    }

    public String getType() {
        return type;
    }

    public String getProject_id() {
        return project_id;
    }

    public String getPrivate_key_id() {
        return private_key_id;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public String getClient_email() {
        return client_email;
    }

    public String getClient_id() {
        return client_id;
    }

    public String getAuth_uri() {
        return auth_uri;
    }

    public String getToken_uri() {
        return token_uri;
    }

    public String getAuth_provider_x509_cert_url() {
        return auth_provider_x509_cert_url;
    }

    public String getClient_x509_cert_url() {
        return client_x509_cert_url;
    }

}
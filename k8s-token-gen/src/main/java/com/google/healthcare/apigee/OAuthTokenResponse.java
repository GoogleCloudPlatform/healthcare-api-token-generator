package com.google.healthcare.apigee;

public class OAuthTokenResponse {
    private String accessToken;
    private Long expiresin;

    public OAuthTokenResponse(final String accessToken, final Long expiresin) {
        this.accessToken = accessToken;
        this.expiresin = expiresin;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresin() {
        return expiresin;
    }
}

package com.skripsi.siap_sewa.dto.authentication.token;

public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
    private final String tokenType = "Bearer";
}
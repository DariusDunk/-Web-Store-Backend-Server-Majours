package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenRefreshResponse(@JsonProperty("access_token")
                                   String accessToken,
                                   @JsonProperty("expires_in")
                                   int expiresIn,
                                   @JsonProperty("refresh_expires_in")
                                   int refreshExpiresIn,
                                   @JsonProperty("refresh_token")
                                   String refreshToken) {
}

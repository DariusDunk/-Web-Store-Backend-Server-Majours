package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshResponse(@JsonProperty("access_token")
                              String accessToken,
                              @JsonProperty("expires_in")
                              int expiresIn,
                              @JsonProperty("refresh_expires_in")
                              int refreshExpiresIn,
                              @JsonProperty("refresh_token")
                              String refreshToken,
                              @JsonProperty("session_expires_in")
                              long sessionExpiresIn,
                              @JsonProperty("is_guest")
                              boolean isGuest,
                              @JsonProperty("is_remember_me")
                              boolean isRememberMe,
                              @JsonProperty("session_id")
                              String validSessionId){ }

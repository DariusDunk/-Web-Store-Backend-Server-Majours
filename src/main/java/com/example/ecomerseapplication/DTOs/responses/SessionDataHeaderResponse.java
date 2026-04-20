package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionDataHeaderResponse(@JsonProperty("session_id")
                                        String sessionId,
                                        @JsonProperty("session_expires_in")
                                        Long sessionTTLSeconds,
                                        @JsonProperty("is_guest")
                                        boolean isGuest,
                                        @JsonProperty("is_replaced")
                                        boolean isReplaced,
                                        @JsonProperty("is_remember_me")
                                        boolean isRememberMe) {


}

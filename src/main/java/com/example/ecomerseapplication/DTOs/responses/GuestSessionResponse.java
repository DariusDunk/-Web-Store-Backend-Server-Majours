package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GuestSessionResponse(@JsonProperty("session_id") String guestSessionId,
                                   @JsonProperty("session_expires_in") Long ttlSeconds) {
}

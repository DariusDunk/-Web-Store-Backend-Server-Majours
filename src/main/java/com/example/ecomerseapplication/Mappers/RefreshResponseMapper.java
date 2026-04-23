package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.RefreshResponse;
import com.example.ecomerseapplication.DTOs.responses.TokenRefreshResponse;

import java.time.Duration;
import java.time.Instant;

public class RefreshResponseMapper {
    public static RefreshResponse tokenRefreshToRefreshResponse(TokenRefreshResponse tokenRefreshResponse,
                                                                Instant sessionExpiry,
                                                                boolean isGuest,
                                                                boolean isRememberMe,
                                                                String validSessionId) {
        return new RefreshResponse(tokenRefreshResponse.accessToken(),
                Duration.between(Instant.now(), sessionExpiry).getSeconds(),
                isGuest,
                isRememberMe,
                validSessionId
                );
    }
}

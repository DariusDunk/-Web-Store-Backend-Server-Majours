package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.SessionDataHeaderResponse;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Services.SessionService;

public class SessionMapper {

    public static SessionDataHeaderResponse entToHeaderResponse(Session session, boolean isReplaced) {

        long ttl = SessionService.calculateSessionTTLSeconds(session.getExpiresAt());

        return new SessionDataHeaderResponse(session.getSessionId(),
                ttl,
                session.getIsGuest(),
                isReplaced,
                session.getIsRememberMeSession());
    }
}

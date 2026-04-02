package com.example.ecomerseapplication.Auth.helpers;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SessionExtractor {
    public String getSessionId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes();

        return (String) attributes
                .getRequest()
                .getAttribute("sessionId");
    }
}

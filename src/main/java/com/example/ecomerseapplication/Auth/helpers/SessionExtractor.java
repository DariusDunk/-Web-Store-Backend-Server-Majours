package com.example.ecomerseapplication.Auth.helpers;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@SuppressWarnings("ConstantConditions")
public class SessionExtractor {
    public static String getRequestSessionId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes();

        if (attributes == null) {
            return null;
        }

        return (String) attributes
                .getRequest()
                .getAttribute("sessionId");
    }
}

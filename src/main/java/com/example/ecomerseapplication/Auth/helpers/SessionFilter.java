package com.example.ecomerseapplication.Auth.helpers;

import com.example.ecomerseapplication.Services.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SessionFilter extends OncePerRequestFilter {

    private final SessionService sessionService;

    public SessionFilter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String sessionId = request.getHeader("X-Session-Id");

        if (sessionId != null) {
            request.setAttribute("sessionId", sessionId);

            // Update last activity (throttled inside service)
            sessionService.updateActivity(sessionId);
        }
        //tuk moje da se pravi testvane na skorost na zaqvkata kato se sloji taimer predi tova izvikvane (koeto realno pozvolqva da se izpylni zaqvkata sled filtyra), koioto da svyr6i sled nego
        filterChain.doFilter(request, response);
    }
}

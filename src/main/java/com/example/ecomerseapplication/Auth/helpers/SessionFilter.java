package com.example.ecomerseapplication.Auth.helpers;

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
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId != null) {
            // Optionally attach sessionId to a request attribute or SecurityContext
            request.setAttribute("sessionId", sessionId);
        }

        filterChain.doFilter(request, response);
    }
}

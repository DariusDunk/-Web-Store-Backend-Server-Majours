package com.example.ecomerseapplication.Auth.helpers;

import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Mappers.SessionMapper;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.Services.ClientTypeService;
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
    private final ClientTypeService clientTypeService;
    private final EndpointMatcher endpointMatcher;

    public SessionFilter(SessionService sessionService, ClientTypeService clientTypeService, EndpointMatcher endpointMatcher) {
        this.sessionService = sessionService;
        this.clientTypeService = clientTypeService;
        this.endpointMatcher = endpointMatcher;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        boolean isPublic = isPublicEndpoint(request);

        if (!isPublic)
        {
//            System.out.println("(Semi-)Protected endpoint accessed, checking session...");
            sessionValidation(request);
        }
//        else
//            System.out.println("Public endpoint accessed, skipping session check...");
        //tuk moje da se pravi testvane na skorost na zaqvkata kato se sloji taimer predi tova izvikvane (koeto realno pozvolqva da se izpylni zaqvkata sled filtyra), koioto da svyr6i sled nego
        filterChain.doFilter(request, response);

        if (!isPublic)
        {
            Session session = (Session) request.getAttribute(GlobalConstants.SESSION_ATTRIBUTE);
            Boolean sessionReplaced = (Boolean) request.getAttribute(GlobalConstants.IS_REPLACED_ATTRIBUTE);

            if (session != null && sessionReplaced==true) {
                System.out.println("Session replaced, sending new session info...");
                response.setHeader("x-session-info", String.valueOf(SessionMapper.entToHeaderResponse(session, sessionReplaced)));
            }
        }

    }

    private void sessionValidation(@NonNull HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-Id");

        String clientTypeName = request.getHeader("X-Client-Type");
        if (clientTypeName == null)
            clientTypeName = "Web";

        request.setAttribute(GlobalConstants.CLIENT_TYPE_ATTRIBUTE, clientTypeName);

        Session session = sessionService.getActiveByIdOptional(sessionId).orElse(null);
        boolean sessionReplaced = false;

        if (session == null) {
            sessionReplaced = true;
            ClientType clientType = clientTypeService.getByTypeName(clientTypeName);
            session = sessionService.createGuestSession(clientType);
        }

        request.setAttribute(GlobalConstants.SESSION_ATTRIBUTE, session);
        request.setAttribute(GlobalConstants.IS_REPLACED_ATTRIBUTE, sessionReplaced);

        // Update last activity (throttled inside service)
        if (session != null) {
            sessionService.updateActivity(session);
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return endpointMatcher.isPublic(path);
    }

//    private boolean isLoginEndpoint(HttpServletRequest request) {
//        String path = request.getRequestURI();
//        return
//    }
}

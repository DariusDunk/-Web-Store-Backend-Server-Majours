package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.Repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public Session createSession(String refreshToken, Customer customer, ClientType clientType, boolean rememberMe, int refreshTokenExpirySeconds) {
        Session session = new Session();

        session.setSessionId(generateSessionId());

        session.setCustomer(customer);
        if (rememberMe) {
            session.setExpiresAt(Instant.now().plus(refreshTokenExpirySeconds, ChronoUnit.SECONDS));
        } else {
            session.setExpiresAt(Instant.now().plus(GlobalConstants.NORMAL_SESSION_TTL_HOURS, ChronoUnit.HOURS));
        }

        session.setRefreshToken(refreshToken);
        session.setIsGuest(false);// TODO tova 6te se promeni v byde6te no ne tuk lol
        session.setIsRevoked(false);
        session.setClientType(clientType);
        session.setRememberMeSession(rememberMe);

       return sessionRepository.save(session);

    }

    private String generateSessionId() {
        byte[] random = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    public void revokeSession(Session session) {
        session.setIsRevoked(true);
        session.setRevokedAt(Instant.now());
        sessionRepository.save(session);
    }

    public void updateActivity() {

        String sessionId = (String) ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                .getRequest().getAttribute("sessionId");

        Session session = getById(sessionId);
        Instant now = Instant.now();

        if (now.isAfter(session.getLastActivityAt().plus(5, ChronoUnit.MINUTES))) {

            System.out.println("Updating session activity timestamp");

            session.setLastActivityAt(now);
            sessionRepository.save(session);
        }

    }

    public Session getById(String sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    public void save(Session session) {
        sessionRepository.save(session);
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void revokeExpiredOnStartup() {
//        System.out.println("-------------------------Revoking expired sessions on startup-------------------------");
        revokeExpiredSessions();
    }
    @Transactional
    @Scheduled(fixedDelay = 300000)
    public void revokeExpiredPeriodically() {
        revokeExpiredSessions();
    }

    public void revokeExpiredSessions() {

        int revokedSessions = sessionRepository.revokeExpired();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (revokedSessions > 0) {
            System.out.println("-------------------------["+now.format(formatter)+"]"+" Revoking expired sessions-------------------------");
            System.out.println("-------------------------Revoked " + revokedSessions + " expired sessions-------------------------");
        }

    }
}

package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Auth.helpers.SessionExtractor;
import com.example.ecomerseapplication.DTOs.responses.KeycloakTokenResponse;
import com.example.ecomerseapplication.DTOs.responses.ReportResponses;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.SessionActivityProjection;
import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidSessionException;
import com.example.ecomerseapplication.Repositories.SessionRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.example.ecomerseapplication.Others.GlobalConstants.*;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CartService cartService;

    @Autowired
    public SessionService(SessionRepository sessionRepository, CartService cartService) {
        this.sessionRepository = sessionRepository;
        this.cartService = cartService;
    }

    public Session guestToLoginSession(Session session, KeycloakTokenResponse tokenResponse, boolean isRememberMe, Customer customer) {

        session.markAsAuthenticated(customer,
                tokenResponse.refreshToken(),
                isRememberMe,
                tokenResponse.refreshExpiresIn(), //todo tuk trqbva da se kriptira refresh tokena
                NORMAL_SESSION_TTL_HOURS);

        return sessionRepository.save(session);

    }

    private String generateSessionId() {
        byte[] random = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    public void updateActivity(Session session) {

        boolean hasCart = cartService.existsBySession(session);

        if (session.registerActivity(hasCart, LOW_PRIORITY_GUEST_SESSION_TTL_MINUTES, CART_GUEST_SESSION_TTL_DAYS))
            sessionRepository.save(session);
    }

    public Session getById(String sessionId) {
        return sessionRepository.getActiveById(sessionId).orElseThrow(() -> new ResourceNotFoundException("Session expired or not found"));
    }

    public void save(Session session) {
        sessionRepository.save(session);
    }

    @Transactional
    public void revokeSessions(List<Session> expiredSessions) {

        for (Session session : expiredSessions) {
            session.revoke();
        }

        sessionRepository.saveAll(expiredSessions);

    }

    public List<Session> getExpiredSessions() {
        return sessionRepository.getExpired();
    }

    public Session createGuestSession(ClientType clientType) {

        Session session = new Session();
        session.setSessionId(generateSessionId());
        session.setClientType(clientType);
        session.markAsGuestWithoutCart(LOW_PRIORITY_GUEST_SESSION_TTL_MINUTES);

        return sessionRepository.save(session);

    }

//    public Session fetchByIdWithLocNullable(String sessionId) {
//        return sessionRepository.findBySessionId(sessionId).orElse(null);
//    }

    public Session AuthToGuestSession(Session session) {

        if (session.getIsGuest()) {
            throw new InvalidSessionException("Session is already a guest session");
        }

        session.markAsGuestWithoutCart(LOW_PRIORITY_GUEST_SESSION_TTL_MINUTES);

        return sessionRepository.save(session);
    }

    public Session getRequestSession() {
        return SessionExtractor.getRequestSession()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Session not found. SessionFilter should guarantee session presence."));
    }

    public static long calculateSessionTTLSeconds(Instant expiresAt) {
        return ChronoUnit.SECONDS.between(Instant.now(), expiresAt);
    }

    public Optional<Session> getByIdOptional(String sessionId) {
        return sessionRepository.findById(sessionId);
    }


    public ReportResponses.ReportResponse getActiveSessions() {

        Instant now = Instant.now();
        Instant fourMinutesAgo = now.minus(4, ChronoUnit.MINUTES);
        Instant truncated = now.truncatedTo(ChronoUnit.MINUTES);

        SessionActivityProjection projection = sessionRepository.getActiveSessionCount(fourMinutesAgo);

        List<Map<String, String>> rows = getMapList(projection);

//        ReportResponses.ReportResponse response = ReportResponses.buildTableReport("Потребителска активност към " + truncated,
//                List.of("Общо", "Потребители", "Гост потребители"),
//                rows);

//        List<ReportResponses.MetricDto> metrics = new ArrayList<>();
//
//        if (!rows.isEmpty()) {
//            for (Map.Entry<String, String> entry : rows.getFirst().entrySet()) {
//                String key = entry.getKey();
//                String value = entry.getValue();
//
//                metrics.add(new ReportResponses.MetricDto(key, value));
//            }
//        }

        List<Map<String, String>> chartData = buildChartDataMapList(projection);

        ReportResponses.ChartDto chartDto = ReportResponses.buildBarChart("sessionType", "count", "Брой потребители", chartData);

        ReportResponses.ReportResponse response = ReportResponses.buildMixedReport("Потребителска активност към " + truncated,
                null,
                chartDto,
                List.of("Общо", "Потребители", "Гост потребители"),
                rows);

        System.out.println("response: " + response);

        return response;
    }

    @NotNull
    private static List<Map<String, String>> getMapList(SessionActivityProjection projection) {
        String total = projection.getTotal() != null ? projection.getTotal().toString() : "0";
        String auth = projection.getAuth() != null ? projection.getAuth().toString() : "0";
        String guest = projection.getGuest() != null ? projection.getGuest().toString() : "0";

        Map<String, String> totalMap = Map.of("Общо", total, "Потребители", auth, "Гост потребители", guest);

        return List.of(totalMap);
    }

    @NotNull
    private static List<Map<String, String>> buildChartDataMapList(SessionActivityProjection projection) {
        String total = projection.getTotal() != null ? projection.getTotal().toString() : "0";
        String auth = projection.getAuth() != null ? projection.getAuth().toString() : "0";
        String guest = projection.getGuest() != null ? projection.getGuest().toString() : "0";

        Map<String, String> totalMap = Map.of("sessionType", "Общо", "count", total);
        Map<String, String> authMap = Map.of("sessionType", "Потребители", "count", auth);
        Map<String, String> guestMap = Map.of("sessionType", "Гост потребители", "count", guest);

        return List.of(totalMap, authMap, guestMap);
    }
}

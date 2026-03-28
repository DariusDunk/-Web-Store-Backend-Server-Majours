package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.DTOs.responses.KeycloakTokenResponse;
import com.example.ecomerseapplication.DTOs.responses.LoginResponse;
import com.example.ecomerseapplication.DTOs.responses.TokenRefreshResponse;
import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidSessionException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.LoginFailedException;
import com.example.ecomerseapplication.Mappers.LoginResponseMapper;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final KeycloakService keycloakService;
    private final CustomerService customerService;
    private final SessionService sessionService;
    private final ClientTypeService clientTypeService;

    public AuthService(KeycloakService keycloakService, CustomerService customerService, SessionService sessionService, ClientTypeService clientTypeService) {
        this.keycloakService = keycloakService;
        this.customerService = customerService;
        this.sessionService = sessionService;
        this.clientTypeService = clientTypeService;
    }

    private String extractIdFromToken(String token) throws ParseException {
        SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
        return signedJWT.getJWTClaimsSet().getSubject();
    }

    @Transactional
    public LoginResponse login(UserLoginRequest request) throws ParseException {
        KeycloakTokenResponse tokenResponse = null;
        try {
            tokenResponse = keycloakService.loginUser(request);
            String userId = extractIdFromToken(tokenResponse.accessToken());
            Customer customer = customerService.getById(userId);
            ClientType clientType = clientTypeService.getByTypeName(request.clientType());

            Session session = sessionService.createSession(tokenResponse.refreshToken(), customer, clientType, request.rememberMe());

            return LoginResponseMapper.fromKeycloakResponseAndSession(tokenResponse, session);
        } catch (Exception e) {
            if (tokenResponse != null) {
                System.out.println();
                keycloakService.invalidateRefreshToken(tokenResponse.refreshToken());
                throw new LoginFailedException("Login failed, rolling back session creation: "+ e.getMessage());
            }
            throw new LoginFailedException("Login failed: "+ e.getMessage());
        }
    }

    @Transactional
    public void logout(String refreshToken, String sessionId) {

        Session session = sessionService.getById(sessionId);

        sessionService.revokeSession(session);

        keycloakService.invalidateRefreshToken(refreshToken);
    }

    @Transactional
    public TokenRefreshResponse refresh(String refreshToken, Session session) {//todo tuk da se vry6ta DTO Response sys tokenite i sesiqta
//todo tova sled kato vsi4ko sys sesiite e setupnato
        if (session.getIsRevoked()|| session.isExpired())
            throw new InvalidSessionException("Session is revoked or expired");

        try {
            TokenRefreshResponse tokenRefreshResponse = keycloakService.refreshBothTokens(refreshToken);
            Instant newTTL = session.isRememberMeSession()
                    ?Instant.now().plus(tokenRefreshResponse.expiresIn(), ChronoUnit.SECONDS)
                    :Instant.now().plus(GlobalConstants.NORMAL_SESSION_TTL_HOURS, ChronoUnit.HOURS);
            session.setExpiresAt(newTTL);

            return tokenRefreshResponse;
        }
        catch (Exception e)
        {
            throw new InvalidSessionException("Session/token refresh failed: "+ e.getMessage());
        }
    }

//    @Transactional
//    public TokenRefreshResponse tokensOfSession(String sessionId) {
//
//        Session session = sessionService.getById(sessionId);
//
//        if (session.getIsRevoked()|| session.isExpired())
//            throw new InvalidSessionException("Session is revoked or expired");
//
//        return refresh(session.getRefreshToken(), session);
//    }

}

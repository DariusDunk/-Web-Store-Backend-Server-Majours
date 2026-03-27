package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.DTOs.responses.KeycloakTokenResponse;
import com.example.ecomerseapplication.DTOs.responses.LoginResponse;
import com.example.ecomerseapplication.DTOs.responses.TokenRefreshResponse;
import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidSessionException;
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

        KeycloakTokenResponse tokenResponse = keycloakService.loginUser(request); //todo tova sled kato vsi4ko sys sesiite e setupnato
        String userId = extractIdFromToken(tokenResponse.accessToken());
        Customer customer = customerService.getById(userId);
        ClientType clientType = clientTypeService.getByTypeName("web");//todo smeni ot zaqvkata
        Session session = sessionService.createSession(tokenResponse.refreshToken(), customer, clientType, false);// todo remember me go vzemi ot zaqvkata

        return LoginResponseMapper.fromKeycloakResponseAndSession(tokenResponse, session);
    }

    @Transactional
    public void logout(String refreshToken, Session session) {
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

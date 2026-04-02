package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.ClientType;
import com.example.ecomerseapplication.Entities.Customer;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidSessionException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.LoginFailedException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.RegistrationFailedException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.UserAlreadyExistsException;
import com.example.ecomerseapplication.Mappers.LoginResponseMapper;
import com.example.ecomerseapplication.Mappers.RefreshResponseMapper;
import com.example.ecomerseapplication.Others.GlobalConstants;
import com.example.ecomerseapplication.enums.UserRole;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Duration;
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

    public void register(String firstname, String lastName, String password, String email, UserRole userRole) {
        String userId = null;
        try {
            userId = keycloakService.registerUser(firstname, lastName, password, email, userRole);

            if (userId != null)
            {
                Customer customer = new Customer(userId, firstname, lastName, email);

                customerService.save(customer);
            }

        } catch (Exception e) {

            System.out.println("Error registering user: " + e.getMessage());
            if (userId != null) {
                try {
                    System.out.println("Rolling back user creation");
                    keycloakService.deleteUser(userId);
                } catch (Exception ex) {
                    System.out.println("Error rollback deleting user: " + ex.getMessage());
                }
            }

            if (e instanceof ValidationException
                    || e instanceof UserAlreadyExistsException
                    || e instanceof RegistrationFailedException) {
                throw e;

            }
            throw new RegistrationFailedException("Registration failed");
        }
    }

    private String extractIdFromToken(String token) throws ParseException {
        SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
        return signedJWT.getJWTClaimsSet().getSubject();
    }

    @Transactional
    public LoginResponse login(UserLoginRequest request) {
        KeycloakTokenResponse tokenResponse = null;
        try {
            tokenResponse = keycloakService.loginUser(request);
            String userId = extractIdFromToken(tokenResponse.accessToken());
            Customer customer = customerService.getById(userId);
            ClientType clientType = clientTypeService.getByTypeName(request.clientType());
            Session session = sessionService.createSession(tokenResponse.refreshToken(), customer, clientType, request.rememberMe(), tokenResponse.refreshExpiresIn());
            
            return LoginResponseMapper.fromKeycloakResponseAndSession(tokenResponse, session);
        } catch (Exception e) {
            if (tokenResponse != null) {
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
    public RefreshResponse refresh(String refreshToken, Session session) {
        if (session.getIsRevoked()|| session.isExpired())
            throw new InvalidSessionException("Session is revoked or expired");

        try {

            TokenRefreshResponse tokenRefreshResponse = keycloakService.refreshBothTokens(refreshToken);
            Instant newTTL = session.isRememberMeSession()
                    ?Instant.now().plus(tokenRefreshResponse.refreshExpiresIn(), ChronoUnit.SECONDS)
                    :Instant.now().plus(GlobalConstants.NORMAL_SESSION_TTL_HOURS, ChronoUnit.HOURS);
            session.setExpiresAt(newTTL);
            session.setRefreshToken(tokenRefreshResponse.refreshToken());

            sessionService.save(session);

//            System.out.println("New session TTL: " + newTTL);

            return RefreshResponseMapper.tokenRefreshToRefreshResponse(tokenRefreshResponse, newTTL);
        }
        catch (Exception e)
        {
            throw new InvalidSessionException("Session/token refresh failed: "+ e.getMessage());
        }
    }

    public GuestSessionResponse createGuest(String clientTypeName) {

        ClientType clientType = clientTypeService.getByTypeName(clientTypeName);

        Session session = sessionService.createGuestSession(clientType);

        return new GuestSessionResponse(session.getSessionId(),
                Duration.between(Instant.now(), session.getExpiresAt()).getSeconds());
    }
}

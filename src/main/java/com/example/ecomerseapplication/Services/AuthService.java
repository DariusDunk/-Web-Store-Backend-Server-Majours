package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Auth.helpers.SessionExtractor;
import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.Cart;
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
import org.springframework.beans.factory.annotation.Autowired;
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
    private final CartService cartService;
    private final CartProductService cartProductService;

    @Autowired
    public AuthService(KeycloakService keycloakService, CustomerService customerService, SessionService sessionService, ClientTypeService clientTypeService, CartService cartService, CartProductService cartProductService) {
        this.keycloakService = keycloakService;
        this.customerService = customerService;
        this.sessionService = sessionService;
        this.clientTypeService = clientTypeService;
        this.cartService = cartService;
        this.cartProductService = cartProductService;
    }

    @Transactional
    public void register(String firstname, String lastName, String password, String email, UserRole userRole) {
        String userId = null;
        try {
            userId = keycloakService.registerUser(firstname, lastName, password, email, userRole);

            if (userId != null) {
                Customer customer = new Customer(userId, firstname, lastName, email);
                customer = customerService.save(customer);

                cartService.save(new Cart(customer));
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
            Session session;
            String sessionId = SessionExtractor.getRequestSessionId();

            if (sessionId != null) {
                session = sessionService.getById(sessionId);
                session = sessionService.guestToLoginSession(session, tokenResponse, request.rememberMe(), customer);
                if (cartService.existsBySession(session)) {
//                    cartService.mergeCarts(session, customer);
                    cartProductService.mergeCarts(session, customer);
                }
            } else {
                session = sessionService.createAuthenticatedSession(tokenResponse.refreshToken(), customer, clientType, request.rememberMe(), tokenResponse.refreshExpiresIn());
            }

            return LoginResponseMapper.fromKeycloakResponseAndSession(tokenResponse, session);
        } catch (Exception e) {
            if (tokenResponse != null) {
                keycloakService.invalidateRefreshToken(tokenResponse.refreshToken());
                throw new LoginFailedException("Login failed, rolling back session creation: " + e.getMessage());
            }
            throw new LoginFailedException("Login failed: " + e.getMessage());
        }
    }

    @Transactional
    public GuestSessionResponse logout(String refreshToken, String sessionId) {

//        Session session = sessionService.getById(sessionId);

//        sessionService.revokeSession(session);

        Session session = sessionService.LoginToGuestSession(sessionId);
        try {
            keycloakService.invalidateRefreshToken(refreshToken);
        } catch (Exception e) {
            System.out.println("-------------------------------------------------------");
            System.out.println("Error invalidating refresh token in keycloak: " + e.getMessage());
            System.out.println("-------------------------------------------------------");
        }

        return new GuestSessionResponse(session.getSessionId(),
                Duration.between(Instant.now(), session.getExpiresAt()).getSeconds());
    }

    @Transactional
    public RefreshResponse refresh(String refreshToken, Session session) {
        if (session.getIsRevoked() || session.isExpired())
            session = sessionService.createGuestSession(session.getClientType());

        TokenRefreshResponse tokenRefreshResponse;

        try {
            tokenRefreshResponse = keycloakService.refreshBothTokens(refreshToken);
        } catch (Exception e) {
            System.out.println("Error refreshing token in keycloak, session will be treated as a guest: \n" + e.getMessage() + "\n -------------------------------------------------------");

            session.setIsGuest(true);
            session.setRefreshToken(null);
            session.setExpiresAt(Instant.now().plus(GlobalConstants.GUEST_SESSION_TTL_DAYS, ChronoUnit.DAYS));
            session.setLastActivityAt(Instant.now());
            session.setCustomer(null);
            session.setIsRememberMeSession(false);
            session.setIsRevoked(false);

            sessionService.save(session);

            return new RefreshResponse(null,
                    0,
                    0,
                    null,
                    Duration.between(Instant.now(), session.getExpiresAt()).getSeconds(),
                    true,
                    false,
                    session.getSessionId());
        }

        try {

            Instant newTTL = session.getIsRememberMeSession()
                    ? Instant.now().plus(tokenRefreshResponse.refreshExpiresIn(), ChronoUnit.SECONDS)
                    : Instant.now().plus(GlobalConstants.NORMAL_SESSION_TTL_HOURS, ChronoUnit.HOURS);
            session.setExpiresAt(newTTL);
            session.setRefreshToken(tokenRefreshResponse.refreshToken());

            sessionService.save(session);

            return RefreshResponseMapper.tokenRefreshToRefreshResponse(tokenRefreshResponse,
                    newTTL,
                    false,
                    session.getIsRememberMeSession(),
                    session.getSessionId());
        } catch (Exception e) {
            throw new InvalidSessionException("Session persist failed: " + e.getMessage());
        }
    }

    public GuestSessionResponse createGuest(String clientTypeName) {

        ClientType clientType = clientTypeService.getByTypeName(clientTypeName);

        Session session = sessionService.createGuestSession(clientType);

        return new GuestSessionResponse(session.getSessionId(),
                Duration.between(Instant.now(), session.getExpiresAt()).getSeconds());
    }
}

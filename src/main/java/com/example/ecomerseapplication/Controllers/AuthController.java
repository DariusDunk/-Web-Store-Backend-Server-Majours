package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.CustomerAccountRequest;
import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Services.AuthService;
import com.example.ecomerseapplication.Services.KeycloakService;
import com.example.ecomerseapplication.Services.SessionService;
import com.example.ecomerseapplication.enums.UserRole;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("auth/")
public class AuthController {

    private final KeycloakService keycloakService;
    private final SessionService sessionService;
    private final AuthService authService;

    @Autowired
    public AuthController(KeycloakService keycloakService, SessionService sessionService, AuthService authService) {
        this.keycloakService = keycloakService;
        this.sessionService = sessionService;
        this.authService = authService;
    }

    @GetMapping("refresh/{token}/{sessionId}")
    public ResponseEntity<?> refreshTokens(@PathVariable("token") String refreshToken
            , @PathVariable String sessionId) {
        try {
            Session session = sessionService.getById(sessionId);

            return ResponseEntity.ok(authService.refresh(refreshToken,session));
        } catch (Exception e) {
            System.out.println("Error refreshing tokens: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PostMapping("register")
    public ResponseEntity<?> registerUserKeycloak(@RequestBody @Valid CustomerAccountRequest customerAccountRequest) {//TODO ZAPISVANETO V BAZATA TRQBVA DA SE KRIPTIRA!!!

//        System.out.println("Register request: "+customerAccountRequest);

        keycloakService.registerFlow(customerAccountRequest.firstName,// todo sloji tova vyv auth servica
                customerAccountRequest.familyName,
                customerAccountRequest.password,
                customerAccountRequest.email,
                UserRole.CUSTOMER);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PostMapping("login")
    public ResponseEntity<?> loginUserKeycloak(@RequestBody @Valid UserLoginRequest request) {//todo trqbva refresh tokena v tablicata na sesiqta da se kriptira
        return ResponseEntity.ok(authService.login(request));

    }

    @GetMapping("invalidate/{token}/{sessionId}")
    public ResponseEntity<?> invalidateToken(@PathVariable("token") String refreshToken, @PathVariable String sessionId
    ) {
        try {

            authService.logout(refreshToken, sessionId);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("Error invalidating token or session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("tokens/{sessionId}")
    public ResponseEntity<?> getTokensOfSession(@PathVariable String sessionId) {

        Session session = sessionService.getById(sessionId);

        return ResponseEntity.ok(authService.refresh(session.getRefreshToken(), session));
    }
}

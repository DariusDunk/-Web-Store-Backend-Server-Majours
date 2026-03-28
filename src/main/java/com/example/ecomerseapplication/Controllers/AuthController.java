package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.CustomerAccountRequest;
import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.Entities.Session;
import com.example.ecomerseapplication.Services.AuthService;
import com.example.ecomerseapplication.Services.CustomerService;
import com.example.ecomerseapplication.Services.KeycloakService;
import com.example.ecomerseapplication.Services.SessionService;
import com.example.ecomerseapplication.enums.UserRole;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@Validated
@RequestMapping("auth/")
public class AuthController {

    private final KeycloakService keycloakService;
    private final SessionService sessionService;
    private final CustomerService customerService;
    private final AuthService authService;

    @Autowired
    public AuthController(KeycloakService keycloakService, SessionService sessionService, CustomerService customerService, AuthService authService) {
        this.keycloakService = keycloakService;
        this.sessionService = sessionService;
        this.customerService = customerService;
        this.authService = authService;
    }

    @GetMapping("refresh/{token}"
//                        +"/{sessionId}"
    )
    public ResponseEntity<?> refreshTokens(@PathVariable("token") String refreshToken
//                                               , @PathVariable("sessionId") String sessionId,
//                                           @RequestParam("rememberMe") boolean rememberMe
    ) {

        try {

            return ResponseEntity.ok(keycloakService.refreshBothTokens(refreshToken));

            //todo tova sled kato vsi4ko sys sesiite e setupnato

//            Session session = sessionService.getById(sessionId);
//
//            return ResponseEntity.ok(authService.refresh(refreshToken,session,rememberMe));
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
//
//    private String extractIdFromToken(String token) throws ParseException {
//        SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
//        return signedJWT.getJWTClaimsSet().getSubject();
//    }

    @PostMapping("login")//TODO dobavi kym requesta i tipa na ustroistvoto, toi 6te se zadava ot syotvetniq BFF
    public ResponseEntity<?> loginUserKeycloak(@RequestBody @Valid UserLoginRequest request) throws ParseException {//todo wrapper dto za session-a ili po-dobre napravi nov response, koito da ima samo vajnite ne6ta ot keycloak response-a, zaedno sys sesiqta

        return ResponseEntity.ok(authService.login(request));

//        return ResponseEntity.ok(keycloakService.loginUser(request));
    }

    @GetMapping("invalidate/{token}"
            +"/{sessionId}"
    )
    public ResponseEntity<?> invalidateToken(@PathVariable("token") String refreshToken
    , @PathVariable String sessionId
    ) {//TODO tuyk da se invalidira i sesiqta
        try {

//            Session session = sessionService.getById(sessionId); //todo tova sled kato vsi4ko sys sesiite e setupnato
//
//            sessionService.logout(session, refreshToken);

            authService.logout(refreshToken, sessionId);

            return ResponseEntity.noContent().build();

//            return ResponseEntity.status(HttpStatus.valueOf(keycloakService.invalidateRefreshToken(refreshToken))).build();
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

package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.CustomerAccountRequest;
import com.example.ecomerseapplication.DTOs.requests.RefreshTokenRequest;
import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.Services.KeycloakService;
import com.example.ecomerseapplication.Utils.NullFieldChecker;
import com.example.ecomerseapplication.enums.UserRole;
import org.keycloak.common.VerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth/")
public class AuthController {

    private final KeycloakService keycloakService;

    @Autowired
    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }


//    @PostMapping("create")
//    public ResponseEntity<?> createSession() {
//        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
//    }

    @PostMapping("refresh")
    public ResponseEntity<?> refreshTokens(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try
        {
            System.out.println("Refresh token: "+ refreshTokenRequest.refreshToken());
            return keycloakService.refreshBothTokens(refreshTokenRequest.refreshToken());
        }
        catch (Exception e)
        {
            System.out.println("Error refreshing tokens: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PostMapping("register")
    @Transactional
    public ResponseEntity<?> registerUserKeycloak(@RequestBody CustomerAccountRequest customerAccountRequest) {

        if (NullFieldChecker.hasNullFields(customerAccountRequest)) {
            System.out.println("Null fields from request: "+ NullFieldChecker.getNullFields(customerAccountRequest));
            return ResponseEntity.badRequest().build();
        }


        System.out.println("Registering user: " + customerAccountRequest);

        try {
            return  keycloakService.registerUser(customerAccountRequest.firstName,
                    customerAccountRequest.familyName,
                    customerAccountRequest.password,
                    customerAccountRequest.email,
                    UserRole.CUSTOMER);
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping("login")
    public ResponseEntity<?> loginUserKeycloak(@RequestBody UserLoginRequest request) throws VerificationException {

        if (request.identifier() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        return keycloakService.loginUser(request);
    }

    @PostMapping("invalidate")
    public ResponseEntity<?> invalidateToken(@RequestBody RefreshTokenRequest refreshTokenRequest)
    {

        try
        {
//            System.out.println("Invalidating token: " + refreshTokenRequest.refreshToken());
            return ResponseEntity.status(HttpStatus.valueOf(keycloakService.invalidateRefreshToken(refreshTokenRequest.refreshToken()))).build();
        }
        catch (Exception e)
        {
            System.out.println("Error invalidating token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}

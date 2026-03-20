package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.CustomerAccountRequest;
import com.example.ecomerseapplication.DTOs.requests.RefreshTokenRequest;
import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.Services.KeycloakService;
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

    @Autowired
    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @PostMapping("refresh")
    public ResponseEntity<?> refreshTokens(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {//TODO naprvi da ne e requestBody

        try {
            return ResponseEntity.ok(keycloakService.refreshBothTokens(refreshTokenRequest.refreshToken()));
        } catch (Exception e) {
            System.out.println("Error refreshing tokens: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @PostMapping("register")
    public ResponseEntity<?> registerUserKeycloak(@RequestBody @Valid CustomerAccountRequest customerAccountRequest) {//TODO ZAPISVANETO V BAZATA TRQBVA DA SE KRIPTIRA!!!

        System.out.println("Register request: "+customerAccountRequest);

        keycloakService.registerFlow(customerAccountRequest.firstName,
                customerAccountRequest.familyName,
                customerAccountRequest.password,
                customerAccountRequest.email,
                UserRole.CUSTOMER);

        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    @PostMapping("login")
    public ResponseEntity<?> loginUserKeycloak(@RequestBody @Valid UserLoginRequest request) {

        return ResponseEntity.ok(keycloakService.loginUser(request));
    }

    @PostMapping("invalidate")
    public ResponseEntity<?> invalidateToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest)//TODO naprvi da ne e requestBody
    {
        try {
            return ResponseEntity.status(HttpStatus.valueOf(keycloakService.invalidateRefreshToken(refreshTokenRequest.refreshToken()))).build();
        } catch (Exception e) {
            System.out.println("Error invalidating token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}

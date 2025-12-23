package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.DTOs.requests.RefreshTokenRequest;
import com.example.ecomerseapplication.Services.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    @PostMapping("create")
    public ResponseEntity<?> createSession() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

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

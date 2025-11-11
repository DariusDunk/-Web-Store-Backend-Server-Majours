package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.DTOs.responses.LoginResponse;
import com.example.ecomerseapplication.DTOs.responses.TokenResponse;
import com.example.ecomerseapplication.enums.UserRole;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class KeycloakService {

    private Keycloak keycloak;

    @Value("${keycloak.server-url}")
    private String serverUrl;
    @Value("${keycloak.admin-realm}")
    private String adminRealm;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.admin-username}")
    private String adminUsername;
    @Value("${keycloak.admin-password}")
    private String adminPassword;
    @Value("${keycloak.user-realm}")
    private String userRealm;
    @Value("${keycloak.token-target-address}")
    private String tokenAddress;
    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String secret;

    @PostConstruct
    public void init() {
        //login kato admin za da moje da se syzdavat potrebiteli
        keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(adminRealm)
                .clientId(clientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    public void registerUser(String username, String password, String email, UserRole userRole) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setEnabled(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setTemporary(false);
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        user.setCredentials(Collections.singletonList(cred));

        //Syzdavane na potrebitel v potrebitelskiq realm
        var response = keycloak.realm(userRealm).users().create(user);

        //Pri polu4avane na rezultat se polu4ava celiq url za potrebitelq i vsi4ko do poslednata 4ast, koqto e potrebitelskoto id
        if (response.getStatus() == org.apache.http.HttpStatus.SC_CREATED) {
            String userId = response.getLocation()
                    .getPath()
                    .replaceAll(".*/([^/]+)$", "$1");

            //Vzemane na rolqta na dadeniq potrebitel ot keycloak
            RoleRepresentation role = keycloak.realm(userRealm)
                    .roles()
                    .get(userRole.getValue())
                    .toRepresentation();

            //dobavqne na izbraniq potrebitel kym spisyka s izbranata rolq. Demek na potrebitelq mu se dava izbranata rolq
            keycloak.realm(userRealm)
                    .users()
                    .get(userId)
                    .roles()
                    .realmLevel()
                    .add(Collections.singletonList(role));
        } else {
            //pri neuspe6na registraciq
            throw new RuntimeException("Неуспешна регистрация в Keycloak: " + response.getStatusInfo());
        }
    }



    public String getUserIdFromToken(String token) throws VerificationException {
        AccessToken accessToken = TokenVerifier.create(token, AccessToken.class).getToken();
        return accessToken.getSubject();
    }

    public List<UserRepresentation> getAllUsersOfRole(String roleName) {
        return keycloak.realm(userRealm).roles().get(roleName).getUserMembers();
    }

    public String getRoleByUserId(String userId) {
        return keycloak.realm(userRealm).users().get(userId).roles().realmLevel().listAll().getFirst().getName();
    }

    public UserRepresentation getUserIdByEmail(String username) {
        return keycloak.realm(userRealm).users().searchByEmail(username, true).getFirst();
    }

    public ResponseEntity<?> loginUser(UserLoginRequest request) throws VerificationException {

        MultivaluedMap<String, String> formParams = new MultivaluedHashMap<>();
        formParams.add("grant_type", "password");
        formParams.add("client_id", "backend-service");
        formParams.add("client_secret", secret);
        formParams.add("username", request.identifier());
        formParams.add("password", request.password());

        Response response = ClientBuilder.newClient()
                .target(tokenAddress)
                .request()
                .post(Entity.form(new Form(formParams)));

        if (response.getStatus() == HttpStatus.SC_OK) {

            TokenResponse tokenResponse = response.readEntity(TokenResponse.class);

            return ResponseEntity.ok(
                    new LoginResponse(request.identifier(),
                    getRoleByUserId(getUserIdFromToken(tokenResponse.accessToken())), tokenResponse)
            );

        }

        return ResponseEntity.status(response.getStatus()).build();

    }
}

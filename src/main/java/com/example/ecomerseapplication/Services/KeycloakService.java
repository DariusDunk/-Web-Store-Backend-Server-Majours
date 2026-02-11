package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.DTOs.responses.KeycloakTokenResponse;
import com.example.ecomerseapplication.DTOs.responses.TokenRefreshResponse;
import com.example.ecomerseapplication.enums.UserRole;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class KeycloakService {// TODO Rewrite the Keycloak client using Spring WebClient after migration

//    private final CustomerService customerService;
    private final WebClient keycloakWebClient;

    @Value("${keycloak.server-url}")
    private String serverUrl;
    @Value("${keycloak.admin-realm}")
    private String adminRealm;
    @Value("${keycloak.admin.client-id}")
    private String adminCliendId;
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
    @Value("${keycloak.ream.name}")
    private String realmName;
    @Value("${keycloak.regular-client-id}")
    private String regularClientId;

    @Autowired
    public KeycloakService(
//            CustomerService customerService,
                           WebClient keycloakWebClient
    ) {
//        this.customerService = customerService;
        this.keycloakWebClient = keycloakWebClient;
    }


    private String getAdminAccessToken() {
//        System.out.println("Getting Admin Access Token");
        return Objects.requireNonNull(keycloakWebClient.post()
                        .uri("/realms/" + adminRealm + "/protocol/openid-connect/token")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData("grant_type", "password")
                                .with("client_id", adminCliendId)
                                .with("username", adminUsername)
                                .with("password", adminPassword))
                        .retrieve()
                        .bodyToMono(KeycloakTokenResponse.class)
                        .block())
                .accessToken();
    }


    private Boolean doesUserExist(String email, String adminToken) {

//        System.out.println("User check");

        UserRepresentation[] existingUsers = keycloakWebClient.get()
                .uri("/admin/realms/" + userRealm + "/users?username={username}", email)
                .headers(h -> h.setBearerAuth(adminToken))
                .retrieve()
                .bodyToMono(UserRepresentation[].class)
                .block();

        return existingUsers != null && existingUsers.length > 0;
    }

    private ResponseEntity<String > createUser(UserRepresentation user, String adminToken) {
        String userId;
//        System.out.println("Creating user");

        try {
           ResponseEntity<?> response = keycloakWebClient.post()
                    .uri("/admin/realms/" + userRealm + "/users")
                    .headers(h -> h.setBearerAuth(adminToken))
                    .bodyValue(user)
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().is2xxSuccessful()) {
                            return Mono.just(ResponseEntity.status(clientResponse.statusCode())
                                    .headers(clientResponse.headers().asHttpHeaders())
                                    .build());
                        }
                        else {
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Failed to create user in Keycloak: " + body
                                    )));
                        }
                    })
                    .block();

           if (response != null&& response.getHeaders().getLocation() != null) {
               String location = response.getHeaders().getLocation().toString();
               userId = location.substring(location.lastIndexOf("/") + 1);
//               System.out.println("Created User with Id: " + userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(userId);
           }
           else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            throw new RuntimeException("Failed to create user in Keycloak: " + responseBody, e);
        }

    }

    private RoleRepresentation getRealmRole(String roleName, String adminToken) {
        return keycloakWebClient.get()
                .uri("/admin/realms/{realm}/roles/{roleName}", userRealm, roleName)
                .headers(h -> h.setBearerAuth(adminToken))
                .retrieve()
                .bodyToMono(RoleRepresentation.class)
                .block();
    }


    private ResponseEntity<?> assignUserRole(String userId, UserRole userRole, String adminToken) {
//        System.out.println("Assigning user role: " + userRole);
        RoleRepresentation role = getRealmRole(userRole.getValue(), adminToken);

    try
        {
            keycloakWebClient.post()
                    .uri("/admin/realms/" + userRealm + "/users/{id}/role-mappings/realm", userId)
                    .headers(h -> h.setBearerAuth(adminToken))
                    .bodyValue(Collections.singletonList(role))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return ResponseEntity.status(HttpStatus.CREATED).body(role);
        }
    catch (WebClientResponseException e) {
        String responseBody = e.getResponseBodyAsString();
        deleteUser(userId, adminToken);
        throw new RuntimeException("Failed to create user in Keycloak: " + responseBody, e);
    }

    }

    private void deleteUser(String userId, String adminToken) {
//        String token = getAdminAccessToken();
//        System.out.println("Deleting user");
        keycloakWebClient.delete()
                .uri("/admin/realms/" + userRealm + "/users/{id}", userId)
                .headers(h -> h.setBearerAuth(adminToken))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<?> registerUser(String firstname,String lastName , String password, String email, UserRole userRole) {

//        System.out.println("Registration begining");

        String adminToken = getAdminAccessToken();

//        boolean realmUserCreated = false;
        String userId;

        UserRepresentation user = new UserRepresentation();
        user.setEmail(email);
        user.setUsername(email);
        user.setEnabled(true);
        user.setFirstName(firstname);
        user.setLastName(lastName);

//        System.out.println("User information set");

        if (password.length() < 12) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.VALIDATION_ERROR,
                    "Кратка парола!",
                    HttpStatus.BAD_REQUEST.value(),
                    "Паролата не трябва да е по-кратка от 12 символа"));
        }

        if (Objects.equals(password, email)) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new ErrorResponse(ErrorType.VALIDATION_ERROR,
                    "Имейлът съвпада с паролата!",
                    HttpStatus.BAD_REQUEST.value(),
                    "Имейлът и паролата не трябва да съвпадат!"));
        }

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setTemporary(false);
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        user.setCredentials(Collections.singletonList(cred));
//        System.out.println("Password checked");

        if (doesUserExist(email, adminToken))
        {
//            System.out.println("User already exists");
            return ResponseEntity.status(HttpStatus.MULTI_STATUS)
                    .body(new ErrorResponse(
                            ErrorType.USER_ALREADY_EXISTS,
                            "Съществуващ потребител",
                            HttpStatus.CONFLICT.value(),
                            "Потребител с този имейл адрес вече съществува"
                    ));
        }

        ResponseEntity<String> userCreationResponse = createUser(user, adminToken);

        userId = userCreationResponse.getBody();

        if (userCreationResponse.getStatusCode().is2xxSuccessful()) {
            ResponseEntity<?> roleAssignResponse = assignUserRole(userId, userRole, adminToken);

            if (roleAssignResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(userId);
            }

        }

        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    public List<UserRepresentation> getAllUsersOfRole(String roleName) {//Untested
        String adminToken = getAdminAccessToken();
        return keycloakWebClient.get()
                .uri("/admin/realms/" + userRealm + "/roles/{role}/users", roleName)
                .headers(h -> h.setBearerAuth(adminToken))
                .retrieve()
                .bodyToMono(UserRepresentation[].class)
                .map(Arrays::asList)
                .block();
    }

    public String getRoleByUserId(String userId) {
        String adminToken = getAdminAccessToken();

//        System.out.println("Role: " + role);
        return Objects.requireNonNull(keycloakWebClient.get()
                        .uri("/admin/realms/" + userRealm + "/users/{id}/role-mappings/realm", userId)
                        .headers(h -> h.setBearerAuth(adminToken))
                        .retrieve()
                        .bodyToMono(RoleRepresentation[].class)
                        .map(Arrays::asList)
                        .block())
                .getFirst().getName();
    }

    public ResponseEntity<?> loginUser(UserLoginRequest request) throws VerificationException {

        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("grant_type", "password");
        formParams.add("client_id", regularClientId);
        formParams.add("client_secret", secret);
        formParams.add("username", request.identifier());
        formParams.add("password", request.password());

        //todo vremenno dokato ne zavy6i migraciqta? Kakvo izob6to prave6e tova??
        formParams.add("scope", "openid profile email");
    try
        {
            KeycloakTokenResponse response = Objects.requireNonNull(keycloakWebClient.post()
                    .uri(tokenAddress)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formParams))
                    .retrieve()
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block());

            return ResponseEntity.ok(response);
        }
    catch (WebClientResponseException e) {
        System.out.println("Error logging in user: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    }

    public Integer invalidateRefreshToken(String refreshToken) {

        String logoutUrl = "/realms/" + realmName + "/protocol/openid-connect/logout";

        try {
            MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
            formParams.add("client_id", regularClientId);
            formParams.add("client_secret", secret);
            formParams.add("refresh_token", refreshToken);

           ResponseEntity<?> response = keycloakWebClient.post()
                    .uri(logoutUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formParams))
                    .retrieve()
                   .toBodilessEntity()
                    .block();

            assert response != null;
            if (response.getStatusCode().is2xxSuccessful()) {
//                System.out.println("Logout successful");
            }
            return response.getStatusCode().value();

        } catch (WebClientResponseException e) {

            String responseBody = e.getResponseBodyAsString();
            HttpStatusCode status = e.getStatusCode();

            throw new RuntimeException(
                    "Keycloak logout failed. Status: " + status +
                            ", Body: " + responseBody, e
            );
        }

    }

    public ResponseEntity<?> refreshBothTokens(String refreshToken) {
        String refreshUrl = "/realms/" + realmName + "/protocol/openid-connect/token";

//        System.out.println("Refreshing tokens");

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "refresh_token");
            formData.add("client_id", regularClientId);
            formData.add("client_secret", secret);
            formData.add("refresh_token", refreshToken);

            TokenRefreshResponse response = keycloakWebClient.post()
                    .uri(refreshUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(TokenRefreshResponse.class)
                    .block();

//            System.out.println("Response: " + response);

            return ResponseEntity.ok(response);

        } catch (WebClientResponseException e) {
//            System.out.println("Failed to refresh tokens:");
            throw new RuntimeException("Failed to refresh Keycloak token: " + e.getMessage(), e);
        }
    }
}

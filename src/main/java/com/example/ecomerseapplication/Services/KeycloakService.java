package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.DTOs.responses.KeycloakTokenResponse;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.DTOs.responses.TokenRefreshResponse;
import com.example.ecomerseapplication.enums.UserRole;
//import jakarta.annotation.PostConstruct;
//import jakarta.ws.rs.client.Client;
//import jakarta.ws.rs.client.ClientBuilder;
//import jakarta.ws.rs.client.Entity;
//import jakarta.ws.rs.core.*;
//import org.apache.http.HttpStatus;
import org.springframework.http.HttpStatus;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class KeycloakService {// TODO Rewrite the Keycloak client using Spring WebClient after migration

    private final CustomerService customerService;
    private final WebClient keycloakWebClient;
    private Keycloak keycloak;

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
    public KeycloakService(CustomerService customerService, WebClient keycloakWebClient) {
        this.customerService = customerService;
        this.keycloakWebClient = keycloakWebClient;
    }

//    @PostConstruct
//    public void init() {
//        //login kato admin za da moje da se syzdavat potrebiteli
////        keycloak = KeycloakBuilder.builder()
////                .serverUrl(serverUrl)
////                .realm(adminRealm)
////                .clientId(adminCliendId)
////                .username(adminUsername)
////                .password(adminPassword)
////                .build();
//
//
//    }

    private String getAdminAccessToken() {
        System.out.println("Getting Admin Access Token");
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

        System.out.println("User check");

        UserRepresentation[] existingUsers = keycloakWebClient.get()
                .uri("/admin/realms/" + userRealm + "/users?username={username}", email)//todo test
                .headers(h -> h.setBearerAuth(adminToken))
                .retrieve()
                .bodyToMono(UserRepresentation[].class)
                .block();

        return existingUsers != null && existingUsers.length > 0;
    }

    private ResponseEntity<String > createUser(UserRepresentation user, String adminToken) {
        String userId;
        System.out.println("Creating user");

        try {
           ResponseEntity<?> response = keycloakWebClient.post()
                    .uri("/admin/realms/" + userRealm + "/users")//todo test
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
               System.out.println("Created User with Id: " + userId);

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
        System.out.println("Assigning user role: " + userRole);
        RoleRepresentation role = getRealmRole(userRole.getValue(), adminToken);

    try
        {
            keycloakWebClient.post()
                    .uri("/admin/realms/" + userRealm + "/users/{id}/role-mappings/realm", userId)//todo test
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
        System.out.println("Deleting user");
        keycloakWebClient.delete()
                .uri("/admin/realms/" + userRealm + "/users/{id}", userId)//todo test
                .headers(h -> h.setBearerAuth(adminToken))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public ResponseEntity<?> registerUser(String firstname,String lastName , String password, String email, UserRole userRole) {//TODO TEST

        System.out.println("Registration begining");

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
        System.out.println("Password checked");
//        System.out.println("Credentials set");


//        if (!keycloak.realm(userRealm).users().search(email).isEmpty())
//           return ResponseEntity.status(HttpStatus.SC_MULTI_STATUS).body(
//                   new ErrorResponse(ErrorType.USER_ALREADY_EXISTS,
//                           "Съществуващ потребител",
//                           HttpStatus.SC_CONFLICT,
//                           "Потребител с този имейл адрес вече съществува")
//           );
        if (doesUserExist(email, adminToken))
        {
            System.out.println("User already exists");
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

//            realmUserCreated = true;

            if (roleAssignResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(userId);
            }

        }

        //Syzdavane na potrebitel v potrebitelskiq realm
//      try ( var response = keycloak.realm(userRealm).users().create(user))
//      {
//          //Pri polu4avane na rezultat se polu4ava celiq url za potrebitelq i vsi4ko do poslednata 4ast, koqto e potrebitelskoto id
//          if (response.getStatus() == org.apache.http.HttpStatus.SC_CREATED) {
//              userId = response.getLocation()
//                      .getPath()
//                      .replaceAll(".*/([^/]+)$", "$1");
//
//              realmUserCreated = true;
////              System.out.println("User created successfully");
//
//              //Vzemane na rolqta na dadeniq potrebitel ot keycloak
//              RoleRepresentation role = keycloak.realm(userRealm)
//                      .roles()
//                      .get(userRole.getValue())
//                      .toRepresentation();
//
//              //dobavqne na izbraniq potrebitel kym spisyka s izbranata rolq. Demek na potrebitelq mu se dava izbranata rolq
//              keycloak.realm(userRealm)
//                      .users()
//                      .get(userId)
//                      .roles()
//                      .realmLevel()
//                      .add(Collections.singletonList(role));
//
//              customerService.createByRepresentation(user, userId);
////              System.out.println("User saved to database");
//
//              return ResponseEntity.status(HttpStatus.SC_CREATED).build();
//          } else {
//              //pri neuspe6na registraciq
////              System.out.println("response:" + response);
//              String errorBody = response.readEntity(String.class);
//              System.out.println("Keycloak create user failed:");
//              System.out.println("Status: " + response.getStatus());
//              System.out.println("Body: " + errorBody);
//              throw new RuntimeException("Неуспешна регистрация в Keycloak: " + errorBody);
//          }
//      }
//      catch (Exception e) {
//
//          if (realmUserCreated) {
//
//              System.out.println("Keycloak error after creating a user: " + e.getMessage());
//              try {
//                  keycloak.realm(userRealm)
//                          .users()
//                          .get(userId)
//                          .remove();
//              } catch (Exception cleanEx) {
//                  System.out.println("Error cleaning up user: " + cleanEx.getMessage());
//              }
//          }
//
//          else {
//              System.out.println("Keycloak error before creating a user: " + e.getMessage());
//          }
//
//          return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
//      }
        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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

//    public ResponseEntity<?> loginUser(UserLoginRequest request) throws VerificationException {
//
//        MultivaluedMap<String, String> formParams = new MultivaluedHashMap<>();
//        formParams.add("grant_type", "password");
//        formParams.add("client_id", regularClientId);
//        formParams.add("client_secret", secret);
//        formParams.add("username", request.identifier());
//        formParams.add("password", request.password());
//
//        //todo vremenno dokato ne zavy6i migraciqta? Kakvo izob6to prave6e tova??
//        formParams.add("scope", "openid profile email");
//
//       try (Client client = ClientBuilder.newClient();
//            Response response =  client.target(tokenAddress)
//                .request()
//                .post(Entity.form(new Form(formParams)))
//       ) {
//           System.out.println("Response status: "+ response.getStatus());
//           if (response.getStatus() == HttpStatus.SC_OK) {
//
//               KeycloakTokenResponse keycloakTokenResponse = response.readEntity(KeycloakTokenResponse.class);
//
//               return ResponseEntity.ok(new AuthTokenResponse(keycloakTokenResponse.accessToken(),
//                       keycloakTokenResponse.expiresIn(),
//                       keycloakTokenResponse.refreshExpiresIn(),
//                       keycloakTokenResponse.refreshToken()));
//
//           }
//
//           return ResponseEntity.status(response.getStatus()).build();
//       }
//       catch (Exception e) {
//           System.out.println("Error logging in user: " + e.getMessage());
//           return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
//       }
//
//    }

    public Integer invalidateRefreshToken(String refreshToken) {//TODO TEST

//        String logoutUrl = serverUrl + "realms/" + realmName + "/protocol/openid-connect/logout";
        String logoutUrl = "/realms/" + realmName + "/protocol/openid-connect/logout";//todo test

//        System.out.println("URL: " + logoutUrl);

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
                System.out.println("Logout successful");
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
//
//        try (Client client = ClientBuilder.newClient();
//             Response response = client.target(logoutUrl)
//                     .request()
//                     .post(Entity.form(new Form(formParams)))) {
//
//            String responseText = response.readEntity(String.class);
//
//
//            int status = response.getStatus();
////            System.out.println("STATUS: "+ status);
//            if (status >= 200 && status < 300) {
//                System.out.println("Logout successful");
//                return status;
//            }
//            else
//                throw new RuntimeException("Failed to logout from Keycloak: " + responseText);
//
//        }




    }

    public ResponseEntity<?> refreshBothTokens(String refreshToken) {
//        String refreshUrl = serverUrl + "realms/" + realmName + "/protocol/openid-connect/token";
        String refreshUrl = "/realms/" + realmName + "/protocol/openid-connect/token";//todo test

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
                    .bodyToMono(TokenRefreshResponse.class)  // map response to DTO
                    .block(); // <-- convert Mono to synchronous object

            System.out.println("Response: " + response);

            return ResponseEntity.ok(response);

        } catch (WebClientResponseException e) {
            // handle errors, e.g., 400, 401
            throw new RuntimeException("Failed to refresh Keycloak token: " + e.getMessage(), e);
        }
    }
}

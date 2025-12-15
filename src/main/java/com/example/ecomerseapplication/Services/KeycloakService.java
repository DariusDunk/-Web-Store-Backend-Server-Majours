package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.requests.UserLoginRequest;
import com.example.ecomerseapplication.DTOs.responses.ErrorResponse;
import com.example.ecomerseapplication.DTOs.responses.LoginResponse;
import com.example.ecomerseapplication.DTOs.responses.TokenResponse;
import com.example.ecomerseapplication.Others.ErrorType;
import com.example.ecomerseapplication.enums.UserRole;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.*;
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
import java.util.Map;

@Service
public class KeycloakService {

    private final CustomerService customerService;
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

    public KeycloakService(CustomerService customerService) {
        this.customerService = customerService;
    }

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

    public ResponseEntity<?> registerUser(String firstname,String lastName , String password, String email, UserRole userRole) {

        boolean realmUserCreated = false;
        String userId = "";

        UserRepresentation user = new UserRepresentation();
        user.setEmail(email);
        user.setUsername(email);
        user.setEnabled(true);
        user.setFirstName(firstname);
        user.setLastName(lastName);

//        System.out.println("User information set");

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setTemporary(false);
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        user.setCredentials(Collections.singletonList(cred));

//        System.out.println("Credentials set");


        if (!keycloak.realm(userRealm).users().search(email).isEmpty())
           return ResponseEntity.status(HttpStatus.SC_MULTI_STATUS).body(
                   new ErrorResponse(ErrorType.USER_ALREADY_EXISTS,
                           "Съществуващ потребител",
                           HttpStatus.SC_CONFLICT,
                           "Потребител с този имейл адрес вече съществува")
           );

        //Syzdavane na potrebitel v potrebitelskiq realm
      try ( var response = keycloak.realm(userRealm).users().create(user))
      {
//          keycloak.realm(userRealm).users().delete(userId);
          //Pri polu4avane na rezultat se polu4ava celiq url za potrebitelq i vsi4ko do poslednata 4ast, koqto e potrebitelskoto id
          if (response.getStatus() == org.apache.http.HttpStatus.SC_CREATED) {
              userId = response.getLocation()
                      .getPath()
                      .replaceAll(".*/([^/]+)$", "$1");

              realmUserCreated = true;
//              System.out.println("User created successfully");

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

              customerService.createByRepresentation(user, userId);

              return ResponseEntity.status(HttpStatus.SC_CREATED).build();//TODO dobavi logika za dobavqne v bazata
          } else {
              //pri neuspe6na registraciq
//              System.out.println("response:" + response);
              throw new RuntimeException("Неуспешна регистрация в Keycloak: " + response.getStatusInfo());
          }
      }
      catch (Exception e) {

          if (realmUserCreated) {

              System.out.println("Keycloak error after creating a user: " + e.getMessage());
              try {
                  keycloak.realm(userRealm)
                          .users()
                          .get(userId)
                          .remove();
              } catch (Exception cleanEx) {
                  System.out.println("Error cleaning up user: " + cleanEx.getMessage());
              }
          }

          else {
              System.out.println("Keycloak error before creating a user: " + e.getMessage());
          }

          return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
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
        //todo vremenno
        formParams.add("scope", "openid profile email");

       try (Client client = ClientBuilder.newClient();
            Response response =  client.target(tokenAddress)
                .request()
                .post(Entity.form(new Form(formParams)))
       ) {
           if (response.getStatus() == HttpStatus.SC_OK) {

               TokenResponse tokenResponse = response.readEntity(TokenResponse.class);

               Response userInfoResponse = client.target(serverUrl +"/realms/" + userRealm+ "/protocol/openid-connect/userinfo")
                       .request()
                       .header("Authorization", "Bearer " + tokenResponse.accessToken())
                       .get();

//               System.out.println("token: " + tokenResponse.accessToken());
//
//               System.out.println("response: " + userInfoResponse.getStatus());

               if (userInfoResponse.getStatus() == 200) {
                   Map<String, Object> userInfo = userInfoResponse.readEntity(new GenericType<>() {
                   });
                   String userId = (String) userInfo.get("sub");
                   String firstName = (String) userInfo.get("given_name");
                   String lastName = (String) userInfo.get("family_name");
                   Long oldId = customerService.getByKId(userId);
//                   System.out.println("ID: " + userId);
//                   System.out.println("oldId: " + oldId);

                   return ResponseEntity.ok(
                           new LoginResponse(firstName+ " " + lastName,
                                   getRoleByUserId(getUserIdFromToken(tokenResponse.accessToken())), tokenResponse,
                                   oldId)
                   );

               }
           }

           return ResponseEntity.status(response.getStatus()).build();
       }
       catch (Exception e) {
           System.out.println("Error logging in user: " + e.getMessage());
           return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
       }

    }
}

package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.ToString;

@ToString
public class CustomerAccountRequest {
    @NotNull
    @JsonProperty("first_name")
    public String firstName;
    @NotNull
    @JsonProperty("last_name")
    public String familyName;
    @NotNull
    public String email;
    @NotNull
    public String password;
}

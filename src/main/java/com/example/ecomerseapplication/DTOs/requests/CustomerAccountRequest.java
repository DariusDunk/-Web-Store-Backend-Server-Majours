package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.ToString;

@ToString
public class CustomerAccountRequest {
    @NotBlank
    @JsonProperty("first_name")
    public String firstName;
    @NotBlank
    @JsonProperty("last_name")
    public String familyName;
    @NotBlank
    public String email;
    @NotBlank
    public String password;
}

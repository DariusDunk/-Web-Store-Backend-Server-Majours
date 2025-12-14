package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

@ToString
public class CustomerAccountRequest {
    @JsonProperty("first_name")
    public String firstName;
    @JsonProperty("last_name")
    public String familyName;
    public String email;
    public String password;
}

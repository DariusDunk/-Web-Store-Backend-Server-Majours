package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerAccountRequest {
    @JsonProperty("username")
    public String userName;
    public String email;
    public String password;
}

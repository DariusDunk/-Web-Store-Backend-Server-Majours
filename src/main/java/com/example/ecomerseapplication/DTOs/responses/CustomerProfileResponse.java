package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;


public record CustomerProfileResponse(
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String familyName,
        @JsonProperty("email")
        String email,
        @JsonProperty("register_date")
        Instant registerDate,
        @JsonProperty("phone_number")
        String phoneNumber,
        @JsonProperty("delivery_address")
        String deliveryAddress,
        @JsonProperty("favourites_count")
        int favouritesCount,
        @JsonProperty("user_pfp")
        String userPfp,
        @JsonProperty("reviews_count")
        int reviewsCount,
        @JsonProperty("purchases_count")
        int purchasesCount
        ) {
}

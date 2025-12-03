package com.example.ecomerseapplication.DTOs.responses;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    public String reviewText;
    public short rating;
    public CustomerDetailsForReview customerDetailsForReview;



}

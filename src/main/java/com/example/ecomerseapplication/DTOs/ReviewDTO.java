package com.example.ecomerseapplication.DTOs;

public record ReviewDTO(long reviewId,
                        String reviewText,
                        short rating,
                        ReviewCustomerDTO customer) {
}

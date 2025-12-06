package com.example.ecomerseapplication.DTOs.serverDtos;

public record ReviewDTO(long reviewId,
                        String reviewText,
                        short rating,
                        ReviewCustomerDTO customer) {
}

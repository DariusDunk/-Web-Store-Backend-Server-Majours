package com.example.ecomerseapplication.DTOs;

public record ReviewDTO(String reviewText,
                        short rating,
                        ReviewCustomerDTO customer) {
}

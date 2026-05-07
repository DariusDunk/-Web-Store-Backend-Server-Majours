package com.example.ecomerseapplication.DTOs.responses;

public record ErrorResponse(String type,
                            String title,
                            int status,
                            String detail) {
}

package com.example.ecomerseapplication.DTOs.responses;

public record ErrorResponse(String type,          // A URI reference to the error type (can be "about:blank")
                            String title,         // Short, human-readable summary of the problem
                            int status,           // HTTP status code
                            String detail) {
}

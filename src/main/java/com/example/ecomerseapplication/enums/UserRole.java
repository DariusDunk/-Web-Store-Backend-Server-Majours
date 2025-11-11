package com.example.ecomerseapplication.enums;

import lombok.Getter;

public enum UserRole {
    ADMIN("admin"),
    CUSTOMER("customer");

    @Getter
    private final String value;

    UserRole(String value) {
        this.value = value;
    }
}

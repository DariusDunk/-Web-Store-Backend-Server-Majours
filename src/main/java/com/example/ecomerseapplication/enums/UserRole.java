package com.example.ecomerseapplication.enums;

import lombok.Getter;

public enum UserRole {
    ADMIN("ADMIN"),
    CUSTOMER("CUSTOMER");

    @Getter
    private final String value;

    UserRole(String value) {
        this.value = value;
    }

//    /**
//     * Returns an object of the enum, based on the {@code value} object.
//     * Using the name of the enum will cause exceptions.
//     * To get the status object from its variable name, see {@link #valueOf(String)}
//     * @param value the value of the enum, not its name.
//     * @return the status object
//     * @see #valueOf(String)
//     */
//    public static UserRole fromValue(String value) {
//        for (UserRole status : UserRole.values()) {
//            if (status.value.equals(value)) {
//                return status;
//            }
//        }
//        throw new IllegalArgumentException("Unknown value: " + value);
//    }
}

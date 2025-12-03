package com.example.ecomerseapplication.DTOs.responses;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailsForReview {
    public String name;
    public String customerPfp;
    public boolean currentUser;
    public boolean isVerified;
    public Long userId;

    public CustomerDetailsForReview(String name, String customerPfp) {
        this.name = name;
        this.customerPfp = customerPfp;
    }

    public CustomerDetailsForReview(boolean currentUser, boolean isVerified) {
        this.currentUser = currentUser;
        this.isVerified = isVerified;
    }
}

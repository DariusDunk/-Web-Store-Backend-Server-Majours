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

}

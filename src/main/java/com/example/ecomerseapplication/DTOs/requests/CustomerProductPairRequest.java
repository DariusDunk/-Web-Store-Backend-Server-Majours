package com.example.ecomerseapplication.DTOs.requests;

import lombok.ToString;

@ToString
public class CustomerProductPairRequest {
    public long customerId;
    public String productCode;
}

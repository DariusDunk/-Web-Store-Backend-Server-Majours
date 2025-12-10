package com.example.ecomerseapplication.DTOs.requests;

import lombok.ToString;

@ToString
public class ProductForCartRequest {
    public CustomerProductPairRequest customerProductPairRequest;
    public short quantity;
}

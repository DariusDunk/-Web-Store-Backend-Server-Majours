package com.example.ecomerseapplication.DTOs.responses;


import com.example.ecomerseapplication.DTOs.CompactProductQuantityPairResponse;

import java.util.ArrayList;
import java.util.List;

public class CustomerCartResponse {
    public List<CompactProductQuantityPairResponse> productQuantityPair = new ArrayList<>();
    public int totalCost;
}

package com.example.ecomerseapplication.DTOs.requests;

import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Entities.ProductCategory;
import java.util.Map;

public class ProductFilterRequest {
    public Map<Integer, String> filterAttributes;
    public ProductCategory productCategory;
    public int priceLowest;
    public int priceHighest;

    public Manufacturer manufacturer;

}

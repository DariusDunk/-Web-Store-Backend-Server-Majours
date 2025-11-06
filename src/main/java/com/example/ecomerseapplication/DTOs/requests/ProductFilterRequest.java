package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ProductFilterRequest {
    @JsonProperty("filter_attributes")
    public Map<String, String> filterAttributes;
    @JsonProperty("product_category")
    public String productCategory;
    @JsonProperty("price_lowest")
    public int priceLowest;
    @JsonProperty("price_highest")
    public int priceHighest;
    @JsonProperty("manufacturer_name")
    public String  manufacturerName;

}

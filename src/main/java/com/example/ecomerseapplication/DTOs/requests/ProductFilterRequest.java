package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
public class ProductFilterRequest {
    @JsonProperty("filter_attributes")
    public Map<String, List<String>> filterAttributes;
    @JsonProperty("product_category")
    public String productCategory;
    @JsonProperty("price_lowest")
    public int priceLowest;
    @JsonProperty("price_highest")
    public int priceHighest;
    @JsonProperty("manufacturer_names")
    public List<String> manufacturerNames;
    @JsonProperty("rating")
    public Integer rating;
}

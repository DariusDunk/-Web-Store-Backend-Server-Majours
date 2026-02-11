package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@ToString
public class DetailedProductResponse {
    public String name;
    public String categoryName;
    public int originalPriceStotinki;
    public int salePriceStotinki;
    public String productCode;
    public String manufacturer;
//    public Set<CategoryAttribute> attributes;
    public Set<AttributeOptionResponse> attributes;
    public String productDescription;
    public short rating;
    public short deliveryCost;
    public String model;
    @JsonProperty("productImages")
    public List<String> productImageURLs;
//    public List<ReviewResponse> reviews;
    public boolean inFavourites = false;
    public boolean inCart = false;
    public boolean reviewed = false;
    public boolean isInStock = false;
}

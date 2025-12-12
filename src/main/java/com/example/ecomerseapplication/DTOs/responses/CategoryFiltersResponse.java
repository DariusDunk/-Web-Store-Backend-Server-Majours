package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

public class CategoryFiltersResponse {

    @JsonProperty("category_attributes")
    public List<CategoryAttributesResponse> categoryAttributesResponses;

    //    public Set<ManufacturerDTOResponse> manufacturerDTOResponseSet;
    @JsonProperty("manufacturers")
    public Set<String> manufacturerNames;

    @JsonProperty("ratings")
    public Set<Integer> ratings;

    @JsonProperty("price_lowest")
    public Integer priceLowest;

    @JsonProperty("price_highest")
    public Integer priceHighest;
}

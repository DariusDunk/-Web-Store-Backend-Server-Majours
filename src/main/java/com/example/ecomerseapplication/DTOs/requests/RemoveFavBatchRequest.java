package com.example.ecomerseapplication.DTOs.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RemoveFavBatchRequest(@JsonProperty("current_page")
                                    short currentPage,
                                    @JsonProperty("product_codes")
                                    List<String> productCodes) {

}

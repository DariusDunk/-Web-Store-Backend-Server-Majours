package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProductImagesSectionResponse(
        @JsonProperty("main_image")
        ProductImageResponse mainImage,
        @JsonProperty("gallery_images")
        List<ProductImageResponse> galleryImages) {
}

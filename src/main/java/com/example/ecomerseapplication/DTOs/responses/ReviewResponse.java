package com.example.ecomerseapplication.DTOs.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    public long reviewId;
    public String reviewText;
    public short rating;
    @JsonProperty("post_timestamp")
    public LocalDateTime postTimestamp;
    public CustomerDetailsForReview customerDetailsForReview;
    @JsonProperty("is_deleted")
    public Boolean isDeleted;
}

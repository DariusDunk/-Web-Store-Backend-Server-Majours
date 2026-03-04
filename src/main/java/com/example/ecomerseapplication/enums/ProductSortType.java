package com.example.ecomerseapplication.enums;

import lombok.Getter;

public enum ProductSortType {
    RELEVANCE("relevance"),
    POPULARITY("popularity"),
    PRICE_ASC("price_asc"),
    PRICE_DESC("price_desc"),
    NEWEST("newest"),
    REVIEW_COUNT("review_count");

    @Getter
    private final String value;

    ProductSortType(String value) {
        this.value = value;
    }
}

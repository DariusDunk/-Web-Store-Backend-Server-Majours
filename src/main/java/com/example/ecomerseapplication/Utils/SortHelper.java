package com.example.ecomerseapplication.Utils;

import org.springframework.data.domain.Sort;

public class SortHelper {

    public static Sort buildProdSort(String  sortType) {
        return switch (sortType) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "salePriceStotinki");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "salePriceStotinki");
            case "newest" -> Sort.by(Sort.Direction.DESC, "creationTimeStamp");
            case "review_count" -> Sort.by(Sort.Direction.DESC, "reviewCount");
            case "popularity" -> Sort.by(Sort.Direction.DESC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "reviewCount"));
            default -> Sort.by(Sort.Direction.ASC, "name");
        };
    }
}

package com.example.ecomerseapplication.Utils;

import org.springframework.data.domain.Sort;

public class SortHelper {

    public static Sort buildProdSort(String  sortType) {
        return switch (sortType) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "creationTimeStamp");
            case "review_count" -> Sort.by(Sort.Direction.DESC, "reviewCount");
            case "popularity" -> Sort.by(Sort.Direction.DESC, "reviewCount")
                    .and(Sort.by(Sort.Direction.DESC, "rating"));
            case "product_code" -> Sort.by(Sort.Direction.ASC, "productCode");

            default -> Sort.by(Sort.Direction.ASC, "name");
        };
    }
}

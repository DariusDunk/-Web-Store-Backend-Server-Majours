package com.example.ecomerseapplication.Auth.helpers;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

@Component
public class EndpointMatcher {

    private final AntPathMatcher matcher = new AntPathMatcher();

//    private final List<String> PUBLIC_AND_SEMI_PROTECTED_ENDPOINTS = List.of(
//            "/auth/**",
//            "/cart/*",
//            "/cart/add/quantity",
//            "/attributes/**",
//            "/category/**",
//            "/manufacturer/**",
//            "/product/*",
//            "/product/*/review/overview",
//            "/product/manufacturer/*/p*",
//            "/product/category/*/p*",
//            "/product/filter/*",
//            "/product/reviews/paged"
//    );

    private final List<String> PUBLIC_ENDPOINTS = List.of(
            "/attributes/**",
            "/category/**",
            "/manufacturer/**",
            "/product/*/review/overview",
            "/product/manufacturer/*/p*",//todo opravi strukturata na URL-a da dyrji "p" i samata stranica otdeleni
            "/product/category/*/p*",//todo opravi strukturata na URL-a da dyrji "p" i samata stranica otdeleni
            "/product/filter/*"
    );//todo purchase endpoint-ovete predstoqt da se dobavqt

    private final List<String> SEMI_PROTECTED_ENDPOINTS = List.of(
            "/auth/**",
            "/cart/*",
            "/cart/add/quantity",
            "/product/*",
            "/product/reviews/paged"
    );//todo purchase endpoint-ovete predstoqt da se dobavqt

    public boolean isPublicOrSemiProtected(String path) {

        List<String> publicAndSemiProtectedEndpoints = new ArrayList<>(PUBLIC_ENDPOINTS);
        publicAndSemiProtectedEndpoints.addAll(SEMI_PROTECTED_ENDPOINTS);

        return publicAndSemiProtectedEndpoints.stream()
                .anyMatch(pattern -> matcher.match(pattern, path));
    }

    public boolean isPublic(String path) {
        return PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> matcher.match(pattern, path));
    }

}
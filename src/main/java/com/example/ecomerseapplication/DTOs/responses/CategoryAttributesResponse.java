package com.example.ecomerseapplication.DTOs.responses;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
public class CategoryAttributesResponse {
//    public int nameId;
    public String attributeName;
    public Set<String> options= new HashSet<>();
    public String measurementUnit;
}

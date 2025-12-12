package com.example.ecomerseapplication.DTOs.responses;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
public class CategoryAttributesResponse {
//    public int nameId;
    public String attributeName;
    public List<String> options= new ArrayList<>();
    public String measurementUnit;
}

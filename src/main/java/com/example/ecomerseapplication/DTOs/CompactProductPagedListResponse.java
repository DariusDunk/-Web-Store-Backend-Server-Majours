package com.example.ecomerseapplication.DTOs;

import com.example.ecomerseapplication.DTOs.responses.CompactProductResponse;

import java.util.List;

public class CompactProductPagedListResponse {

    public List<CompactProductResponse> content;
    public PageInformationResponse page = new PageInformationResponse();


}

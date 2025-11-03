package com.example.ecomerseapplication.DTOs.responses;

import java.util.List;

public class CompactProductPagedListResponse {

    public List<CompactProductResponse> content;
    public PageInformationResponse page = new PageInformationResponse();


}

package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.DetailedSalePageResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.DetailedSalePageProjection;

import java.util.List;

public class SaleMapper {

    public static DetailedSalePageResponse saleDetailProjToResponse(DetailedSalePageProjection projection) {
        return new DetailedSalePageResponse(
                projection.getId(),
                projection.getName(),
                projection.getDefaultDiscount(),
                projection.getStartDate(),
                projection.getEndDate(),
                projection.getIsActive(),
                projection.getProductCount());
    }

    public static List<DetailedSalePageResponse> saleDetailProjListToResponseList(List<DetailedSalePageProjection> projections) {
        return projections.stream().map(SaleMapper::saleDetailProjToResponse).toList();
    }
}

package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.DetailedSaleResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.DetailedSaleProjection;

import java.util.List;

public class SaleMapper {

    public static DetailedSaleResponse saleDetailProjToResponse(DetailedSaleProjection projection) {
        return new DetailedSaleResponse(projection.getName(),
                projection.getDefaultDiscount(),
                projection.getStartDate(),
                projection.getEndDate(),
                projection.getIsActive(),
                projection.getProductCount());
    }

    public static List<DetailedSaleResponse> saleDetailProjListToResponseList(List<DetailedSaleProjection> projections) {
        return projections.stream().map(SaleMapper::saleDetailProjToResponse).toList();
    }
}

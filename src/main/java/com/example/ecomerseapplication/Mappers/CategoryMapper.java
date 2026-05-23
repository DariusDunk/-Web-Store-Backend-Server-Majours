package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.CompactCategoryAdminResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactAdminCategoryProjection;

import java.util.List;

public class CategoryMapper {

    public static CompactCategoryAdminResponse compactAdminProjToResponse(CompactAdminCategoryProjection projection) {
        return new CompactCategoryAdminResponse(projection.getName(), projection.getId(), projection.getIsDeleted());
    }

    public static List<CompactCategoryAdminResponse> compactAdminProjListToResponseList(List<CompactAdminCategoryProjection> projections) {
        return projections.stream().map(CategoryMapper::compactAdminProjToResponse).toList();
    }
}

package com.example.ecomerseapplication.Mappers;

import com.example.ecomerseapplication.DTOs.responses.CompactManufacturerResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.ManufacturerProjection;

import java.util.List;

public class ManufacturerMapper {

    public static CompactManufacturerResponse projectionToCompactResponse(ManufacturerProjection projection) {
        return new CompactManufacturerResponse(projection.getName(), projection.getId());
    }

    public static List<CompactManufacturerResponse> projectionListToCompactResponseList(List<ManufacturerProjection> projections) {
        return projections.stream().map(ManufacturerMapper::projectionToCompactResponse).toList();
    }
}

package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.DetailedSaleResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactSaleProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.DetailedSaleProjection;
import com.example.ecomerseapplication.Entities.Sale;
import com.example.ecomerseapplication.Mappers.SaleMapper;
import com.example.ecomerseapplication.Repositories.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;

    @Autowired
    public SaleService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    public List<Sale> getExpiredSales() {
        return saleRepository.getExpiredSales();
    }

    public void markAsInActive(List<Sale> expiredSales) {
        saleRepository.markAsInactive(expiredSales);
    }

    public List<CompactSaleProjection> getTopCurrentSalesCompact() {
        return saleRepository.findActiveAndNotExpired(PageRequest.of(0, 2));
    }

    public List<DetailedSaleResponse> getAllSales() {
        List<DetailedSaleProjection> saleProjections = saleRepository.getAllSalesProjection();
        return SaleMapper.saleDetailProjListToResponseList(saleProjections);
    }



}


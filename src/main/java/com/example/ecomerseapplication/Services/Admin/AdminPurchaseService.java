package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.responses.CompactAdminPurchaseResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProjection;
import com.example.ecomerseapplication.Mappers.PurchaseMapper;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import com.example.ecomerseapplication.Services.PurchaseService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminPurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseService purchaseService;

    public AdminPurchaseService(PurchaseRepository purchaseRepository, PurchaseService purchaseService) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseService = purchaseService;
    }

    public List<CompactAdminPurchaseResponse> getPagedPurchasesCompact(int page) {
        PageRequest pageRequest = PageRequest.of(page, 10);

        List<PurchaseProjection> purchaseProjections = purchaseRepository.getAllForAdminPaged(pageRequest);

        return PurchaseMapper.purchaseProjectionListToCompactAdminResponseList(purchaseProjections);
    }
}

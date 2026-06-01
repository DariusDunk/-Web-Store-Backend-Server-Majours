package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.responses.CompactAdminPurchaseResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProjection;
import com.example.ecomerseapplication.Mappers.PurchaseMapper;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import com.example.ecomerseapplication.Services.PurchaseService;
import com.example.ecomerseapplication.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    public PageResponse<CompactAdminPurchaseResponse> getPagedPurchasesCompact(int page) {
        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        Page<PurchaseProjection> purchaseProjectionsPage = purchaseRepository.getAllForAdminPaged(pageRequest);

        List<PurchaseProjection> purchaseProjectionsList = purchaseProjectionsPage.getContent();

        List<CompactAdminPurchaseResponse> responseList = PurchaseMapper.purchaseProjectionListToCompactAdminResponseList(purchaseProjectionsList);

        Page<CompactAdminPurchaseResponse> oldPage = new PageImpl<>(responseList,
                purchaseProjectionsPage.getPageable(),
                purchaseProjectionsPage.getTotalElements());

        return PageResponse.from(oldPage);
    }

    public Integer getRefundPendingCount() {
        return purchaseRepository.refundPendingCount(DeliveryStatus.REFUND_REQUESTED);
    }
}

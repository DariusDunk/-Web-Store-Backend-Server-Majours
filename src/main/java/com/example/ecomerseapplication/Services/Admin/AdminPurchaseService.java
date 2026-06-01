package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.requests.PurchaseActionRequest;
import com.example.ecomerseapplication.DTOs.responses.CompactAdminPurchaseResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProjection;
import com.example.ecomerseapplication.Entities.Purchase;
import com.example.ecomerseapplication.Entities.PurchaseCart;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidEnumNameException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.PessimisticLockOrTimeoutPurchaseException;
import com.example.ecomerseapplication.Mappers.PurchaseMapper;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import com.example.ecomerseapplication.Services.PurchaseCartService;
import com.example.ecomerseapplication.Services.PurchaseService;
import com.example.ecomerseapplication.enums.DeliveryStatus;
import com.example.ecomerseapplication.enums.PurchaseStatusAction;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminPurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseService purchaseService;
    private final AdminProductService adminProductService;
    private final PurchaseCartService purchaseCartService;

    public AdminPurchaseService(PurchaseRepository purchaseRepository, PurchaseService purchaseService, AdminProductService adminProductService, PurchaseCartService purchaseCartService) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseService = purchaseService;
        this.adminProductService = adminProductService;
        this.purchaseCartService = purchaseCartService;
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

    @Transactional
    public void executePurchaseStatusAction(PurchaseActionRequest request) {

        Purchase purchase;
        try {
            purchase = purchaseService.getByIdWithLock(request.purchaseId());
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new PessimisticLockOrTimeoutPurchaseException("Purchase with id: " + request.purchaseId() + " currently locked updates",
                    "Заключена поръчка",
                    "Поръчката беше временно недостъпна за промени, опитайте отново");
        }

        String action = request.action();

        purchaseActionResolver(action, purchase);

    }

    private void purchaseActionResolver(String action, Purchase purchase) {

        PurchaseStatusAction newAction;
        try {
            newAction = PurchaseStatusAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidEnumNameException("Invalid PurchaseStatusAction enum name: " + action,
                    "Невалидна действие",
                    "Зададеното действие за поръчката е невалидно");
        }

        switch (newAction) {
            case DELIVER -> purchase.deliverPurchase();
            case CANCEL -> {
                purchase.cancelPurchase();
                List<PurchaseCart> purchaseCarts = purchaseCartService.getByPurchaseId(purchase.getId());
                purchaseService.restockProductsOfCancelledPurchase(purchaseCarts);
            }
            case SHIP -> purchase.shipPurchase();
            case APPROVE_REFUND -> {
                purchase.approveRefund();
                List<PurchaseCart> purchaseCarts = purchaseCartService.getByPurchaseId(purchase.getId());

                adminProductService.refreshStockOfPurchaseProducts(purchaseCarts);
            }
            case REJECT_REFUND -> purchase.rejectRefund();
        }
    }


}

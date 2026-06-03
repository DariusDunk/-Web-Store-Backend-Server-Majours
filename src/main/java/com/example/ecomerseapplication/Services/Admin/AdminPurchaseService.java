package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.requests.DateRangeRequest;
import com.example.ecomerseapplication.DTOs.requests.PurchaseActionRequest;
import com.example.ecomerseapplication.DTOs.responses.CompactAdminPurchaseResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.DTOs.responses.ReportResponses;
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
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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


    public ReportResponses.ReportResponse revenueReport(DateRangeRequest request) {

        List<PurchaseProjection> projections = purchaseRepository.purchasesOfPeriod(
                request.startDate(),
                request.endDate(),
                DeliveryStatus.DELIVERED);

        List<MonthDataResponse> months = getMonthsFromRequest(request);

        Map<String, MonthDataResponse> monthMap = months
                .stream()
                .collect(Collectors.toMap(mdr -> mdr.monthKey, mdr -> mdr));

//        System.out.println("monthMap: " + monthMap);

        ZoneId zoneId = ZoneId.of(request.timezone());

        for (PurchaseProjection projection : projections) {

            if (projection == null) {
                continue;
            }

            // 1. Convert the Instant to the user's timezone
            ZonedDateTime zonedDelivery = projection.getDeliveryDate().atZone(zoneId);

// 2. Convert to a YearMonth object and call toString()
            String mapKey = YearMonth.from(zonedDelivery).toString();

//            String mapKey = String
//                    .valueOf(projection
//                            .getDeliveryDate()
//                            .atZone(zoneId).getMonth());

//            System.out.println("month key: " + mapKey);

            MonthDataResponse monthResponse = monthMap
                    .get(mapKey);

            if (monthResponse != null) {
                monthResponse.revenueCents += projection.getTotalCost();
                monthResponse.purchasesCount++;
            }

//            System.out.println("\nprojection:\n");
//
//            System.out.println("Total cost: " + projection.getTotalCost() + "\n" +
//                    "Delivery date: " + projection.getDeliveryDate() + "");
//
//            System.out.println("monthResponse: " + monthResponse);
        }

//        System.out.println("months list: "+months);

        List<ReportResponses.MetricDto> metrics = getMetricDtos(monthMap, projections);

        ReportResponses.ChartDto chartDto = new ReportResponses.ChartDto(ReportResponses.ChartType.LINE,
                "month",
                "revenue",
                "Приходи (€)",
                buildChartDataMapList(months),
                ReportResponses.ValueType.CURRENCY);

        //        System.out.println("response: " + response);

        return ReportResponses.buildMixedReport("Приходи за периода ",
                metrics,
                chartDto,
                List.of("Месец", "Приходи (€)", "Поръчки"),
                getTableRowMapList(months),
                List.of(request.startDate().toString(), request.endDate().toString()),
                "http://localhost:3000/admin/purchases/revenue-report/pdf"
        );
    }

    @NotNull
    private static List<ReportResponses.MetricDto> getMetricDtos(Map<String, MonthDataResponse> monthMap, List<PurchaseProjection> projections) {
        int total = 0;

        for (MonthDataResponse monthResponse : monthMap.values()) {
            total += monthResponse.revenueCents;
        }

        int averagePerPurchase = total / projections.size();

        return List.of(new ReportResponses.MetricDto("Общи приходи", total+"" , ReportResponses.ValueType.CURRENCY),
                new ReportResponses.MetricDto("Брой поръчки", projections.size() + "", ReportResponses.ValueType.NUMBER),
                new ReportResponses.MetricDto("Средна стойност", averagePerPurchase + "", ReportResponses.ValueType.CURRENCY));
    }

    @NotNull
    private static List<Map<String, ReportResponses.TableRow>> getTableRowMapList(List<MonthDataResponse> months) {
//        String total = totalCount != null ? totalCount : "0";
//        String auth = authCount != null ? authCount : "0";
//        String guest = guestCount != null ? guestCount : "0";

        String mon = "Месец";
        String cash = "Приходи (€)";
        String purch = "Поръчки";

        List<Map<String, ReportResponses.TableRow>> mapList = new ArrayList<>();

        for (MonthDataResponse month : months) {
            Map<String, ReportResponses.TableRow> monthMap = Map.of(
                    mon, new ReportResponses.TableRow(month.displayName, ReportResponses.ValueType.TEXT),
                    cash, new ReportResponses.TableRow(month.revenueCents + "", ReportResponses.ValueType.CURRENCY),
                    purch, new ReportResponses.TableRow(month.purchasesCount + "", ReportResponses.ValueType.NUMBER));

            mapList.add(monthMap);
        }


//        Map<String, String> totalMap = Map.of("Тип потребител", "Общо", "Брой", total);
//        Map<String, String> authMap = Map.of("Тип потребител", "Потребители", "Брой", auth);
//        Map<String, String> guestMap = Map.of("Тип потребител", "Гост потребители", "Брой", guest);

//        return List.of(totalMap, authMap, guestMap);
        return mapList;
    }

    @NotNull
    private static List<Map<String, String>> buildChartDataMapList(List<MonthDataResponse> months) {
//        String total = totalCount != null ? totalCount : "0";
//        String auth = authCount != null ? authCount : "0";
//        String guest = guestCount != null ? guestCount : "0";

        List<Map<String, String>> responseList = new ArrayList<>();

        for (MonthDataResponse month : months) {
            Map<String, String> monthMap = Map.of("month", month.displayName, "revenue", month.revenueCents + "");
            responseList.add(monthMap);
        }

//        Map<String, String> totalMap = Map.of("sessionType", "Общо", "count", total);
//        Map<String, String> authMap = Map.of("sessionType", "Потребители", "count", auth);
//        Map<String, String> guestMap = Map.of("sessionType", "Гост потребители", "count", guest);

//        return List.of(totalMap, authMap, guestMap);
        return responseList;
    }


    public List<MonthDataResponse> getMonthsFromRequest(DateRangeRequest request) {
        // 1. Parse the timezone safely
        ZoneId zoneId = ZoneId.of(request.timezone());

        // 2. Convert the UTC Instants back to the user's local timeline
        // to accurately figure out what month it was for THEM.
        ZonedDateTime localStart = request.startDate().atZone(zoneId);
        ZonedDateTime localEnd = request.endDate().atZone(zoneId);

        List<MonthDataResponse> responseList = new ArrayList<>();

        // 3. Extract the months sequentially
        YearMonth startMonth = YearMonth.from(localStart);
        YearMonth endMonth = YearMonth.from(localEnd);

        Locale bgLocale = Locale.of("bg", "BG");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", bgLocale);

        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            MonthDataResponse item = new MonthDataResponse();

            // "2026-01" -> ISO standard, always sorts perfectly, easy to map to data
            item.monthKey = current.toString();

            // Localized name for table headers / graph axis labels
            item.displayName = current.format(formatter);

            responseList.add(item);
            current = current.plusMonths(1);
        }

        return responseList;
    }

    @ToString
    public static class MonthDataResponse {
        public String monthKey;
        public String displayName;
        public int revenueCents = 0;
        public int purchasesCount = 0;
    }
}

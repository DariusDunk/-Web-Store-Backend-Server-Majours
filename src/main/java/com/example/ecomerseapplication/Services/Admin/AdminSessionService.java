package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.requests.ActiveSessionsPDFRequest;
import com.example.ecomerseapplication.DTOs.responses.ReportResponses;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.SessionActivityProjection;
import com.example.ecomerseapplication.Repositories.SessionRepository;
import com.example.ecomerseapplication.Services.ReportPdfService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class AdminSessionService {
    private final SessionRepository sessionRepository;
    private final ReportPdfService reportPdfService;

    public AdminSessionService(SessionRepository sessionRepository, ReportPdfService reportPdfService) {
        this.sessionRepository = sessionRepository;
        this.reportPdfService = reportPdfService;
    }

    public ReportResponses.ReportResponse getActiveSessions() {

        Instant now = Instant.now();
        Instant fourMinutesAgo = now.minus(4, ChronoUnit.MINUTES);
        Instant truncated = now.truncatedTo(ChronoUnit.MINUTES);

        List<String> dateList = List.of(truncated.toString());

        SessionActivityProjection projection = sessionRepository.getActiveSessionCount(fourMinutesAgo);

        return getReportResponse(projection.getTotal()!=null ?projection.getTotal().toString(): "0",
                projection.getAuth()!=null ?projection.getAuth().toString() :"0",
                projection.getGuest()!=null ?projection.getGuest().toString():"0",
                dateList);
    }

    @NotNull
    private static ReportResponses.ReportResponse getReportResponse(String totalCount, String authCount, String guestCount, List<String> dateList) {
        List<Map<String, String>> rows = getMapList(
                totalCount,
                authCount,
                guestCount);

        List<Map<String, String>> chartData = buildChartDataMapList(
                totalCount,
                authCount,
                guestCount);

        ReportResponses.ChartDto chartDto = ReportResponses.buildBarChart("sessionType", "count", "Брой потребители", chartData);

        ReportResponses.ReportResponse response = ReportResponses.buildMixedReport("Потребителска активност към ",
                null,
                chartDto,
                List.of("Тип потребител", "Брой"),
                rows,
                dateList,
                "http://localhost:3000/admin/session/active-sessions/pdf");

        System.out.println("response: " + response);

        return response;
    }

    @NotNull
    private static List<Map<String, String>> getMapList(String totalCount, String authCount, String guestCount) {
        String total = totalCount != null ? totalCount : "0";
        String auth = authCount != null ? authCount : "0";
        String guest = guestCount != null ? guestCount : "0";

        Map<String, String> totalMap = Map.of("Тип потребител", "Общо", "Брой", total);
        Map<String, String> authMap = Map.of("Тип потребител", "Потребители", "Брой", auth);
        Map<String, String> guestMap = Map.of("Тип потребител", "Гост потребители", "Брой", guest);

        return List.of(totalMap, authMap, guestMap);
    }

    @NotNull
    private static List<Map<String, String>> buildChartDataMapList(String totalCount, String authCount, String guestCount) {
        String total = totalCount != null ? totalCount : "0";
        String auth = authCount != null ? authCount : "0";
        String guest = guestCount != null ? guestCount : "0";

        Map<String, String> totalMap = Map.of("sessionType", "Общо", "count", total);
        Map<String, String> authMap = Map.of("sessionType", "Потребители", "count", auth);
        Map<String, String> guestMap = Map.of("sessionType", "Гост потребители", "count", guest);

        return List.of(totalMap, authMap, guestMap);
    }
}

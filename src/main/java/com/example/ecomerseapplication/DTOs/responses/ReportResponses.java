package com.example.ecomerseapplication.DTOs.responses;

import java.util.List;
import java.util.Map;

public class ReportResponses {
    public record ReportResponse(
            ReportType type,
            String title,
            List<MetricDto> metrics,
            ChartDto chart,
            List<String> columns,
            List<Map<String, String>> rows
    ) {}

    public record MetricDto(
            String label,
            String value
    ) {}

    public record ChartDto(
            ChartType type,
            String xKey,
            String yKey,
            String label,
            List<Map<String, String>> data
    ) {}

    public enum ReportType {
        TABLE,
        CHART,
        MIXED
    }

    public enum ChartType {
        LINE,
        BAR
    }

    public static ReportResponse buildTableReport(String title, List<String> columns, List<Map<String, String>> rows) {
        return new ReportResponse(ReportType.TABLE, title, null, null, columns, rows);
    }

    public static ReportResponse buildMixedReport(String title,
                                                  List<MetricDto> metrics,
                                                  ChartDto chart,
                                                  List<String> columns,
                                                  List<Map<String, String>> rows) {
        return new ReportResponse(ReportType.MIXED, title, metrics, chart, columns, rows);
    }

    public static ChartDto buildBarChart(String xKey, String yKey, String label, List<Map<String, String>> data) {
        return new ChartDto(ChartType.BAR, xKey, yKey, label, data);
    }
}

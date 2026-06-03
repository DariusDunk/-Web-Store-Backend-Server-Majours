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
            List<Map<String, TableRow>> rows,
            List<String> dates,
            String pdfUrl
    ) {}

    public record TableRow(String value, ValueType valueType) {
    }


    public record MetricDto(
            String label,
            String value,
            ValueType type
    ) {}

    public record ChartDto(
            ChartType type,
            String xKey,
            String yKey,
            String label,
            List<Map<String, String>> data,
            ValueType valueType
    ) {}

    public enum ReportType {
        TABLE,
        MIXED
    }

    public enum ChartType {
        LINE,
        BAR
    }

    public enum ValueType {
        CURRENCY,
        TEXT,
        NUMBER
    }

    public static ReportResponse buildTableReport(String title,
                                                  List<String> columns,
                                                  List<Map<String, TableRow>> rows,
                                                  List<String> dateData,
                                                  String pdfUrl
                                                  ) {
        return new ReportResponse(ReportType.TABLE, title, null, null, columns, rows, dateData, pdfUrl);
    }

    public static ReportResponse buildMixedReport(String title,
                                                  List<MetricDto> metrics,
                                                  ChartDto chart,
                                                  List<String> columns,
                                                  List<Map<String, TableRow>> rows,
                                                  List<String> dateData,
                                                  String pdfUrl) {
        return new ReportResponse(ReportType.MIXED, title, metrics, chart, columns, rows , dateData, pdfUrl);
    }

    public static ChartDto buildBarChart(String xKey, String yKey, String label, List<Map<String, String>> data, ValueType valueType) {
        return new ChartDto(ChartType.BAR, xKey, yKey, label, data, valueType);
    }


}

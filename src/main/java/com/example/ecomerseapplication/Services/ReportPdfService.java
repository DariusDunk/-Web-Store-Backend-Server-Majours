package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.responses.ReportResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Generates a branded PDF document from any {@link ReportResponses.ReportResponse}.
 *
 * <p>Only the <em>table</em> section of the response is rendered (columns + rows).
 * Charts are intentionally excluded — they live in the React frontend only.
 *
 * <p>Relies on {@link PDFService#generateInvoicePdf(String)} for the actual PDF
 * conversion, so Open Sans (regular + bold) is already registered and available
 * in CSS without an explicit {@code @font-face} block.
 *
 * <p>OpenHTMLtoPDF CSS constraints respected throughout:
 * <ul>
 *   <li>No {@code display: flex / grid} — all multi-column layout uses {@code <table>}.</li>
 *   <li>No {@code :nth-child} pseudo-selectors — row striping is applied via Java-added classes.</li>
 *   <li>Background colours on block elements and {@code <td>} work fine.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ReportPdfService {

    private final PDFService pdfService;

    // ─── Store constants (mirror InvoiceHtmlService) ──────────────────────────
    private static final String STORE_NAME    = "Агромаг";
    private static final String STORE_TAGLINE = "Земеделски продукти и консумативи";
    private static final String STORE_EIK     = "ЕИК: 1234567890";
    private static final String STORE_VAT     = "ДДС номер: BG1234567890";
    private static final String STORE_ADDRESS = "гр. Пловдив, ул. Роза 14";
    private static final String STORE_PHONE   = "Телефон: +359 88 123 4567";

    private static final ZoneId            SOFIA_ZONE         = ZoneId.of("Europe/Sofia");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Builds the HTML for {@code report} and converts it to a PDF byte array.
     *
     * @param report a {@link ReportResponses.ReportResponse} — any {@code ReportType}
     *               is accepted; only {@code columns} and {@code rows} are rendered.
     * @return raw PDF bytes ready to stream as {@code application/pdf}.
     */
    public byte[] generateReportPdf(ReportResponses.ReportResponse report) {
        String html = buildReportHtml(report);
        return pdfService.generateInvoicePdf(html);
    }

    // ─── HTML orchestration ───────────────────────────────────────────────────

    private String buildReportHtml(ReportResponses.ReportResponse report) {
        String timestamp = nowTimestamp();
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<meta charset=\"UTF-8\" />");
        sb.append("<style>");
        appendStyles(sb);
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");

        appendPageHeader(sb);
        appendTitleSection(sb, report.title(), timestamp);

        if (hasTableData(report)) {
            appendTable(sb, report.columns(), report.rows());
        } else {
            appendEmptyState(sb);
        }

        appendPageFooter(sb, timestamp);

        sb.append("</body>");
        sb.append("</html>");
        return sb.toString();
    }

    // ─── Styles ───────────────────────────────────────────────────────────────

    private void appendStyles(StringBuilder sb) {

        // ── Reset & base ──────────────────────────────────────────────────────
        // Open Sans is pre-registered in PDFService — no @font-face needed here.
        sb.append("body {")
          .append("  font-family: 'Open Sans', sans-serif;")
          .append("  color: #111;")
          .append("  font-size: 13px;")
          .append("  line-height: 1.5;")
          .append("  margin: 0;")
          .append("  padding: 0;")
          .append("}");
        sb.append("b, strong { font-weight: bold; }");

        // ── Branded header bar ────────────────────────────────────────────────
        // Uses a full-width table because OpenHTMLtoPDF does not support flexbox.
        sb.append(".page-header {")
          .append("  background-color: #2d6a2d;")
          .append("  padding: 18px 32px;")
          .append("}");
        sb.append(".header-layout { width: 100%; border-collapse: collapse; }");
        sb.append(".header-layout td { vertical-align: middle; }");
        sb.append(".store-name  { color: #ffffff; font-size: 20px; font-weight: bold; }");
        sb.append(".store-tagline { color: #c8e6c9; font-size: 10px; margin-top: 3px; }");
        sb.append(".header-right  { color: #e8f5e9; font-size: 10px; line-height: 1.9; text-align: right; }");

        // ── Title / meta section ──────────────────────────────────────────────
        sb.append(".title-section { padding: 24px 32px 0 32px; }");
        sb.append(".report-label  { font-size: 9px; color: #888; letter-spacing: 0.1em; text-transform: uppercase; margin-bottom: 4px; }");
        sb.append(".report-title  { font-size: 17px; font-weight: bold; color: #1a1a1a; }");
        sb.append(".report-meta   { font-size: 11px; color: #666; margin-top: 5px; }");
        sb.append(".title-rule    { border: none; border-top: 2px solid #2d6a2d; margin: 16px 32px 0 32px; }");

        // ── Data table ────────────────────────────────────────────────────────
        // :nth-child is unreliable in OpenHTMLtoPDF; striping classes are applied in Java instead.
        sb.append(".table-wrapper { padding: 20px 32px 0 32px; }");
        sb.append(".data-table { width: 100%; border-collapse: collapse; }");
        sb.append(".data-table th {")
          .append("  background-color: #2d6a2d;")
          .append("  color: #ffffff;")
          .append("  text-align: left;")
          .append("  padding: 10px 12px;")
          .append("  font-size: 12px;")
          .append("  font-weight: bold;")
          .append("}");
        sb.append(".data-table td { padding: 9px 12px; font-size: 12px; border-bottom: 1px solid #e0e0e0; }");
        sb.append(".row-even td { background-color: #f4f8f4; color: #1a1a1a; }");
        sb.append(".row-odd  td { background-color: #ffffff;  color: #222;   }");
        // Bottom accent on the last row
        sb.append(".row-last td { border-bottom: 2px solid #2d6a2d; }");

        sb.append(".row-count { font-size: 10px; color: #999; text-align: right; padding: 8px 32px 0 0; }");

        // ── Empty state ───────────────────────────────────────────────────────
        sb.append(".empty-state { padding: 48px 32px; text-align: center; color: #aaa; font-size: 13px; }");

        // ── Page footer ───────────────────────────────────────────────────────
        sb.append(".page-footer   { margin-top: 48px; border-top: 1px solid #ddd; padding: 14px 32px; }");
        sb.append(".footer-layout { width: 100%; border-collapse: collapse; }");
        sb.append(".footer-layout td { font-size: 10px; color: #888; vertical-align: top; }");
        sb.append(".auto-note     { font-size: 9px; color: #bbb; text-align: center; padding: 10px 32px 16px; }");
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    private void appendPageHeader(StringBuilder sb) {
        sb.append("<div class='page-header'>");
        sb.append("<table class='header-layout'><tr>");

        // Left: store branding
        sb.append("<td>");
        sb.append("<div class='store-name'>").append(esc(STORE_NAME)).append("</div>");
        sb.append("<div class='store-tagline'>").append(esc(STORE_TAGLINE)).append("</div>");
        sb.append("</td>");

        // Right: legal / contact info
        sb.append("<td class='header-right'>");
        sb.append(esc(STORE_EIK)).append("<br/>");
        sb.append(esc(STORE_VAT)).append("<br/>");
        sb.append(esc(STORE_ADDRESS)).append("<br/>");
        sb.append(esc(STORE_PHONE));
        sb.append("</td>");

        sb.append("</tr></table>");
        sb.append("</div>");
    }

    // ─── Title / timestamp section ────────────────────────────────────────────

    private void appendTitleSection(StringBuilder sb, String title, String timestamp) {
        sb.append("<div class='title-section'>");
        sb.append("<div class='report-label'>Статистически отчет</div>");
        sb.append("<div class='report-title'>").append(esc(title)).append("</div>");
        sb.append("<div class='report-meta'>Генериран на: <b>").append(esc(timestamp)).append("</b></div>");
        sb.append("</div>");
        sb.append("<hr class='title-rule' />");
    }

    // ─── Table ────────────────────────────────────────────────────────────────

    private void appendTable(StringBuilder sb, List<String> columns, List<Map<String, ReportResponses.TableRow>> rows) {
        sb.append("<div class='table-wrapper'>");
        sb.append("<table class='data-table'>");

        // Header row — column names driven entirely by the ReportResponse
        sb.append("<thead><tr>");
        for (String col : columns) {
            sb.append("<th>").append(esc(col)).append("</th>");
        }
        sb.append("</tr></thead>");

        // Body rows — striping classes applied here because :nth-child is unreliable
        sb.append("<tbody>");
        int lastIndex = rows.size() - 1;
        for (int i = 0; i < rows.size(); i++) {
            String stripingClass = (i % 2 == 0) ? "row-even" : "row-odd";
            String lastClass     = (i == lastIndex) ? " row-last" : "";

            sb.append("<tr class='").append(stripingClass).append(lastClass).append("'>");
            for (String col : columns) {
                ReportResponses.TableRow rowElement = rows.get(i).get(col);
                ReportResponses.ValueType valueType = rowElement!=null ? rowElement.valueType() : ReportResponses.ValueType.TEXT;
                String value = rowElement!=null ? rowElement.value() : "-";

                if (valueType != null && valueType.equals(ReportResponses.ValueType.CURRENCY)) {
                    value = ((Integer.parseInt(value) + 50) / 100) + "€";
                }
//                String value = rows.get(i).getOrDefault(col, new ReportResponses.TableRow("-", ReportResponses.ValueType.TEXT)).value();
                sb.append("<td>").append(esc(value)).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");

        sb.append("</table>");
        sb.append("</div>");

        sb.append("<div class='row-count'>Общо записи: <b>").append(rows.size()).append("</b></div>");
    }

    // ─── Empty state ──────────────────────────────────────────────────────────

    private void appendEmptyState(StringBuilder sb) {
        sb.append("<div class='empty-state'>Няма данни за показване.</div>");
    }

    // ─── Footer ───────────────────────────────────────────────────────────────

    private void appendPageFooter(StringBuilder sb, String timestamp) {
        sb.append("<div class='page-footer'>");
        sb.append("<table class='footer-layout'><tr>");

        // Left: store identity
        sb.append("<td>");
        sb.append("<b>").append(esc(STORE_NAME)).append("</b><br/>");
        sb.append(esc(STORE_ADDRESS)).append("<br/>");
        sb.append(esc(STORE_PHONE));
        sb.append("</td>");

        // Right: generation metadata
        sb.append("<td style='text-align: right;'>");
        sb.append("Вътрешен административен документ<br/>");
        sb.append("Генериран: ").append(esc(timestamp));
        sb.append("</td>");

        sb.append("</tr></table>");
        sb.append("</div>");

        sb.append("<div class='auto-note'>")
          .append("Документът е генериран автоматично от системата и не изисква подпис.")
          .append("</div>");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private boolean hasTableData(ReportResponses.ReportResponse report) {
        return report.columns() != null && !report.columns().isEmpty()
            && report.rows()    != null && !report.rows().isEmpty();
    }

    private String nowTimestamp() {
        return ZonedDateTime.now(SOFIA_ZONE).format(TIMESTAMP_FORMATTER);
    }

    /**
     * Escapes characters that would break HTML rendering — mirrors the same
     * helper in {@link InvoiceHtmlService} to keep behaviour consistent.
     */
    private String esc(String text) {
        if (text == null) return "";
        return text.replace("&",  "&amp;")
                   .replace("<",  "&lt;")
                   .replace(">",  "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'",  "&#39;");
    }
}

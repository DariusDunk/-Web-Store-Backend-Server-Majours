package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.InvoiceFullDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.InvoicePurchaseProjection;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PDFService {

    private byte[] regularFontData;
    private byte[] boldFontData;

    @PostConstruct
    public void initFonts() {

        try {
            this.regularFontData = loadFont("/fonts/OpenSans-Regular.ttf");
            this.boldFontData = loadFont("/fonts/OpenSans-Bold.ttf");
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize fonts for PDF generation", e);
        }
    }

    private byte[] loadFont(String path) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) throw new IOException("Font not found at: " + path);
            return is.readAllBytes();
        }
    }

    public byte[] generateTestPdf() {

        try {

            String html = """
                    <html>
                        <body>
                            <h1>Test Invoice</h1>
                            <p>PDF generation works.</p>
                        </body>
                    </html>
                    """;

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            PdfRendererBuilder builder =
                    new PdfRendererBuilder();

            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);

            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateInvoicePdf(InvoiceFullDTO invoice) {
        try {
            String html = buildInvoicePdfString(invoice);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // Register Regular weight
            builder.useFont(() -> new ByteArrayInputStream(regularFontData),
                    "Open Sans", 400, BaseRendererBuilder.FontStyle.NORMAL, true);

            builder.useFont(() -> new ByteArrayInputStream(boldFontData),
                    "Open Sans", 700, BaseRendererBuilder.FontStyle.NORMAL, true);


            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    public String buildInvoicePdfString(InvoiceFullDTO invoice) {
        InvoicePurchaseProjection projection = invoice.invoicePurchaseProjection();

        // Format the date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                .withZone(ZoneId.systemDefault());
        String formattedDate = dateFormatter.format(projection.getDate());

        // Bulgarian locale ensures the decimal separator is a comma (e.g., 10,50 лв.)
        Locale bgLocale = Locale.forLanguageTag("bg-BG");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset=\"UTF-8\" />");
        html.append("<style>");

        // Linking the registered Java fonts to the CSS engine
//        html.append("@font-face { font-family: 'Open Sans'; src: url('OpenSans-Regular.ttf'); -fs-pdf-font-embed: embed; -fs-pdf-font-encoding: Identity-H; font-weight: normal; }");
//        html.append("@font-face { font-family: 'Open Sans'; src: url('OpenSans-Bold.ttf'); -fs-pdf-font-embed: embed; -fs-pdf-font-encoding: Identity-H; font-weight: bold; }");

        html.append("body { font-family: 'Open Sans', sans-serif; color: #000; font-size: 13px; line-height: 1.4; }");
        html.append("b, strong { font-weight: bold; }");
        html.append("table { width: 100%; border-collapse: collapse; }");
        html.append(".header-table td { vertical-align: top; }");
        html.append(".title { text-align: center; font-size: 22px; font-weight: bold; margin: 40px 0; }");

        html.append(".items-table { margin-bottom: 20px; }");
        html.append(".items-table th { text-align: left; padding: 10px 0; border-top: 2px solid #000; border-bottom: 1px solid #000; }");
        html.append(".items-table td { padding: 10px 0; border-bottom: 1px solid #eee; }");

        // Column alignment and width
        html.append(".items-table th:nth-child(2), .items-table td:nth-child(2) { text-align: right; width: 12%; }");
        html.append(".items-table th:nth-child(3), .items-table td:nth-child(3) { text-align: right; width: 18%; }");
        html.append(".items-table th:nth-child(4), .items-table td:nth-child(4) { text-align: right; width: 18%; }");

        html.append(".totals-table { width: 100%; margin-top: 20px; border-top: 1px solid #000; }");
        html.append(".totals-table td { text-align: right; padding: 6px 0; }");
        html.append(".totals-table .total-row td { font-weight: bold; font-size: 16px; padding-top: 10px; border-top: 1px double #000; }");

        html.append(".footer { margin-top: 60px; border-top: 1px solid #ccc; padding-top: 20px; font-size: 11px; color: #444; }");
        html.append(".footer-table td { vertical-align: top; width: 50%; }");
        html.append(".signatures { margin-top: 60px; }");
        html.append("</style></head><body>");

        // --- HEADER SECTION ---
        html.append("<table class=\"header-table\"><tr>");
        html.append("<td>");
        html.append("Получател:<br />");
        html.append("<b>").append(escapeHtml(projection.getContactName())).append("</b><br />");
        html.append(escapeHtml(projection.getAddress())).append("<br/>");
        html.append("</td>");
        html.append("<td style=\"text-align: right;\">");
        html.append("Дата на издаване: ").append(formattedDate);
        html.append("</td>");
        html.append("</tr></table>");

        // --- TITLE ---
        html.append("<div class=\"title\">Фактура # ").append(escapeHtml(projection.getPurchaseCode())).append(" (Оригинал)</div>");

        // --- PRODUCTS TABLE ---
        html.append("<table class=\"items-table\">");
        html.append("<tr><th>Наименование</th><th>К-во</th><th>Цена</th><th>Общо</th></tr>");

        for (var item : invoice.products()) {
            var product = item.purchaseProductProjection();

            double price = item.finalPrice() / 100.0;
            int qty = product.getQuantity();
            double totalLine = price * qty;

            html.append("<tr>");
            html.append("<td>").append(escapeHtml(product.getProductName())).append("</td>");
            html.append("<td>").append(qty).append("</td>");
            html.append("<td>").append(String.format(bgLocale, "%.2f лв.", price)).append("</td>");
            html.append("<td>").append(String.format(bgLocale, "%.2f лв.", totalLine)).append("</td>");
            html.append("</tr>");
        }
        html.append("</table>");

        // --- TOTALS SECTION ---
        double productTotal = projection.getProductTotal() / 100.0;
        double shippingFee = projection.getShippingFee() / 100.0;
        double totalCost = projection.getTotalCost() / 100.0;

        html.append("<table class=\"totals-table\">");
        html.append("<tr><td>Общо:</td><td style=\"width: 120px;\">")
                .append(String.format(bgLocale, "%.2f лв.", productTotal)).append("</td></tr>");
        html.append("<tr><td>Доставка:</td><td>")
                .append(String.format(bgLocale, "%.2f лв.", shippingFee)).append("</td></tr>");
        html.append("<tr class=\"total-row\"><td>Сума за плащане:</td><td>")
                .append(String.format(bgLocale, "%.2f лв.", totalCost)).append("</td></tr>");
        html.append("</table>");

        // --- FOOTER SECTION ---
        html.append("<div class=\"footer\">");
        html.append("<table class=\"footer-table\"><tr>");
        html.append("<td>");
        html.append("<b>Агромаг</b><br />");
        html.append("ЕИК: 1234567890<br />");
        html.append("ДДС номер: BG1234567890<br />");
        html.append("МОЛ: 0987654321");
        html.append("</td>");
        html.append("<td>");
        html.append("Адрес:<br />");
        html.append("гр. Пловдив, ул. Роза 14<br />");
        html.append("Телефон: +359 88 123 4567");
        html.append("</td>");
        html.append("</tr></table>");
        html.append("</div>");

        // --- SIGNATURES SECTION ---
        html.append("<table class=\"signatures\"><tr>");
        html.append("<td>Съставил: ____________________</td>");
        html.append("<td style=\"text-align: right;\">Получател: ____________________</td>");
        html.append("</tr></table>");

        html.append("</body></html>");

        return html.toString();
    }
    /**
     * Escapes characters that could break HTML rendering.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

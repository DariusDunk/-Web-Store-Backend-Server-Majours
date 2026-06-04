package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.InvoiceFullDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.InvoicePurchaseProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InvoiceHtmlService {

    private final Locale bgLocale = Locale.forLanguageTag("bg-BG");

    private String buildProductRowsHtml(InvoiceFullDTO invoice, Locale locale) {

        StringBuilder html = new StringBuilder();

        for (var item : invoice.products()) {

//            var product = item.purchaseProductProjection();

            double price = item.getPurchasePrice() / 100.0;
            int qty = item.getQuantity();
            double totalLine = price * qty;

            html.append("<tr>");

            html.append("<td>")
                    .append(escapeHtml(item.getProductName()))
                    .append("</td>");

            html.append("<td style='text-align:right;'>")
                    .append(qty)
                    .append("</td>");

            html.append("<td style='text-align:right;'>")
                    .append(String.format(locale, "%.2f €.", price))
                    .append("</td>");

            html.append("<td style='text-align:right;'>")
                    .append(String.format(locale, "%.2f €.", totalLine))
                    .append("</td>");

            html.append("</tr>");
        }

        return html.toString();
    }

    private String buildTotalsHtml(InvoicePurchaseProjection projection, Locale locale) {

        double productTotal = projection.getProductTotal() / 100.0;
        double shippingFee = projection.getShippingFee() / 100.0;
        double totalCost = projection.getTotalCost() / 100.0;

        StringBuilder html = new StringBuilder();

        html.append("<table class='totals-table' style='width:100%; margin-top:20px; border-top:1px solid #000;'>");

        html.append("<tr>")
                .append("<td style='text-align:right;'>Общо продукти:</td>")
                .append("<td style='width:120px; text-align:right;'>")
                .append(String.format(locale, "%.2f €.", productTotal))
                .append("</td>")
                .append("</tr>");

        html.append("<tr>")
                .append("<td style='text-align:right;'>Доставка:</td>")
                .append("<td style='text-align:right;'>")
                .append(String.format(locale, "%.2f €.", shippingFee))
                .append("</td>")
                .append("</tr>");

        html.append("<tr>")
                .append("<td style='text-align:right; font-weight:bold; font-size:16px; border-top:1px double #000; padding-top:10px;'>Сума за плащане:</td>")
                .append("<td style='text-align:right; font-weight:bold; font-size:16px; border-top:1px double #000; padding-top:10px;'>")
                .append(String.format(locale, "%.2f €.", totalCost))
                .append("</td>")
                .append("</tr>");

        html.append("</table>");

        return html.toString();
    }

    public String buildInvoicePdfString(InvoiceFullDTO invoice) {
        InvoicePurchaseProjection projection = invoice.invoicePurchaseProjection();

        String formattedDate = getFormattedDate(projection);

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

        html.append(buildProductRowsHtml(invoice, bgLocale));
        html.append("</table>");

        // --- TOTALS SECTION ---
        html.append(buildTotalsHtml(projection, bgLocale));

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

    @NonNull
    private static String getFormattedDate(InvoicePurchaseProjection projection) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                .withZone(ZoneId.systemDefault());
        return dateFormatter.format(projection.getDate());
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

    public String buildEmailHTML(InvoiceFullDTO invoice) {

        StringBuilder html = new StringBuilder();
        String formattedDate = getFormattedDate(invoice.invoicePurchaseProjection());

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<body style=\"font-family: Arial, sans-serif; font-size: 14px; color: #333;\">");

// HEADER
        html.append("<h2 style=\"color: #2e7d32;\">Успешна поръчка 🎉</h2>");

        html.append("<p>");
        html.append("Благодарим Ви за покупката от <b>")
                .append(escapeHtml("Agromag"))
                .append("</b>!");
        html.append("</p>");

        html.append("<p>");
        html.append("Вашата поръчка беше успешно обработена. ");

        html.append("</p>");
        html.append("<hr>");

// ORDER INFO
        html.append("<h3>Информация за поръчката</h3>");

        html.append("<p>");

        html.append("<b>Номер на поръчка:</b> ")
                .append(escapeHtml(invoice.invoicePurchaseProjection().getPurchaseCode()))
                .append("<br>");

        html.append("<b>Дата:</b> ")
                .append(formattedDate)
                .append("<br>");

        html.append("<b>Метод на плащане:</b> ")
                .append(escapeHtml(invoice.invoicePurchaseProjection().getPaymentMethod().getDisplayNameBg()));

        html.append("</p>");

        html.append("<hr>");

// PRODUCTS TABLE
        html.append("<h3>Продукти</h3>");

        html.append("<table style=\"width:100%; border-collapse: collapse;\">");

        html.append("<thead>");
        html.append("<tr>");

        html.append("<th align=\"left\">Продукт</th>");
        html.append("<th align=\"center\">Количество</th>");
        html.append("<th align=\"right\">Ед. цена</th>");
        html.append("<th align=\"right\">Общо</th>");

        html.append("</tr>");
        html.append("</thead>");

        html.append("<tbody>");
        html.append(buildProductRowsHtml(invoice, bgLocale));
        html.append("</tbody>");

        html.append("</table>");

        html.append("<hr>");

// TOTALS
        html.append(buildTotalsHtml(invoice.invoicePurchaseProjection(), bgLocale));

        html.append("<hr>");

// FOOTER
        html.append("<p>");
        html.append("Вашата фактура е прикачена към този имейл като PDF документ.");
        html.append("</p>");

        html.append("<p style=\"font-size: 12px; color: #777;\">");
        html.append("Ако имате въпроси, можете да се свържете с нашия екип за поддръжка.");
        html.append("</p>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}

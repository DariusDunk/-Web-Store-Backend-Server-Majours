package com.example.ecomerseapplication.Services;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    public byte[] generateInvoicePdf(String html) {
        try {
//            String html = buildInvoicePdfString(invoice);
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


}

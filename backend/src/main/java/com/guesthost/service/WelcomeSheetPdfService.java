package com.guesthost.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.guesthost.model.Property;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class WelcomeSheetPdfService {

    public byte[] generate(Property property, String portalUrl) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 48, 48, 56, 56);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28);
            Font sectionTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph(safe(property.getName(), "Welcome"), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8f);
            document.add(title);

            Paragraph address = new Paragraph(safe(property.getAddress(), ""), body);
            address.setAlignment(Element.ALIGN_CENTER);
            address.setSpacingAfter(24f);
            document.add(address);

            Paragraph wifiHeading = new Paragraph("Wi-Fi Access", sectionTitle);
            wifiHeading.setSpacingAfter(8f);
            document.add(wifiHeading);
            document.add(new Paragraph("Network: " + safe(property.getWifiName(), "-"), body));
            document.add(new Paragraph("Password: " + safe(property.getWifiPassword(), "-"), body));

            Paragraph qrHeading = new Paragraph("Guest Portal", sectionTitle);
            qrHeading.setSpacingBefore(28f);
            qrHeading.setSpacingAfter(8f);
            document.add(qrHeading);

            Image qrCode = Image.getInstance(buildQrCode(portalUrl));
            qrCode.scaleToFit(280, 280);
            qrCode.setAlignment(Element.ALIGN_CENTER);
            document.add(qrCode);

            Paragraph portalLink = new Paragraph(portalUrl, FontFactory.getFont(FontFactory.HELVETICA, 9));
            portalLink.setAlignment(Element.ALIGN_CENTER);
            portalLink.setSpacingBefore(8f);
            document.add(portalLink);
            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate welcome sheet PDF", ex);
        }
    }

    private byte[] buildQrCode(String content) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 640, 640);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    private String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return new String(Objects.requireNonNull(value).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }
}

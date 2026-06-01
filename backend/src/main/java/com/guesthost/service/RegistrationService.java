package com.guesthost.service;

import com.guesthost.dto.GuestRegistrationDto;
import com.guesthost.dto.GuestRegistrantDto;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.Booking;
import com.guesthost.model.GuestRegistration;
import com.guesthost.repository.BookingRepository;
import com.guesthost.repository.GuestRegistrationRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final GuestRegistrationRepository guestRegistrationRepository;
    private final BookingRepository bookingRepository;

    public List<GuestRegistration> saveRegistrations(GuestRegistrationDto dto) {
        Booking booking = bookingRepository.findById(dto.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + dto.getBookingId()));
        if (!booking.getPropertyId().equals(dto.getPropertyId())) {
            throw new IllegalArgumentException("Booking does not belong to the provided property");
        }

        List<GuestRegistration> saved = new ArrayList<>();
        for (GuestRegistrantDto guest : dto.getGuests()) {
            GuestRegistration registration = GuestRegistration.builder()
                    .bookingId(dto.getBookingId())
                    .propertyId(dto.getPropertyId())
                    .fullName(guest.getFullName())
                    .dateOfBirth(guest.getDateOfBirth())
                    .citizenship(guest.getCitizenship())
                    .documentNumber(guest.getDocumentNumber())
                    .documentType(guest.getDocumentType())
                    .documentIssuingCountry(guest.getDocumentIssuingCountry())
                    .documentExpiry(guest.getDocumentExpiry())
                    .gender(guest.getGender())
                    .address(guest.getAddress())
                    .ubyportStatus(GuestRegistration.UbyportStatus.PENDING)
                    .createdAt(Instant.now())
                    .build();
            saved.add(guestRegistrationRepository.save(registration));
        }

        booking.setRegistrationCompleted(true);
        bookingRepository.save(booking);
        return saved;
    }

    public List<GuestRegistration> getLogbook(String propertyId, LocalDate from, LocalDate to) {
        return guestRegistrationRepository.findAllByPropertyId(propertyId).stream()
                .filter(r -> isWithinRange(r.getCreatedAt(), from, to))
                .toList();
    }

    public byte[] exportCsv(String propertyId, LocalDate from, LocalDate to) {
        List<GuestRegistration> registrations = getFilteredRegistrations(propertyId, from, to);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.writeNext(new String[]{
                    "bookingId", "propertyId", "fullName", "dateOfBirth",
                    "citizenship", "documentNumber", "documentType", "documentIssuingCountry",
                    "documentExpiry", "gender", "address", "createdAt", "ubyportStatus"
            });
            for (GuestRegistration registration : registrations) {
                writer.writeNext(new String[]{
                        registration.getBookingId(),
                        registration.getPropertyId(),
                        registration.getFullName(),
                        registration.getDateOfBirth() == null ? "" : registration.getDateOfBirth().toString(),
                        registration.getCitizenship(),
                        registration.getDocumentNumber(),
                        registration.getDocumentType() == null ? "" : registration.getDocumentType(),
                        registration.getDocumentIssuingCountry() == null ? "" : registration.getDocumentIssuingCountry(),
                        registration.getDocumentExpiry() == null ? "" : registration.getDocumentExpiry().toString(),
                        registration.getGender() == null ? "" : registration.getGender(),
                        registration.getAddress(),
                        registration.getCreatedAt() == null ? "" : registration.getCreatedAt().toString(),
                        registration.getUbyportStatus() == null ? "" : registration.getUbyportStatus().name()
                });
            }
            writer.flush();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to export registrations CSV", ex);
        }
    }

    /**
     * Generates a Czech-authority-compliant Ubyport XML export for the given property and date range,
     * then marks all included records as SUBMITTED.
     */
    public byte[] exportUbyportXml(String propertyId, LocalDate from, LocalDate to) {
        List<GuestRegistration> registrations = getFilteredRegistrations(propertyId, from, to);

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setExpandEntityReferences(false);
            Document doc = dbf.newDocumentBuilder().newDocument();

            Element root = doc.createElementNS("urn:cz:mvcr:ubyport:v2", "UBYReport");
            root.setAttribute("ubytovnaId", propertyId);
            root.setAttribute("datumOd", from == null ? "" : from.toString());
            root.setAttribute("datumDo", to == null ? "" : to.toString());
            doc.appendChild(root);

            for (GuestRegistration r : registrations) {
                Booking booking = bookingRepository.findById(r.getBookingId()).orElse(null);
                String arrival = booking != null && booking.getCheckIn() != null
                        ? booking.getCheckIn().toString() : "";
                String departure = booking != null && booking.getCheckOut() != null
                        ? booking.getCheckOut().toString() : "";

                Element guest = doc.createElement("Ubytovany");
                addTextElement(doc, guest, "Jmeno", r.getFullName());
                addTextElement(doc, guest, "DatumNarozeni", r.getDateOfBirth() == null ? "" : r.getDateOfBirth().toString());
                addTextElement(doc, guest, "StatObcanstvi", r.getCitizenship());
                addTextElement(doc, guest, "Pohlavi", r.getGender() == null ? "" : r.getGender());
                addTextElement(doc, guest, "DruhDokladu", r.getDocumentType() == null ? "PASSPORT" : r.getDocumentType());
                addTextElement(doc, guest, "CisloDokladu", r.getDocumentNumber());
                addTextElement(doc, guest, "StatVydaniDokladu", r.getDocumentIssuingCountry() == null ? "" : r.getDocumentIssuingCountry());
                addTextElement(doc, guest, "PlatnostDokladu", r.getDocumentExpiry() == null ? "" : r.getDocumentExpiry().toString());
                addTextElement(doc, guest, "AdresaPobytu", r.getAddress());
                addTextElement(doc, guest, "DatumPrijezdu", arrival);
                addTextElement(doc, guest, "DatumOdjezdu", departure);
                root.appendChild(guest);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));

            registrations.forEach(r -> r.setUbyportStatus(GuestRegistration.UbyportStatus.SUBMITTED));
            if (!registrations.isEmpty()) {
                guestRegistrationRepository.saveAll(registrations);
            }
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate Ubyport XML export", ex);
        }
    }

    private void addTextElement(Document doc, Element parent, String tag, String value) {
        Element el = doc.createElement(tag);
        el.setTextContent(value == null ? "" : value);
        parent.appendChild(el);
    }

    /**
     * Simulates an electronic submission to the Ubyport system for all PENDING registrations in the
     * given property and date range. Marks each record SUBMITTED and returns a summary.
     */
    public Map<String, Object> submitUbyport(String propertyId, LocalDate from, LocalDate to) {
        List<GuestRegistration> pending = getFilteredRegistrations(propertyId, from, to).stream()
                .filter(r -> r.getUbyportStatus() == GuestRegistration.UbyportStatus.PENDING)
                .toList();

        pending.forEach(r -> r.setUbyportStatus(GuestRegistration.UbyportStatus.SUBMITTED));
        if (!pending.isEmpty()) {
            guestRegistrationRepository.saveAll(pending);
        }

        return Map.of(
                "status", "SUBMITTED",
                "submittedCount", pending.size(),
                "message", pending.isEmpty()
                        ? "No pending registrations found for the selected range."
                        : "Successfully submitted " + pending.size() + " guest registration(s) to Ubyport."
        );
    }

    private List<GuestRegistration> getFilteredRegistrations(String propertyId, LocalDate from, LocalDate to) {
        return guestRegistrationRepository.findAllByPropertyId(propertyId).stream()
                .filter(registration -> isWithinRange(registration.getCreatedAt(), from, to))
                .toList();
    }


    private boolean isWithinRange(Instant createdAt, LocalDate from, LocalDate to) {
        if (createdAt == null) {
            return false;
        }
        LocalDate createdDate = createdAt.atZone(ZoneOffset.UTC).toLocalDate();
        boolean afterFrom = from == null || !createdDate.isBefore(from);
        boolean beforeTo = to == null || !createdDate.isAfter(to);
        return afterFrom && beforeTo;
    }
}


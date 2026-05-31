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

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            writer.writeNext(new String[]{"bookingId", "propertyId", "fullName", "dateOfBirth", "citizenship", "documentNumber", "address", "createdAt"});
            for (GuestRegistration registration : registrations) {
                writer.writeNext(new String[]{
                        registration.getBookingId(),
                        registration.getPropertyId(),
                        registration.getFullName(),
                        registration.getDateOfBirth() == null ? "" : registration.getDateOfBirth().toString(),
                        registration.getCitizenship(),
                        registration.getDocumentNumber(),
                        registration.getAddress(),
                        registration.getCreatedAt() == null ? "" : registration.getCreatedAt().toString()
                });
            }

            public byte[] exportUbyportXml(String propertyId, LocalDate from, LocalDate to) {
                List<GuestRegistration> registrations = getFilteredRegistrations(propertyId, from, to);
                String body = registrations.stream()
                        .map(registration -> "  <registration>"
                                + "<bookingId>" + xmlEscape(registration.getBookingId()) + "</bookingId>"
                                + "<propertyId>" + xmlEscape(registration.getPropertyId()) + "</propertyId>"
                                + "<fullName>" + xmlEscape(registration.getFullName()) + "</fullName>"
                                + "<dateOfBirth>" + xmlEscape(registration.getDateOfBirth() == null ? "" : registration.getDateOfBirth().toString()) + "</dateOfBirth>"
                                + "<citizenship>" + xmlEscape(registration.getCitizenship()) + "</citizenship>"
                                + "<documentNumber>" + xmlEscape(registration.getDocumentNumber()) + "</documentNumber>"
                                + "<address>" + xmlEscape(registration.getAddress()) + "</address>"
                                + "<createdAt>" + xmlEscape(registration.getCreatedAt() == null ? "" : registration.getCreatedAt().toString()) + "</createdAt>"
                                + "</registration>")
                        .collect(Collectors.joining("\n"));
                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<ubyportExport>\n" + body + "\n</ubyportExport>\n";

                registrations.forEach(registration -> registration.setUbyportStatus(GuestRegistration.UbyportStatus.SUBMITTED));
                guestRegistrationRepository.saveAll(registrations);
                return xml.getBytes(StandardCharsets.US_ASCII);
            }

            private List<GuestRegistration> getFilteredRegistrations(String propertyId, LocalDate from, LocalDate to) {
                return guestRegistrationRepository.findAllByPropertyId(propertyId).stream()
                        .filter(registration -> isWithinRange(registration.getCreatedAt(), from, to))
                        .toList();
            }

            private String xmlEscape(String raw) {
                if (raw == null) {
                    return "";
                }
                return raw
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                        .replace("\"", "&quot;")
                        .replace("'", "&apos;");
            }
            writer.flush();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to export registrations CSV", ex);
        }
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

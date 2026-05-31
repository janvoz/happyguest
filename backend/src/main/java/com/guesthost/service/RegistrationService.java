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
                    .createdAt(Instant.now())
                    .build();
            saved.add(guestRegistrationRepository.save(registration));
        }

        booking.setRegistrationCompleted(true);
        bookingRepository.save(booking);
        return saved;
    }

    public byte[] exportCsv(String propertyId, LocalDate from, LocalDate to) {
        List<GuestRegistration> registrations = guestRegistrationRepository.findAllByPropertyId(propertyId).stream()
                .filter(registration -> isWithinRange(registration.getCreatedAt(), from, to))
                .toList();

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

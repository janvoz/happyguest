package com.guesthost.service;

import com.guesthost.dto.BookingDto;
import com.guesthost.dto.RegistrationInviteResponse;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.Booking;
import com.guesthost.model.Property;
import com.guesthost.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final PropertyService propertyService;
    private final NotificationService notificationService;

    public List<Booking> getBookingsForProperty(String hostEmail, String propertyId) {
        propertyService.getOwnedProperty(hostEmail, propertyId);
        return bookingRepository.findAllByPropertyId(propertyId);
    }

    public Booking getBooking(String hostEmail, String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        propertyService.getOwnedProperty(hostEmail, booking.getPropertyId());
        return booking;
    }

    public Booking createBooking(String hostEmail, BookingDto dto) {
        Property property = propertyService.getOwnedProperty(hostEmail, dto.getPropertyId());
        validateDates(dto.getCheckIn(), dto.getCheckOut());
        Booking booking = Booking.builder()
                .propertyId(property.getId())
                .guestName(dto.getGuestName())
                .guestEmail(dto.getGuestEmail())
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .doorCode(generateDoorCode())
                .preArrivalSent(false)
                .postDepartureSent(false)
                .registrationCompleted(false)
                .source(dto.getSource() == null || dto.getSource().isBlank() ? "MANUAL" : dto.getSource())
                .build();
        return bookingRepository.save(booking);
    }

    public Booking updateBooking(String hostEmail, String bookingId, BookingDto dto) {
        Booking existing = getBooking(hostEmail, bookingId);
        validateDates(dto.getCheckIn(), dto.getCheckOut());
        existing.setGuestName(dto.getGuestName());
        existing.setGuestEmail(dto.getGuestEmail());
        existing.setCheckIn(dto.getCheckIn());
        existing.setCheckOut(dto.getCheckOut());
        existing.setSource(dto.getSource() == null || dto.getSource().isBlank() ? existing.getSource() : dto.getSource());
        return bookingRepository.save(existing);
    }

    public void deleteBooking(String hostEmail, String bookingId) {
        Booking booking = getBooking(hostEmail, bookingId);
        bookingRepository.delete(booking);
    }

    public Booking createImportedBooking(Property property, String guestName, String guestEmail,
                                         LocalDateTime checkIn, LocalDateTime checkOut, String source) {
        validateDates(checkIn, checkOut);
        bookingRepository.findByPropertyIdAndCheckIn(property.getId(), checkIn)
                .ifPresent(existing -> { throw new IllegalStateException("Booking already exists for property and check-in"); });
        Booking booking = Booking.builder()
                .propertyId(property.getId())
                .guestName(guestName)
                .guestEmail(guestEmail)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .doorCode(generateDoorCode())
                .preArrivalSent(false)
                .postDepartureSent(false)
                .registrationCompleted(false)
                .source(source)
                .build();
        return bookingRepository.save(booking);
    }

    public String generateDoorCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private void validateDates(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
    }

    public RegistrationInviteResponse sendRegistrationInvite(String hostEmail, String bookingId) {
        Booking booking = getBooking(hostEmail, bookingId);

        if (booking.isRegistrationCompleted()) {
            return RegistrationInviteResponse.builder()
                    .bookingId(booking.getId())
                    .recipient(booking.getGuestEmail())
                    .sent(false)
                    .status("ALREADY_COMPLETED")
                    .message("Guest registration is already completed.")
                    .build();
        }

        if (booking.getGuestEmail() == null || booking.getGuestEmail().isBlank()) {
            return RegistrationInviteResponse.builder()
                    .bookingId(booking.getId())
                    .recipient("")
                    .sent(false)
                    .status("MISSING_EMAIL")
                    .message("Booking has no guest email.")
                    .build();
        }

        boolean sent = notificationService.sendRegistrationInvite(booking);
        if (sent) {
            booking.setPreArrivalSent(true);
            bookingRepository.save(booking);
        }

        return RegistrationInviteResponse.builder()
                .bookingId(booking.getId())
                .recipient(booking.getGuestEmail())
                .sent(sent)
                .status(sent ? "SENT" : "FAILED")
                .message(sent ? "Registration invite has been sent." : "Failed to send registration invite.")
                .build();
    }
}

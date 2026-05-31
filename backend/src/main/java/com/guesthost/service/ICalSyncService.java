package com.guesthost.service;

import com.guesthost.model.Booking;
import com.guesthost.model.Property;
import com.guesthost.repository.BookingRepository;
import com.guesthost.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ICalSyncService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final RestTemplate restTemplate;

    @Scheduled(fixedDelay = 900000)
    public void fetchAndSync() {
        for (Property property : propertyRepository.findAll()) {
            syncProperty(property);
        }
    }

    private void syncProperty(Property property) {
        List<Booking> existingBookings = bookingRepository.findAllByPropertyId(property.getId());
        for (String url : property.getIcalUrls()) {
            if (url == null || url.isBlank()) {
                continue;
            }
            try {
                String feed = restTemplate.getForObject(url, String.class);
                if (feed == null || feed.isBlank()) {
                    continue;
                }
                Calendar calendar = new CalendarBuilder().build(new StringReader(feed));
                for (Object component : calendar.getComponents(Component.VEVENT)) {
                    VEvent event = (VEvent) component;
                    LocalDateTime checkIn = toLocalDateTime(event.getStartDate().getDate());
                    LocalDateTime checkOut = toLocalDateTime(event.getEndDate().getDate());
                    if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
                        continue;
                    }
                    boolean duplicate = existingBookings.stream().anyMatch(booking ->
                            booking.getCheckIn().equals(checkIn) || overlaps(booking, checkIn, checkOut));
                    if (duplicate || bookingRepository.findByPropertyIdAndCheckIn(property.getId(), checkIn).isPresent()) {
                        continue;
                    }
                    Booking imported = bookingService.createImportedBooking(
                            property,
                            event.getSummary() == null ? "Imported Guest" : event.getSummary().getValue(),
                            event.getDescription() == null ? null : event.getDescription().getValue(),
                            checkIn,
                            checkOut,
                            "ICAL"
                    );
                    existingBookings.add(imported);
                }
            } catch (Exception ex) {
                log.warn("Failed to sync iCal for property {} from {}: {}", property.getId(), url, ex.getMessage());
            }
        }
    }

    private boolean overlaps(Booking booking, LocalDateTime checkIn, LocalDateTime checkOut) {
        return !(booking.getCheckOut().isEqual(checkIn) || booking.getCheckOut().isBefore(checkIn)
                || booking.getCheckIn().isEqual(checkOut) || booking.getCheckIn().isAfter(checkOut));
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}

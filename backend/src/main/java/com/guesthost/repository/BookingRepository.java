package com.guesthost.repository;

import com.guesthost.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findAllByPropertyId(String propertyId);
    List<Booking> findByPropertyIdAndCheckInBetween(String propertyId, LocalDateTime from, LocalDateTime to);

    @Query("{ 'isRegistrationCompleted': false, 'preArrivalSent': false }")
    List<Booking> findAllByIsRegistrationCompletedFalseAndPreArrivalSentFalse();

    List<Booking> findAllByPostDepartureSentFalse();
    Optional<Booking> findByPropertyIdAndCheckIn(String propertyId, LocalDateTime checkIn);
}

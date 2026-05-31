package com.guesthost.repository;

import com.guesthost.model.GuestRegistration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestRegistrationRepository extends MongoRepository<GuestRegistration, String> {
    List<GuestRegistration> findAllByPropertyId(String propertyId);
    List<GuestRegistration> findAllByBookingId(String bookingId);
}

package com.guesthost.repository;

import com.guesthost.model.MinibarOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MinibarOrderRepository extends MongoRepository<MinibarOrder, String> {
    List<MinibarOrder> findAllByPropertyId(String propertyId);
    List<MinibarOrder> findAllByBookingId(String bookingId);
    Optional<MinibarOrder> findByStripePaymentIntentId(String stripePaymentIntentId);
}

package com.guesthost.repository;

import com.guesthost.model.GuestMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestMessageRepository extends MongoRepository<GuestMessage, String> {
    List<GuestMessage> findAllByPropertyIdOrderByCreatedAtDesc(String propertyId);
    List<GuestMessage> findAllByPropertyIdAndReadFalse(String propertyId);
}

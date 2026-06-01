package com.guesthost.repository;

import com.guesthost.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findAllByBookingIdOrderByCreatedAtAsc(String bookingId);
}

package com.guesthost.repository;

import com.guesthost.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStripeAccountId(String stripeAccountId);
    Optional<User> findBySubscriptionId(String subscriptionId);
    boolean existsByEmail(String email);
}

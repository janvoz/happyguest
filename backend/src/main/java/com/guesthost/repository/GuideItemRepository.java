package com.guesthost.repository;

import com.guesthost.model.GuideItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuideItemRepository extends MongoRepository<GuideItem, String> {
    List<GuideItem> findAllByPropertyIdOrderByDisplayOrder(String propertyId);
    Optional<GuideItem> findByPropertyIdAndSlug(String propertyId, String slug);
}

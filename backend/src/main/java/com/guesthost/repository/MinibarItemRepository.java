package com.guesthost.repository;

import com.guesthost.model.MinibarItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MinibarItemRepository extends MongoRepository<MinibarItem, String> {
    List<MinibarItem> findAllByPropertyId(String propertyId);
}

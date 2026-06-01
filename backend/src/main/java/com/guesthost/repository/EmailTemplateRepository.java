package com.guesthost.repository;

import com.guesthost.model.EmailTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends MongoRepository<EmailTemplate, String> {
    List<EmailTemplate> findAllByHostIdAndPropertyId(String hostId, String propertyId);
    Optional<EmailTemplate> findFirstByHostIdAndPropertyIdAndTriggerType(String hostId, String propertyId, EmailTemplate.TriggerType triggerType);
}

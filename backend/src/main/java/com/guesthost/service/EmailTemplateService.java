package com.guesthost.service;

import com.guesthost.dto.EmailTemplateDto;
import com.guesthost.exception.ResourceNotFoundException;
import com.guesthost.model.EmailTemplate;
import com.guesthost.repository.EmailTemplateRepository;
import com.guesthost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final PropertyService propertyService;
    private final UserRepository userRepository;

    public List<EmailTemplate> getTemplatesForHost(String hostEmail, String propertyId) {
        propertyService.getOwnedProperty(hostEmail, propertyId);
        String hostId = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + hostEmail))
                .getId();
        return emailTemplateRepository.findAllByHostIdAndPropertyId(hostId, propertyId);
    }

    public EmailTemplate createTemplate(String hostEmail, EmailTemplateDto dto) {
        propertyService.getOwnedProperty(hostEmail, dto.getPropertyId());
        String hostId = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + hostEmail))
                .getId();
        EmailTemplate template = EmailTemplate.builder()
                .hostId(hostId)
                .propertyId(dto.getPropertyId())
                .triggerType(dto.getTriggerType())
                .subject(dto.getSubject())
                .htmlBody(dto.getHtmlBody())
                .build();
        return emailTemplateRepository.save(template);
    }

    public EmailTemplate updateTemplate(String hostEmail, String templateId, EmailTemplateDto dto) {
        EmailTemplate existing = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + templateId));
        propertyService.getOwnedProperty(hostEmail, existing.getPropertyId());
        existing.setTriggerType(dto.getTriggerType());
        existing.setSubject(dto.getSubject());
        existing.setHtmlBody(dto.getHtmlBody());
        return emailTemplateRepository.save(existing);
    }

    public void deleteTemplate(String hostEmail, String templateId) {
        EmailTemplate existing = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Email template not found: " + templateId));
        propertyService.getOwnedProperty(hostEmail, existing.getPropertyId());
        emailTemplateRepository.delete(existing);
    }
}

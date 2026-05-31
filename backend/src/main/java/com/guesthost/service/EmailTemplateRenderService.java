package com.guesthost.service;

import com.guesthost.model.Booking;
import com.guesthost.model.EmailTemplate;
import com.guesthost.model.Property;
import com.guesthost.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmailTemplateRenderService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-z_]+)\\s*}}");

    private final EmailTemplateRepository emailTemplateRepository;

    public RenderedTemplate resolveTemplate(String hostId, Property property, Booking booking, EmailTemplate.TriggerType triggerType, String fallbackSubject, String fallbackBody, String portalLink) {
        EmailTemplate configured = hostId == null
                ? null
                : emailTemplateRepository.findFirstByHostIdAndPropertyIdAndTriggerType(hostId, property.getId(), triggerType).orElse(null);

        String subject = configured != null && configured.getSubject() != null && !configured.getSubject().isBlank()
                ? configured.getSubject()
                : fallbackSubject;
        String htmlBody = configured != null && configured.getHtmlBody() != null && !configured.getHtmlBody().isBlank()
                ? configured.getHtmlBody()
                : fallbackBody;

        Map<String, String> values = new LinkedHashMap<>();
        values.put("guest_name", safe(booking.getGuestName()));
        values.put("check_in_date", booking.getCheckIn() == null ? "" : booking.getCheckIn().toLocalDate().toString());
        values.put("check_out_date", booking.getCheckOut() == null ? "" : booking.getCheckOut().toLocalDate().toString());
        values.put("door_code", safe(booking.getDoorCode()));
        values.put("portal_link", safe(portalLink));
        values.put("property_name", safe(property.getName()));

        return new RenderedTemplate(render(subject, values), render(htmlBody, values));
    }

    public String render(String text, Map<String, String> values) {
        if (text == null || text.isBlank()) {
            return "";
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = values.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public record RenderedTemplate(String subject, String htmlBody) {}
}

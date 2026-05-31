package com.guesthost.service;

import com.guesthost.model.Booking;
import com.guesthost.model.EmailTemplate;
import com.guesthost.model.Property;
import com.guesthost.repository.PropertyRepository;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final com.guesthost.repository.BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final JavaMailSender javaMailSender;
    private final EmailTemplateRenderService emailTemplateRenderService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.mail.sender:${spring.mail.username:}}")
    private String senderAddress;

    @Value("${app.mail.sender-name:Message from Staclo.host}")
    private String senderName;

    @Scheduled(fixedDelay = 300000)
    public void sendPreArrivalEmails() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        for (Booking booking : bookingRepository.findAllByIsRegistrationCompletedFalseAndPreArrivalSentFalse()) {
            if (booking.getCheckIn() != null && booking.getCheckIn().toLocalDate().isEqual(targetDate)) {
                if (sendRegistrationInvite(booking)) {
                    booking.setPreArrivalSent(true);
                    bookingRepository.save(booking);
                }
            }
        }
    }

    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    public void sendPostDepartureEmails() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        for (Booking booking : bookingRepository.findAllByPostDepartureSentFalse()) {
            if (booking.getCheckOut() != null && booking.getCheckOut().isBefore(threshold) && hasGuestEmail(booking)) {
                String link = frontendUrl + "/guest/review/" + booking.getId();
                String fallbackBody = "<html><body><h2>Thank you for your stay!</h2>"
                        + "<p>We would love to hear about your experience.</p>"
                        + "<p><a href=\"" + link + "\">Leave a Review</a></p></body></html>";
                Property property = propertyRepository.findById(booking.getPropertyId()).orElse(null);

                String subject = "Thank you for your stay!";
                String htmlBody = fallbackBody;
                if (property != null) {
                    EmailTemplateRenderService.RenderedTemplate rendered = emailTemplateRenderService.resolveTemplate(
                            property.getHostId(),
                            property,
                            booking,
                            EmailTemplate.TriggerType.POST_DEPARTURE,
                            subject,
                            fallbackBody,
                            link
                    );
                    subject = rendered.subject();
                    htmlBody = rendered.htmlBody();
                }

                if (sendEmail(booking.getGuestEmail(), subject, htmlBody)) {
                    booking.setPostDepartureSent(true);
                    bookingRepository.save(booking);
                }
            }
        }
    }

    public boolean sendRegistrationInvite(Booking booking) {
        if (!hasGuestEmail(booking) || booking.isRegistrationCompleted()) {
            return false;
        }

        String link = frontendUrl + "/guest/portal/" + booking.getPropertyId() + "?bookingId=" + booking.getId();
        Property property = propertyRepository.findById(booking.getPropertyId()).orElse(null);
        String fallbackBody = "<html><body><h2>Action Required: Complete Registration to Get Your Door Code</h2>"
                + "<p>Please complete your registration before arrival to receive your door code.</p>"
                + "<p><a href=\"" + link + "\">Complete Registration</a></p></body></html>";
        if (property == null) {
            return sendEmail(booking.getGuestEmail(), "Action Required: Complete Registration to Get Your Door Code", fallbackBody);
        }

        EmailTemplateRenderService.RenderedTemplate rendered = emailTemplateRenderService.resolveTemplate(
                property.getHostId(),
                property,
                booking,
                EmailTemplate.TriggerType.PRE_ARRIVAL,
                "Action Required: Complete Registration to Get Your Door Code",
                fallbackBody,
                link
        );
        return sendEmail(booking.getGuestEmail(), rendered.subject(), rendered.htmlBody());
    }

    private boolean sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            String configuredFrom = (senderAddress != null && !senderAddress.isBlank()) ? senderAddress : fromAddress;
            if (configuredFrom != null && !configuredFrom.isBlank()) {
                helper.setFrom(new InternetAddress(configuredFrom, senderName));
            }
            javaMailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.warn("Failed to send email to {}: {}", to, ex.getMessage());
            return false;
        }
    }

    private boolean hasGuestEmail(Booking booking) {
        return booking.getGuestEmail() != null && !booking.getGuestEmail().isBlank();
    }
}

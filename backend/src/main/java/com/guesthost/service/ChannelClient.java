package com.guesthost.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChannelClient {

    List<ChannelReservation> fetchActiveReservations(String propertyId);

    Optional<ChannelReservation> parseWebhookReservation(Map<String, Object> payload);

    void pushAvailabilityUpdate(ChannelAvailabilityUpdate update);
}

package com.guesthost.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AirbnbClient {

    private final RestClient restClient = RestClient.create();

    @Value("${app.airbnb.base-url:}")
    private String baseUrl;

    @Value("${app.airbnb.api-token:}")
    private String apiToken;

    public List<RemoteReservation> fetchReservations(String propertyId) {
        if (baseUrl.isBlank() || apiToken.isBlank()) {
            return List.of();
        }
        List<Map<String, Object>> payload = restClient.get()
                .uri(baseUrl + "/reservations?propertyId=" + propertyId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(List.class);
        if (payload == null) {
            return List.of();
        }
        List<RemoteReservation> reservations = new ArrayList<>();
        for (Map<String, Object> item : payload) {
            reservations.add(RemoteReservation.from(item, "AIRBNB"));
        }
        return reservations;
    }

    public record RemoteReservation(
            String externalId,
            String guestName,
            String guestEmail,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            String source,
            String totalPrice) {
        static RemoteReservation from(Map<String, Object> payload, String source) {
            return new RemoteReservation(
                    String.valueOf(payload.getOrDefault("id", "")),
                    String.valueOf(payload.getOrDefault("guestName", "Guest")),
                    String.valueOf(payload.getOrDefault("guestEmail", "")),
                    LocalDateTime.parse(String.valueOf(payload.get("checkIn"))),
                    LocalDateTime.parse(String.valueOf(payload.get("checkOut"))),
                    source,
                    String.valueOf(payload.getOrDefault("totalPrice", "0"))
            );
        }
    }
}

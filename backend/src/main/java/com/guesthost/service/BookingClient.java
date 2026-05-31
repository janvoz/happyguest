package com.guesthost.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BookingClient {

    private final RestClient restClient = RestClient.create();

    @Value("${app.booking.base-url:}")
    private String baseUrl;

    @Value("${app.booking.api-token:}")
    private String apiToken;

    public List<AirbnbClient.RemoteReservation> fetchReservations(String propertyId) {
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
        List<AirbnbClient.RemoteReservation> reservations = new ArrayList<>();
        for (Map<String, Object> item : payload) {
            reservations.add(AirbnbClient.RemoteReservation.from(item, "BOOKING"));
        }
        return reservations;
    }
}

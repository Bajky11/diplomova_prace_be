package com.friends.friends.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MapboxProxyService {

    private final RestTemplate restTemplate;

    @Value("${mapbox.api.url}")
    private String baseUrl;

    @Value("${mapbox.api.access.accessToken}")
    private String accessToken;

    @Value("${mapbox.api.access.sessionToken}")
    private String sessionToken;

    public MapboxProxyService() {
        this.restTemplate = new RestTemplate();
    }

    public String suggest(String suggestion, boolean advancedSearch) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/suggest")
                .queryParam("q", suggestion)
                .queryParam("access_token", accessToken)
                .queryParam("limit", 3)
                .queryParam("language", "cs")
                .queryParam("session_token", sessionToken)
                .queryParam("country", "cz");

        if (advancedSearch) {
            builder.queryParam("types", "city");
        }

        String url = builder.toUriString();

        return restTemplate.getForObject(url, String.class);
    }

    public String retrieve(String mapboxId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/retrieve/" + mapboxId)
                .queryParam("access_token", accessToken)
                .queryParam("language", "cs")
                .queryParam("session_token", sessionToken)
                .toUriString();

        return restTemplate.getForObject(url, String.class);
    }
}
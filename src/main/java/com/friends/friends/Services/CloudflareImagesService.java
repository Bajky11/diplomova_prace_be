package com.friends.friends.Services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class CloudflareImagesService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${cloudflare.images.account-id}")
    private String accountId;

    @Value("${cloudflare.images.api-token}")
    private String apiToken;


    public String createDirectUploadUrl() {
        String url = "https://api.cloudflare.com/client/v4/accounts/" + accountId + "/images/v2/direct_upload";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(new LinkedMultiValueMap<>(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);


        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                return root.path("result").path("uploadURL").asText(); // jen ta URL
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Cloudflare response", e);
            }
        }

        throw new RuntimeException("Cloudflare request failed: " + response.getStatusCode());
    }

    public boolean deleteImage(String imageId) {
        //curl --request DELETE https://api.cloudflare.com/client/v4/accounts/{account_id}/images/v1/{image_id} \
        //--header "Authorization: Bearer <API_TOKEN>"
        String url = "https://api.cloudflare.com/client/v4/accounts/"+ accountId +"/images/v1/" + imageId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
        return response.getStatusCode() == HttpStatus.OK;
    }
}

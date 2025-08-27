package com.friends.friends.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrevoEmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.apiKey}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    public void sendEmail(String toEmail, String subject, String htmlContent, String textFallback) {
        // Brevo payload
        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("email", senderEmail, "name", senderName));
        body.put("to", List.of(Map.of("email", toEmail)));
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);
        body.put("textContent", textFallback);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> resp = restTemplate.exchange(BREVO_ENDPOINT, HttpMethod.POST, entity, String.class);

            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to send email via Brevo. HTTP status: " + resp.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            // chybové hlášky v angličtině
            String response = e.getResponseBodyAsString();
            throw new RuntimeException("Brevo API error: " + e.getStatusCode() + " - " + response);
        } catch (Exception e) {
            throw new RuntimeException("Internal email error", e);
        }
    }

    public void sendPasswordRequestResetEmail(String toEmail, String token) {
        String subject = "Password reset";
        String resetInfo = "Use the token below to reset your password in the app.";
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:520px">
                  <h2>Password reset</h2>
                  <p>%s</p>
                  <p><strong>Token:</strong></p>
                  <pre style="background:#f6f6f6;padding:12px;border-radius:6px">%s</pre>
                  <p>This token expires in 15 minutes. If you did not request this, you can ignore this email.</p>
                </div>
                """.formatted(resetInfo, token);

        this.sendEmail(toEmail, subject, html, "fallback");
    }
}


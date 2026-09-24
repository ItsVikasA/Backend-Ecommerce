package com.example.authapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Value("${imagekit.private.key}")
    private String privateKey;

    @GetMapping("/imagekit")
    public ResponseEntity<String> testImageKit() {
        try {
            String url = "https://api.imagekit.io/v1/files?limit=5&skip=0";
            
            HttpHeaders headers = new HttpHeaders();
            String auth = privateKey + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            return ResponseEntity.ok("SUCCESS! Status: " + response.getStatusCode() + "\nBody length: " + (response.getBody() != null ? response.getBody().length() : 0) + "\nBody: " + response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("ERROR: " + e.getMessage() + "\nCause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
        }
    }
}

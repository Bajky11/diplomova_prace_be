package com.friends.friends.Controller;

import com.friends.friends.Services.CloudflareImagesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cloudflare-images")
public class CloudflareImagesController {

    private final CloudflareImagesService service;

    public CloudflareImagesController(CloudflareImagesService service) {
        this.service = service;
    }

    @GetMapping("/direct-upload")
    public ResponseEntity<String> createDirectUpload() {
        String url = service.createDirectUploadUrl();
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDirectUpload(@PathVariable String id) {
        boolean result = service.deleteImage(id);
        return result ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}

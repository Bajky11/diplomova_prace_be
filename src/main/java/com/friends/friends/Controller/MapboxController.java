package com.friends.friends.Controller;

import com.friends.friends.Services.MapboxProxyService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mapbox")
public class MapboxController {

    MapboxProxyService mapboxProxyService;

    MapboxController(MapboxProxyService mapboxProxyService) {
        this.mapboxProxyService = mapboxProxyService;
    }

    @GetMapping(value = "/suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSuggestions(
            @RequestParam String suggestion,
            @RequestParam boolean advancedSearch
    ) {
        String json = mapboxProxyService.suggest(suggestion, advancedSearch);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }


    @GetMapping(value = "/retrieve/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getRetrieved(@PathVariable String id) {
        String json = mapboxProxyService.retrieve(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}

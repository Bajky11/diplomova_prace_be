package com.friends.friends.Controller;

import com.friends.friends.Entity.Location.LocationCreateOrUpdateDto;
import com.friends.friends.Entity.Location.LocationDto;
import com.friends.friends.Services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/location")
public class LocationController {
    
    @Autowired
    private LocationService locationService;
    
    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Long id) {
        LocationDto dto = locationService.getLocationDtoById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(Authentication authentication, @Valid @RequestBody LocationCreateOrUpdateDto createLocationDto) {
        LocationDto dto = locationService.createLocation(createLocationDto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationCreateOrUpdateDto createLocationDto) {
        LocationDto dto = locationService.updateLocation(id, createLocationDto);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}

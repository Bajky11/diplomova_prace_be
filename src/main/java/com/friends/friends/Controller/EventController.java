package com.friends.friends.Controller;

import com.friends.friends.Entity.Account.AccountDto;
import com.friends.friends.Entity.Event.*;
import com.friends.friends.Services.AccountService;
import com.friends.friends.Services.EventService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private final EventService eventService;
    private final AccountService accountService;

    @Autowired
    public EventController(EventService eventService, AccountService accountService) {
        this.eventService = eventService;
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<EventDto>> getEventsByAccountId(Authentication authentication) {
        return ResponseEntity.ok(eventService.getEventsByAccountId(authentication));
    }


    @PostMapping
    public ResponseEntity<EventDto> createEvent(Authentication authentication, @Valid @RequestBody EventCreateOrUpdateDto dto) {
        return ResponseEntity.ok(eventService.createEvent(dto, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @Valid @RequestBody EventUpdateDto dto) {
        eventService.updateEvent(id, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<List<EventDto>> getEventsByPreferences(Authentication authentication, @Valid @RequestBody EventPreferencesFilterDto filterDto) {
        String email = authentication.getName();
        AccountDto account = accountService.getCurrentUserDto(email);
        return ResponseEntity.ok(eventService.getEventsByPreferences(account.getId(), filterDto));
    }

    @GetMapping("/filtered")
    public ResponseEntity<List<EventDto>> getFilteredEventsWithCursor(Authentication authentication, @Valid @RequestBody EventPreferencesFilterDto filterDto, @RequestParam(required = false) Long cursorEventId) {
        return ResponseEntity.ok(eventService.getFilteredEventsWithCursor(authentication, filterDto, cursorEventId, 2));
    }

    @GetMapping("/events-in-bounds")
    public ResponseEntity<List<EventMapProjection>> getEventsInBounds(
            @RequestParam double west,
            @RequestParam double south,
            @RequestParam double east,
            @RequestParam double north
    ) {
        List<EventMapProjection> events = eventService.getEventsInBounds(west, south, east, north);
        return ResponseEntity.ok(events);
    }
}

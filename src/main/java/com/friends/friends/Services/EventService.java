package com.friends.friends.Services;

import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Event.*;
import com.friends.friends.Entity.Location.Location;
import com.friends.friends.Exception.Account.AccountNotFoundException;
import com.friends.friends.Exception.Event.EventNotFoundException;
import com.friends.friends.Exception.Category.CategoryNotFoundException;
import com.friends.friends.Exception.Currency.CurrencyNotFoundException;
import com.friends.friends.Repository.*;
import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.Currency.Currency;
import com.friends.friends.Repository.EventRepository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;
    private final AccountService accountService;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final CurrencyService currencyService;

    @Autowired
    public EventService(EventRepository eventRepository, AccountRepository accountRepository, CategoryRepository categoryRepository, CurrencyRepository currencyRepository, AccountService accountService, LocationRepository locationRepository, LocationService locationService, CurrencyService currencyService) {
        this.eventRepository = eventRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.currencyRepository = currencyRepository;
        this.accountService = accountService;
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.currencyService = currencyService;
    }

    public List<EventDto> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream().map(Event::toDto).collect(Collectors.toList());
    }

    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id).orElseThrow(EventNotFoundException::new);
        return event.toDto();
    }

    @Transactional
    public EventDto createEvent(EventCreateOrUpdateDto dto, Authentication authentication) {
        Account account = accountService.getCurrentUser(authentication.getName());

        Event event = new Event();
        event.setImageUrl(dto.imageUrl);
        event.setTitle(dto.title);
        event.setDescription(dto.description);
        event.setStartTime(dto.startTime);
        event.setEndTime(dto.endTime);
        event.setPrice(dto.price);
        event.setCreatedBy(account);
        event.setSelectedCategories(dto.selectedCategoryIds.stream().map(id -> categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new)).collect(Collectors.toList()));
        Location location = locationService.getLocationById(dto.locationId);
        event.setHappeningOn(location);

        if (dto.getCurrencyId() != null) {
            Currency currency = currencyRepository.findById(dto.currencyId).orElseThrow(CurrencyNotFoundException::new);
            event.setCurrency(currency);
        }

        Event saved = eventRepository.save(event);
        return saved.toDto();
    }

    @Transactional
    public void updateEvent(Long id, EventUpdateDto dto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(EventNotFoundException::new);

        if (dto.imageUrl != null) {
            event.setImageUrl(dto.imageUrl);
        }

        if (dto.title != null) {
            event.setTitle(dto.title);
        }

        if (dto.description != null) {
            event.setDescription(dto.description);
        }

        if (dto.startTime != null) {
            event.setStartTime(dto.startTime);
        }

        if (dto.endTime != null) {
            event.setEndTime(dto.endTime);
        }

        if (dto.price != null) {
            event.setPrice(dto.price);
        }

        if (dto.selectedCategoryIds != null && !dto.selectedCategoryIds.isEmpty()) {
            event.setSelectedCategories(dto.selectedCategoryIds.stream().map(catId -> categoryRepository.findById(catId).orElseThrow(CategoryNotFoundException::new)).collect(Collectors.toList()));
        }

        if (dto.currencyId != null) {
            Currency currency = currencyRepository.findById(dto.currencyId).orElseThrow(CurrencyNotFoundException::new);
            event.setCurrency(currency);
        }

        if (dto.locationDto != null) {
            Location location = event.getHappeningOn();
            location.setAddress(dto.locationDto.getAddress());
            location.setLatitude(dto.locationDto.getLatitude());
            location.setLongitude(dto.locationDto.getLongitude());
            locationRepository.save(location);
         }

        eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException();
        }
        eventRepository.deleteById(id);
    }

    public List<EventDto> getEventsByAccountId(Authentication authentication) {
        Account account = accountService.getCurrentUser(authentication.getName());
        List<Event> events = eventRepository.findByCreatedByIdOrderByStartTime(account.getId());
        return events.stream().map(Event::toDto).collect(Collectors.toList());
    }

    private boolean filterByCategories(Event event, List<Long> CategoryIds) {
        if (!CategoryIds.isEmpty()) {
            return event.getSelectedCategories() != null && event.getSelectedCategories().stream().anyMatch(cat -> CategoryIds.contains(cat.getId()));
        }
        return true;
    }


    private boolean filterByTitle(Event event, String titleFilter) {
        return titleFilter == null || titleFilter.isBlank() || event.getTitle().toLowerCase().contains(titleFilter.toLowerCase());
    }

    private boolean filterByTime(Event event, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime now) {
        LocalDate eventDay = event.getStartTime() != null ? event.getStartTime().toLocalDate() : null;
        LocalDate nowDay = now.toLocalDate();
        LocalDate startDay = startDate != null ? startDate.toLocalDate() : null;
        LocalDate endDay = endDate != null ? endDate.toLocalDate() : null;
        // Události z minulosti nevracíme
        if (eventDay == null) {
            // Pokud není startTime, považujeme za "ode dneška do budoucna"
            return true;
        }
        if (eventDay.isBefore(nowDay)) {
            return false;
        }
        if (startDay == null) {
            if (endDay == null) {
                return true;
            } else {
                return !eventDay.isAfter(endDay);
            }
        } else {
            if (endDay == null) {
                return !eventDay.isBefore(startDay);
            } else {
                return !eventDay.isBefore(startDay) && !eventDay.isAfter(endDay);
            }
        }
    }

    public List<EventDto> getEventsByPreferences(Long accountId, EventPreferencesFilterDto filterDto) {
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        LocalDateTime now = java.time.LocalDate.now().atStartOfDay();

        // Get
        List<Event> allEvents = List.of();
        if (filterDto.getLatitude() != null && filterDto.getLongitude() != null && filterDto.getRadius() != null) {
            allEvents = eventRepository.findByLocationWithinRadius(filterDto.getLatitude(), filterDto.getLongitude(), filterDto.getRadius());
        } else {
            allEvents = eventRepository.findAll();
        }

        Integer page = filterDto.getPage();
        Integer size = filterDto.getSize();

        List<Long> categoryIds;

        if (filterDto.getCategories() == null) {
            // filter events by account favorite categories
            categoryIds = account.getFavoriteCategories().stream().map(cat -> cat.getId()).toList();

        } else if (!filterDto.getCategories().isEmpty()) {
            // filter events by send filter categories
            categoryIds = filterDto.getCategories();
        } else {
            // when favoriteCategoryIds = [] return all events
            categoryIds = List.of();
        }


        List<EventDto> filtered = allEvents.stream().filter(event -> filterByCategories(event, categoryIds)).filter(event -> filterByTitle(event, filterDto.getTitleFilter())).filter(event -> filterByTime(event, filterDto.getStartDate(), filterDto.getEndDate(), now)).map(Event::toDto).collect(Collectors.toList());

        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 20;
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, filtered.size());
        if (fromIndex >= filtered.size()) {
            return List.of();
        }
        return filtered.subList(fromIndex, toIndex);
    }

    public List<EventDto> getFilteredEventsWithCursor(Authentication authentication, EventPreferencesFilterDto filterDto, Long cursorEventId, int limit) {
        Account account = accountService.getCurrentUser(authentication.getName());

        // Pokud není předán kurzor, použij první dostupný event v budoucnu
        Long localCursorId;
        if (cursorEventId == null) {

            // When filter is set on event in past we include them
            Event firstEvent;
            if (filterDto.getStartDate() != null) {
                firstEvent = eventRepository.findFirstEventByDate(filterDto.getStartDate());
            } else {
                firstEvent = eventRepository.findFirstTodayEvent();
            }

            if (firstEvent == null) {
                return List.of(); // žádné události → prázdný seznam
            }

            localCursorId = firstEvent.getId();
        } else {
            localCursorId = cursorEventId;
        }

        // Získej událost odpovídající kurzoru
        Event cursorEvent = eventRepository.findById(localCursorId).orElseThrow(EventNotFoundException::new);
        LocalDateTime lastEventStartTime = cursorEvent.getStartTime();

        // Kategorie: buď oblíbené uživatele, nebo konkrétní výběr
        Long[] localCategoryIds;
        if (filterDto.getCategories() == null) {
            // null → filtrujeme podle oblíbených kategorií uživatele
            localCategoryIds = account.getFavoriteCategories().stream().map(Category::getId).toArray(Long[]::new);
        } else {
            // [] → žádná filtrace, [1,2] → filtrace
            localCategoryIds = filterDto.getCategories().toArray(new Long[0]);
        }

        // Tady je klíčové: pokud původní cursor byl null, do funkce pošli null, jinak validní id
        // [?] funkce filtruje event s cursorId, ale jelikož ten byl null, tak je to výchozí request, kde chceme i ten první event.
        Long cursorIdParam = cursorEventId == null ? null : localCursorId;

        // Volání vlastní funkce v DB přes repository
        return eventRepository.findFilteredEvents(cursorIdParam, lastEventStartTime, limit, filterDto.getTitleFilter(), localCategoryIds, filterDto.getStartDate(), filterDto.getEndDate(), filterDto.getLatitude(), filterDto.getLongitude(), filterDto.getRadius()).stream().map(Event::toDto).toList();
    }


    public List<EventMapProjection> getEventsInBounds(double west, double south, double east, double north) {
        List<EventMapProjection> events = eventRepository.getEventsInBounds(west, south, east, north);
        return events;
    }

    public List<Event> getEventsInRadius(double latitude, double longitude, int radius, int limit) {
        return eventRepository.getEventsInRadius(latitude, longitude, radius, limit);
    }

}

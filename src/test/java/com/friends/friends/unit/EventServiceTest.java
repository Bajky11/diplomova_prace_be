package com.friends.friends.unit;

import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Currency.Currency;
import com.friends.friends.Entity.Event.*;
import com.friends.friends.Entity.Location.Location;
import com.friends.friends.Entity.Location.LocationCreateOrUpdateDto;

import com.friends.friends.Exception.Account.AccountNotFoundException;
import com.friends.friends.Exception.Category.CategoryNotFoundException;
import com.friends.friends.Exception.Currency.CurrencyNotFoundException;
import com.friends.friends.Exception.Event.EventNotFoundException;
import com.friends.friends.Repository.*;
import com.friends.friends.Repository.EventRepository.EventRepository;
import com.friends.friends.Services.AccountService;
import com.friends.friends.Services.CurrencyService;
import com.friends.friends.Services.EventService;
import com.friends.friends.Services.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock EventRepository eventRepository;
    @Mock AccountRepository accountRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock CurrencyRepository currencyRepository;
    @Mock
    AccountService accountService;
    @Mock LocationRepository locationRepository;
    @Mock
    LocationService locationService;
    @Mock
    CurrencyService currencyService;

    @InjectMocks
    EventService service;

    @Captor ArgumentCaptor<Event> eventCaptor;
    @Captor ArgumentCaptor<Long> longCaptor;

    Authentication auth;

    @BeforeEach
    void setUp() {
        auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("user@example.com");
    }

    // ---------- getAllEvents / getEventById ----------

    @Test
    void getAllEvents_mapsToDto() {
        Event e1 = spy(new Event()); EventDto d1 = new EventDto();
        Event e2 = spy(new Event()); EventDto d2 = new EventDto();
        doReturn(d1).when(e1).toDto();
        doReturn(d2).when(e2).toDto();
        when(eventRepository.findAll()).thenReturn(List.of(e1, e2));

        List<EventDto> out = service.getAllEvents();

        assertEquals(List.of(d1, d2), out);
        verify(eventRepository).findAll();
        verify(e1).toDto(); verify(e2).toDto();
    }

    @Test
    void getEventById_ok() {
        Event e = spy(new Event());
        EventDto dto = new EventDto();
        doReturn(dto).when(e).toDto();
        when(eventRepository.findById(10L)).thenReturn(Optional.of(e));

        EventDto out = service.getEventById(10L);

        assertSame(dto, out);
        verify(eventRepository).findById(10L);
        verify(e).toDto();
    }

    @Test
    void getEventById_notFound_throws() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () -> service.getEventById(999L));
    }

    // ---------- createEvent ----------

    @Test
    void createEvent_success_withoutCurrency() {
        // Arrange
        Account acc = new Account(); acc.setId(1L); acc.setEmail("user@example.com");
        when(accountService.getCurrentUser("user@example.com")).thenReturn(acc);

        Category c1 = new Category(); c1.setId(100L);
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(c1));

        Location loc = new Location();
        when(locationService.getLocationById(77L)).thenReturn(loc);

        EventDto dtoOut = new EventDto();
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            Event spyE = spy(e);
            when(spyE.toDto()).thenReturn(dtoOut);
            return spyE;
        });

        EventCreateOrUpdateDto dto = new EventCreateOrUpdateDto();
        dto.imageUrl = "img";
        dto.title = "Gig";
        dto.description = "desc";
        dto.startTime = LocalDateTime.now().plusDays(1);
        dto.endTime = dto.startTime.plusHours(2);
        dto.price = BigDecimal.valueOf(150.0);
        dto.selectedCategoryIds = List.of(100L);
        dto.locationId = 77L;
        dto.currencyId = null;

        // Act
        EventDto out = service.createEvent(dto, auth);

        // Assert
        assertSame(dtoOut, out);
        verify(accountService).getCurrentUser("user@example.com");
        verify(categoryRepository).findById(100L);
        verify(locationService).getLocationById(77L);
        verify(eventRepository).save(eventCaptor.capture());
        Event saved = eventCaptor.getValue();
        assertEquals("img", saved.getImageUrl());
        assertEquals("Gig", saved.getTitle());
        assertEquals("desc", saved.getDescription());
        assertEquals(BigDecimal.valueOf(150.0), saved.getPrice());
        assertEquals(acc, saved.getCreatedBy());
        assertEquals(loc, saved.getHappeningOn());
        assertEquals(List.of(c1), saved.getSelectedCategories());
        assertNull(saved.getCurrency());
        verifyNoInteractions(currencyRepository);
    }

    @Test
    void createEvent_success_withCurrency() {
        Account acc = new Account(); acc.setId(2L); acc.setEmail("user@example.com");
        when(accountService.getCurrentUser("user@example.com")).thenReturn(acc);

        Category c1 = new Category(); c1.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c1));

        Location loc = new Location();
        when(locationService.getLocationById(10L)).thenReturn(loc);

        Currency cur = new Currency(); cur.setId(5L);
        when(currencyRepository.findById(5L)).thenReturn(Optional.of(cur));

        EventDto outDto = new EventDto();
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
            Event spyE = spy((Event) inv.getArgument(0));
            when(spyE.toDto()).thenReturn(outDto);
            return spyE;
        });

        EventCreateOrUpdateDto dto = new EventCreateOrUpdateDto();
        dto.title = "Party";
        dto.selectedCategoryIds = List.of(1L);
        dto.locationId = 10L;
        dto.currencyId = 5L;

        EventDto out = service.createEvent(dto, auth);

        assertSame(outDto, out);
        verify(currencyRepository).findById(5L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_categoryNotFound_throws() {
        when(accountService.getCurrentUser("user@example.com")).thenReturn(new Account());
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        EventCreateOrUpdateDto dto = new EventCreateOrUpdateDto();
        dto.selectedCategoryIds = List.of(999L);
        dto.locationId = 1L;

        assertThrows(CategoryNotFoundException.class, () -> service.createEvent(dto, auth));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createEvent_currencyNotFound_throws() {
        when(accountService.getCurrentUser("user@example.com")).thenReturn(new Account());
        Category c = new Category(); c.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c));
        when(locationService.getLocationById(1L)).thenReturn(new Location());
        when(currencyRepository.findById(500L)).thenReturn(Optional.empty());

        EventCreateOrUpdateDto dto = new EventCreateOrUpdateDto();
        dto.selectedCategoryIds = List.of(1L);
        dto.locationId = 1L;
        dto.currencyId = 500L;

        assertThrows(CurrencyNotFoundException.class, () -> service.createEvent(dto, auth));
        verify(eventRepository, never()).save(any());
    }

    // ---------- updateEvent ----------

    @Test
    void updateEvent_partialFields_andLocationUpdate() {
        Event existing = new Event();
        existing.setId(7L);
        existing.setImageUrl("old.png");
        existing.setTitle("Old");
        existing.setDescription("OldD");
        existing.setPrice(BigDecimal.valueOf(10.0));
        Location loc = new Location();
        loc.setAddress("A");
        existing.setHappeningOn(loc);

        when(eventRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Currency cur = new Currency(); cur.setId(9L);
        when(currencyRepository.findById(9L)).thenReturn(Optional.of(cur));

        EventUpdateDto dto = new EventUpdateDto();
        dto.imageUrl = "new.png";
        dto.title = "New";
        dto.description = "NewD";
        dto.price = BigDecimal.valueOf(55.5);
        dto.currencyId = 9L;
        dto.locationDto = new LocationCreateOrUpdateDto();
        dto.locationDto.setAddress("B");
        dto.locationDto.setLatitude(50.1);
        dto.locationDto.setLongitude(14.5);

        service.updateEvent(7L, dto);

        assertEquals("new.png", existing.getImageUrl());
        assertEquals("New", existing.getTitle());
        assertEquals("NewD", existing.getDescription());
        assertEquals(BigDecimal.valueOf(55.5), existing.getPrice());
        assertEquals(cur, existing.getCurrency());
        assertEquals("B", existing.getHappeningOn().getAddress());
        assertEquals(50.1, existing.getHappeningOn().getLatitude());
        assertEquals(14.5, existing.getHappeningOn().getLongitude());
        verify(locationRepository).save(loc);
        verify(eventRepository).save(existing);
    }

    @Test
    void updateEvent_replaceCategories_whenProvided() {
        Event existing = new Event(); existing.setId(1L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        Category c1 = new Category(); c1.setId(1L);
        Category c2 = new Category(); c2.setId(2L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(c1));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(c2));

        EventUpdateDto dto = new EventUpdateDto();
        dto.selectedCategoryIds = List.of(1L, 2L);

        service.updateEvent(1L, dto);

        assertEquals(List.of(c1, c2), existing.getSelectedCategories());
        verify(eventRepository).save(existing);
    }

    @Test
    void updateEvent_notFound_throws() {
        when(eventRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () -> service.updateEvent(404L, new EventUpdateDto()));
    }

    // ---------- deleteEvent ----------

    @Test
    void deleteEvent_ok() {
        when(eventRepository.existsById(5L)).thenReturn(true);
        service.deleteEvent(5L);
        verify(eventRepository).deleteById(5L);
    }

    @Test
    void deleteEvent_notFound_throws() {
        when(eventRepository.existsById(5L)).thenReturn(false);
        assertThrows(EventNotFoundException.class, () -> service.deleteEvent(5L));
        verify(eventRepository, never()).deleteById(anyLong());
    }

    // ---------- getEventsByAccountId ----------

    @Test
    void getEventsByAccountId_delegatesAndMaps() {
        Account acc = new Account(); acc.setId(77L); acc.setEmail("user@example.com");
        when(accountService.getCurrentUser("user@example.com")).thenReturn(acc);

        Event e1 = spy(new Event()); EventDto d1 = new EventDto();
        Event e2 = spy(new Event()); EventDto d2 = new EventDto();
        when(e1.toDto()).thenReturn(d1);
        when(e2.toDto()).thenReturn(d2);

        when(eventRepository.findByCreatedByIdOrderByStartTime(77L)).thenReturn(List.of(e1, e2));

        List<EventDto> out = service.getEventsByAccountId(auth);

        assertEquals(List.of(d1, d2), out);
        verify(eventRepository).findByCreatedByIdOrderByStartTime(77L);
    }

    // ---------- getEventsByPreferences (filtrace/paging) ----------

    @Test
    void getEventsByPreferences_categoriesNull_usesFavorites_andFilters_title_time_pagination() {
        // Account s oblíbenými kategoriemi [1]
        Account acc = new Account();
        Category fav = new Category(); fav.setId(1L);
        acc.setFavoriteCategories(List.of(fav));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(acc));

        // Eventy
        Event e1 = spy(eventWith("Rock", LocalDate.now().plusDays(1))); // title match + future + has cat 1
        e1.setSelectedCategories(List.of(fav));
        when(e1.toDto()).thenReturn(new EventDto());

        Event e2 = spy(eventWith("Pop", LocalDate.now().minusDays(1))); // past → odfiltrován
        e2.setSelectedCategories(List.of(fav));

        Event e3 = spy(eventWith("rock festival", LocalDate.now().plusDays(2))); // title match + future + cat 1
        e3.setSelectedCategories(List.of(fav));
        when(e3.toDto()).thenReturn(new EventDto());

        when(eventRepository.findAll()).thenReturn(List.of(e1, e2, e3));

        EventPreferencesFilterDto f = new EventPreferencesFilterDto();
        f.setTitleFilter("rock");
        f.setPage(0);
        f.setSize(1); // záměrně malý page size pro ověření stránkování
        f.setCategories(null); // → použij oblíbené

        List<EventDto> page0 = service.getEventsByPreferences(10L, f);
        assertEquals(1, page0.size()); // jeden z [e1, e3]

        f.setPage(1);
        List<EventDto> page1 = service.getEventsByPreferences(10L, f);
        assertEquals(1, page1.size()); // druhý z [e1, e3]

        f.setPage(2);
        List<EventDto> page2 = service.getEventsByPreferences(10L, f);
        assertTrue(page2.isEmpty()); // nic dalšího
    }

    @Test
    void getEventsByPreferences_geoBranch_callsWithinRadius() {
        Account acc = new Account(); acc.setFavoriteCategories(List.of());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));

        Event e = spy(eventWith("Title", LocalDate.now().plusDays(1)));
        when(e.toDto()).thenReturn(new EventDto());
        when(eventRepository.findByLocationWithinRadius(50.0, 14.0, 1000))
                .thenReturn(List.of(e));

        EventPreferencesFilterDto f = new EventPreferencesFilterDto();
        f.setLatitude(50.0); f.setLongitude(14.0); f.setRadius(1000.0);
        f.setCategories(List.of()); // [] → bez filtru kategorií

        List<EventDto> out = service.getEventsByPreferences(1L, f);

        assertEquals(1, out.size());
        verify(eventRepository).findByLocationWithinRadius(50.0, 14.0, 1000);
        verify(eventRepository, never()).findAll();
    }

    @Test
    void getEventsByPreferences_categoriesCustom_appliesFilter() {
        Account acc = new Account();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));

        Category c1 = new Category(); c1.setId(1L);
        Category c2 = new Category(); c2.setId(2L);

        Event e1 = spy(eventWith("A", LocalDate.now().plusDays(1)));
        e1.setSelectedCategories(List.of(c1));

        Event e2 = spy(eventWith("B", LocalDate.now().plusDays(1)));
        e2.setSelectedCategories(List.of(c2));
        when(e2.toDto()).thenReturn(new EventDto());

        when(eventRepository.findAll()).thenReturn(List.of(e1, e2));

        EventPreferencesFilterDto f = new EventPreferencesFilterDto();
        f.setCategories(List.of(2L)); // chceme jen c2

        List<EventDto> out = service.getEventsByPreferences(1L, f);

        assertEquals(1, out.size());
        // očekáváme jen e2
    }

    @Test
    void getEventsByPreferences_accountNotFound_throws() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,
                () -> service.getEventsByPreferences(999L, new EventPreferencesFilterDto()));
    }

    // ---------- getFilteredEventsWithCursor ----------

    @Test
    void getFilteredEventsWithCursor_cursorNull_usesFirstEventAndNullParam_forNullCursor() {
        Account acc = new Account();
        Category fav = new Category(); fav.setId(5L);
        acc.setFavoriteCategories(List.of(fav));
        when(accountService.getCurrentUser("user@example.com")).thenReturn(acc);

        // když není startDate, použije findFirstTodayEvent()
        Event first = new Event(); first.setId(111L); first.setStartTime(LocalDate.now().atStartOfDay().plusHours(10));
        when(eventRepository.findFirstTodayEvent()).thenReturn(first);

        when(eventRepository.findById(111L)).thenReturn(Optional.of(first));

        Event resultEvent = spy(new Event());
        EventDto dto = new EventDto();
        when(resultEvent.toDto()).thenReturn(dto);
        when(eventRepository.findFilteredEvents(
                isNull(), // cursorIdParam == null, protože původně byl cursorEventId null
                eq(first.getStartTime()),
                eq(20),
                isNull(), // titleFilter null
                aryEq(new Long[]{5L}), // oblíbené kategorie
                isNull(), isNull(),
                isNull(), isNull(), isNull()
        )).thenReturn(List.of(resultEvent));

        List<EventDto> out = service.getFilteredEventsWithCursor(auth, new EventPreferencesFilterDto(), null, 20);

        assertEquals(1, out.size());
        assertSame(dto, out.get(0));
    }

    @Test
    void getFilteredEventsWithCursor_withGivenCursor_andCustomCategoriesAndDate() {
        Account acc = new Account();
        when(accountService.getCurrentUser("user@example.com")).thenReturn(acc);

        Event cursor = new Event(); cursor.setId(77L); cursor.setStartTime(LocalDateTime.now().plusDays(3));
        when(eventRepository.findById(77L)).thenReturn(Optional.of(cursor));

        EventPreferencesFilterDto f = new EventPreferencesFilterDto();
        f.setCategories(List.of(1L, 2L));
        f.setStartDate(LocalDateTime.now().minusDays(1));
        f.setEndDate(LocalDateTime.now().plusDays(10));
        f.setTitleFilter("rock");
        f.setLatitude(50.0); f.setLongitude(14.0); f.setRadius(5000.0);

        Event result = spy(new Event());
        EventDto dto = new EventDto();
        when(result.toDto()).thenReturn(dto);

        when(eventRepository.findFilteredEvents(
                eq(77L), // teď už NENÍ null (cursor dodán)
                eq(cursor.getStartTime()),
                eq(5),
                eq("rock"),
                aryEq(new Long[]{1L, 2L}),
                eq(f.getStartDate()), eq(f.getEndDate()),
                eq(50.0), eq(14.0), eq(5000.0)
        )).thenReturn(List.of(result));

        List<EventDto> out = service.getFilteredEventsWithCursor(auth, f, 77L, 5);

        assertEquals(1, out.size());
        assertSame(dto, out.get(0));
    }

    @Test
    void getFilteredEventsWithCursor_cursorNull_butStartDateProvided_usesFindFirstEventByDate() {
        Account acc = new Account(); when(accountService.getCurrentUser("user@example.com")).thenReturn(acc);

        EventPreferencesFilterDto f = new EventPreferencesFilterDto();
        LocalDateTime sd = LocalDate.now().plusDays(2).atStartOfDay();
        f.setStartDate(sd);

        Event first = new Event(); first.setId(5L); first.setStartTime(sd.plusHours(8));
        when(eventRepository.findFirstEventByDate(sd)).thenReturn(first);
        when(eventRepository.findById(5L)).thenReturn(Optional.of(first));

        when(eventRepository.findFilteredEvents(
                isNull(), eq(first.getStartTime()), eq(10),
                isNull(), aryEq(new Long[]{}),
                eq(sd), isNull(),
                isNull(), isNull(), isNull()
        )).thenReturn(List.of());

        List<EventDto> out = service.getFilteredEventsWithCursor(auth, f, null, 10);
        assertNotNull(out);
    }

    // ---------- map/bounds & radius delegace ----------

    @Test
    void getEventsInBounds_delegates() {
        List<EventMapProjection> proj = List.of(mock(EventMapProjection.class));
        when(eventRepository.getEventsInBounds(12, 34, 56, 78)).thenReturn(proj);
        List<EventMapProjection> out = service.getEventsInBounds(12, 34, 56, 78);
        assertSame(proj, out);
        verify(eventRepository).getEventsInBounds(12, 34, 56, 78);
    }

    @Test
    void getEventsInRadius_delegates() {
        List<Event> events = List.of(new Event());
        when(eventRepository.getEventsInRadius(50.0, 14.0, 1000, 20)).thenReturn(events);
        List<Event> out = service.getEventsInRadius(50.0, 14.0, 1000, 20);
        assertSame(events, out);
        verify(eventRepository).getEventsInRadius(50.0, 14.0, 1000, 20);
    }

    // ---------- helpers ----------

    private Event eventWith(String title, LocalDate day) {
        Event e = new Event();
        e.setTitle(title);
        e.setStartTime(day == null ? null : day.atStartOfDay().plusHours(12));
        return e;
    }
}
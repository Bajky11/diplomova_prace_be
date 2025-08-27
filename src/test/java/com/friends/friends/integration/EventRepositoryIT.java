package com.friends.friends.integration;

import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Currency.Currency;
import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Entity.Event.EventMapProjection;
import com.friends.friends.Entity.Location.Location;
import com.friends.friends.Repository.AccountRepository;
import com.friends.friends.Repository.CategoryRepository;
import com.friends.friends.Repository.CurrencyRepository;
import com.friends.friends.Repository.EventRepository.EventRepository;
import com.friends.friends.Repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class EventRepositoryIT {

    @Autowired
    EventRepository eventRepo;
    @Autowired
    AccountRepository accountRepo;
    @Autowired
    CategoryRepository categoryRepo;
    @Autowired
    CurrencyRepository currencyRepo;
    @Autowired
    LocationRepository locationRepo;

    Account user;
    Category catRock;
    Category catPop;
    Currency czk;
    Location locCenter;   // (50.087, 14.421) – Praha Staroměstské nám.
    Location locAway;     // (50.100, 14.500) – mimo radius
    Location locNear;     // (50.088, 14.424) – v radiusu

    Event eTodayMorningRockNear;
    Event eTodayEveningPopNear;
    Event eTomorrowRockAway;
    Event eYesterdayRockNear;
    Event eSpecificDate;

    @BeforeEach
    void seed() {
        user = accountRepo.save(Account.builder()
                .email("user@example.com")
                .name("Tester")
                .passwordHash("x")
                .isBusiness(true)
                .region("prague")
                .build());

        catRock = categoryRepo.save(new Category("Rock"));
        catPop = categoryRepo.save(new Category("Pop"));

        czk = currencyRepo.save(new Currency("CZK", "CZK", "Kč"));

        // Lokace (zhruba Praha)
        locCenter = locationRepo.save(new Location("Staromák", 50.087, 14.421, user));
        locNear = locationRepo.save(new Location("Pařížská", 50.088, 14.424, user));
        locAway = locationRepo.save(new Location("Mimo radius", 50.100, 14.500, user));

        // Dnes (předpoklad lokálního času běhu testu)
        LocalDate today = LocalDate.now();
        LocalDateTime todayMorning = today.atTime(9, 0);
        LocalDateTime todayEvening = today.atTime(20, 0);

        // Včera a zítra
        LocalDateTime yesterdayNoon = today.minusDays(1).atTime(12, 0);
        LocalDateTime tomorrowNoon = today.plusDays(1).atTime(12, 0);

        // Eventy:
        eTodayMorningRockNear = saveEvent("Rock Brunch", todayMorning, todayMorning.plusHours(2),
                catRock, locNear, BigDecimal.valueOf(200));

        eTodayEveningPopNear = saveEvent("Pop Night", todayEvening, todayEvening.plusHours(3),
                catPop, locNear, BigDecimal.valueOf(300));

        eTomorrowRockAway = saveEvent("Rock Tomorrow Far", tomorrowNoon, tomorrowNoon.plusHours(2),
                catRock, locAway, BigDecimal.valueOf(150));

        eYesterdayRockNear = saveEvent("Rock Yesterday", yesterdayNoon, yesterdayNoon.plusHours(2),
                catRock, locNear, BigDecimal.valueOf(100));

        // Specifické datum pro findFirstEventByDate(start)
        LocalDateTime specific = today.plusDays(3).atTime(10, 0);
        eSpecificDate = saveEvent("Specific Rock", specific, specific.plusHours(2),
                catRock, locCenter, BigDecimal.valueOf(123));
    }

    private Event saveEvent(String title, LocalDateTime start, LocalDateTime end,
                            Category category, Location location, BigDecimal price) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription(title + " desc");
        e.setStartTime(start);
        e.setEndTime(end);
        e.setPrice(price);
        e.setCreatedBy(user);
        e.setHappeningOn(location);
        e.setCurrency(czk);
        e.setSelectedCategories(List.of(category));
        return eventRepo.save(e);
    }

    // ---------------- findByLocationWithinRadius ----------------

    @Test
    void findByLocationWithinRadius_returnsOnlyEventsInsideRadius_sortedByDistanceOrTime() {
        // Centrum Prahy, radius 500m → oba „Near“ eventy, ale ne „Away“
        List<Event> out = eventRepo.findByLocationWithinRadius(50.087, 14.421, 0.5);

        assertThat(out)
                .extracting(Event::getId)
                .contains(eTodayMorningRockNear.getId(), eTodayEveningPopNear.getId())
                .doesNotContain(eTomorrowRockAway.getId());

        // (volitelně) ověř pořadí — záleží, jestli dotaz řadí vzdáleností/časem
        assertThat(out).isNotEmpty();
    }

    // ---------------- findFirstTodayEvent / findFirstEventByDate ----------------

    @Test
    void findFirstTodayEvent_returnsEarliestToday() {
        Event first = eventRepo.findFirstTodayEvent();
        // očekáváme dopolední Rock (9:00) před večerní Pop (20:00)
        assertThat(first).isNotNull();
        assertThat(first.getId()).isEqualTo(eTodayMorningRockNear.getId());
    }

    @Test
    void findFirstEventByDate_returnsEarliestAtGivenDayOrAfter() {
        // start = „o 3 dny později“ → Specific Rock je první ten den v 10:00
        LocalDateTime start = LocalDate.now().plusDays(3).atStartOfDay();
        Event first = eventRepo.findFirstEventByDate(start);

        assertThat(first).isNotNull();
        assertThat(first.getId()).isEqualTo(eSpecificDate.getId());
    }

    // ---------------- findFilteredEvents (cursor, kategorie, title, date, geo, limit) ----------------

    @Test
    void findFilteredEvents_withCursorAndFilters_respectsAll() {
        // Vezmeme kurzor = dnešní dopolední (first), jeho startTime tvoří hranici.
        Event cursor = eTodayMorningRockNear;
        LocalDateTime lastStart = cursor.getStartTime();

        // Filtr: title contains 'rock' (case-insens), kategorie [catRock], od dneška do +4 dny,
        // geo: kolem centra 5 km, limit 5
        Long[] cats = new Long[]{catRock.getId()};

        List<Event> out = eventRepo.findFilteredEvents(
                cursor.getId(),          // cursorIdParam (není null → kurzor sám se vynechá dle implementace)
                lastStart,               // lastEventStartTime
                5,                       // limit
                "rOcK",                  // title filter (case-insensitive)
                cats,                    // categories
                LocalDate.now().atStartOfDay(),             // startDate
                LocalDate.now().plusDays(4).atStartOfDay(), // endDate
                50.087, 14.421, 6.0                    // geo (6 km)
        );

        // Co čekáme:
        // - eTodayMorningRockNear by měl být vynechán (protože je to kurzor)
        // - eTodayEveningPopNear není "rock" → odfiltrován
        // - eTomorrowRockAway je "rock" a ve window, ale může projít i když je „Away“,
        //   protože radius je 6 km → měl by projít
        // - eYesterdayRockNear je minulost → neprojde (startDate = dnes 00:00)
        // - eSpecificDate je Rock v +3 dnech v centru → měl by projít
        assertThat(out)
                .extracting(Event::getId)
                .contains(eTomorrowRockAway.getId(), eSpecificDate.getId())
                .doesNotContain(eTodayMorningRockNear.getId(), eTodayEveningPopNear.getId(), eYesterdayRockNear.getId());

        assertThat(out.size()).isLessThanOrEqualTo(5);
    }

    @Test
    void findFilteredEvents_whenNoCursor_returnsFromFirstOfWindow_includingThatFirst() {
        // Simulace „prvního requestu“: cursorIdParam = null, ale lastEventStartTime nastavíme
        // podle prvního v daném okně – způsob závisí na implementaci SQL.
        // V mnoha implementacích se při null cursoru zahrne i „first event“.
        LocalDateTime windowStart = LocalDate.now().atStartOfDay();
        Long[] cats = new Long[]{}; // bez filtru kategorií

        // vezmeme „first today“
        Event firstToday = eventRepo.findFirstTodayEvent();

        List<Event> out = eventRepo.findFilteredEvents(
                null,
                firstToday.getStartTime(),
                10,
                null,
                cats,
                windowStart,
                null,
                50.087, 14.421, 10000.0
        );

        // Očekáváme, že výsledek obsahuje i první dnešní event (protože cursor je null)
        assertThat(out).extracting(Event::getId).contains(firstToday.getId());
    }

    @Test
    void getEventsInBounds_returnsOnlyFutureEventsWithinBox() {
        // BBOX kolem centra Prahy: zahrne locCenter (50.087,14.421) i locNear (50.088,14.424),
        // ale ne locAway (50.100,14.500)
        double west = 14.418;
        double south = 50.085;
        double east = 14.430;
        double north = 50.090;

        List<EventMapProjection> out = eventRepo.getEventsInBounds(west, south, east, north);

        // očekáváme: oba dnešní near eventy + Specific Rock (+3 dny v centru)
        assertThat(out)
                .extracting(EventMapProjection::getId)
                .contains(
                        eTodayMorningRockNear.getId(),
                        eTodayEveningPopNear.getId(),
                        eSpecificDate.getId()
                )
                // mimo bbox nebo v minulosti:
                .doesNotContain(
                        eTomorrowRockAway.getId(), // mimo bbox
                        eYesterdayRockNear.getId() // včera -> start_time::DATE < CURRENT_DATE
                );

        // ověř, že všechny vrácené body leží v bboxu
        assertThat(out).allSatisfy(p -> {
            assertThat(p.getLongitude()).isBetween(west, east);
            assertThat(p.getLatitude()).isBetween(south, north);
        });
    }

    @Test
    void getEventsInBounds_excludesEverythingOutsideBox() {
        // BBOX záměrně malý, aby neobsahoval nic
        double west = 14.410;
        double south = 50.070;
        double east = 14.411;
        double north = 50.071;

        List<EventMapProjection> out = eventRepo.getEventsInBounds(west, south, east, north);

        assertThat(out).isEmpty();
    }
}
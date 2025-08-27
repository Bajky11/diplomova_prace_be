package com.friends.friends.Scheduled;

import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Entity.Event.EventPreferencesFilterDto;
import com.friends.friends.Repository.DeviceTokenRepository.DeviceTokenJdbcRepository;
import com.friends.friends.Repository.DeviceTokenRepository.DeviceTokenRepository;
import com.friends.friends.Services.AccountService;
import com.friends.friends.Services.EventService;
import com.friends.friends.Services.NotificationService;
import com.friends.friends.classes.Region;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotificationsScheduler {

    private final NotificationService notificationService;
    private final DeviceTokenJdbcRepository deviceTokenJdbcRepository;
    private final AccountService accountService;
    private final EventService eventService;

    List<Region> regions = Arrays.asList(
            new Region("Hlavní město Praha", 50.0755, 14.4378, 20),
            new Region("Středočeský kraj", 49.9387, 14.1928, 65),
            new Region("Jihočeský kraj", 49.1435, 14.1744, 60),
            new Region("Plzeňský kraj", 49.7475, 13.3776, 55),
            new Region("Karlovarský kraj", 50.2317, 12.8717, 35),
            new Region("Ústecký kraj", 50.6114, 13.7879, 45),
            new Region("Liberecký kraj", 50.6591, 14.7636, 35),
            new Region("Královéhradecký kraj", 50.3476, 15.7977, 40),
            new Region("Pardubický kraj", 49.9466, 15.8681, 45),
            new Region("Vysočina", 49.3988, 15.5870, 50),
            new Region("Jihomoravský kraj", 49.2020, 16.6110, 60),
            new Region("Olomoucký kraj", 49.6586, 17.3465, 45),
            new Region("Zlínský kraj", 49.2235, 17.6747, 35),
            new Region("Moravskoslezský kraj", 49.8209, 18.2625, 55)
    );

    public NotificationsScheduler(NotificationService notificationService, DeviceTokenRepository deviceTokenRepository, DeviceTokenJdbcRepository deviceTokenJdbcRepository, AccountService accountService, EventService eventService) {
        this.notificationService = notificationService;
        this.deviceTokenJdbcRepository = deviceTokenJdbcRepository;
        this.accountService = accountService;
        this.eventService = eventService;
    }

    @Transactional(readOnly = true)
    @Scheduled(cron = "0 0 9 * * ?")
    public void everyDayAtTen() {
        Map<Long, List<String>> tokensByAccount = deviceTokenJdbcRepository.getTokensGroupedByAccount();
        for (Map.Entry<Long, List<String>> entry : tokensByAccount.entrySet()) {
            Long accountId = entry.getKey();
            List<String> tokens = entry.getValue();

            Map<String, String> messageData = constructNotificationMessageForAccount(accountId);

            // Not generated any message
            if (messageData == null) continue;

            for (String token : tokens) {
                notificationService.sendNotification(token, messageData.get("title"), messageData.get("content"));
            }
        }
    }

    public Map<String, String> constructNotificationMessageForAccount(Long accountId) {
        Account account = accountService.getAccountById(accountId);

        // Account does not have region saved in database
        if (account.getRegion() == null) return null;

        List<Region> filtered = regions.stream().filter((region -> region.getName().equals(account.getRegion()))).toList();

        // Wrong region saved in database
        if (filtered.isEmpty()) return null;

        Region region = filtered.get(0);

        List<Event> events = eventService.getEventsInRadius(region.getLatitude(), region.getLongitude(), 100, 10);

        System.out.println(events.size());

        String title = "Co se bude konat v " + region.getName() + "?";
        StringBuilder content = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd. MM.")
                .withLocale(Locale.forLanguageTag("cs-CZ"))
                .withZone(ZoneId.of("Europe/Prague"));



        for (Event event : events) {
            String categories = event.getSelectedCategories().stream()
                    .limit(2) // vezme max 2 kategorie
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));

            if (!categories.isEmpty()) { // pokud nemá aspoň jednu kategorii, nevypisuj nic
                content.append(event.getStartTime().format(formatter))
                        .append(" - ")
                        .append(event.getTitle())
                        .append(" [")
                        .append(categories)
                        .append("]")
                        .append("\n");
            }
        }

        if (content.toString().isEmpty()) return null;

        return Map.of("title", title, "content", content.toString());
    }
}

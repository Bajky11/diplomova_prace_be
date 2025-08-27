package com.friends.friends.Entity.Event;

import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Currency.Currency;
import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.Location.Location;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    // ATTRIBUTES

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column()
    private String imageUrl;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();


    // RELATIONSHIPS

    // FK pointing to one row in Account table (account_id FK)
    @ManyToOne
    @JoinColumn(name = "created_by_account_id", nullable = false)
    private Account createdBy;

    // FK pointing to one row in Account table (account_id FK)
    @ManyToOne
    @JoinColumn(name = "happening_on_location_id", nullable = false)
    private Location happeningOn;

    // JoinTable event_selected_categories is created connecting Event (event_id) and Category (category_id) table
    @ManyToMany
    @JoinTable(
        name = "event_selected_categories",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id",
        nullable = false)
    )
    private List<Category> selectedCategories;

    // FK pointing to one row in Currency table (currency_id FK)
    @ManyToOne
    @JoinColumn(name = "currency_id")
    private Currency currency;

    // Join table "account_favorite_events" is created in Account table using attribute "favoriteEvents"
    @ManyToMany(mappedBy = "favoriteEvents")
    private List<Account> favoriteBy = new ArrayList<>();


    // FUNCTIONS

    public Event(String title, LocalDateTime startTime, LocalDateTime endTime, Location happeningOn) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.happeningOn = happeningOn;
    }

    public EventDto toDto() {
        return EventDto.builder()
                .id(this.getId())
                .imageUrl(this.getImageUrl())
                .title(this.getTitle())
                .description(this.getDescription())
                .startTime(this.getStartTime())
                .endTime(this.getEndTime())
                .price(this.getPrice())
                .currency(this.getCurrency() != null ? this.getCurrency().toDto() : null)
                .createdAt(this.getCreatedAt())
                .createdBy(this.getCreatedBy() != null ? this.getCreatedBy().toDto() : null)
                .selectedCategories(this.getSelectedCategories() != null ? this.getSelectedCategories().stream().map(cat -> cat.toDto()).toList() : null)
                .favoriteByCount(this.getFavoriteBy() != null ? this.getFavoriteBy().size() : 0)
                .happeningOn(this.getHappeningOn())
                .build();
    }
}

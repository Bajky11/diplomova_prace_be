package com.friends.friends.Entity.Account;

import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.DeviceToken.DeviceToken;
import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Entity.Location.Location;
import com.friends.friends.Entity.Notification.Notification;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Account {

    // ATTRIBUTES

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column()
    private String name;

    @Column(name = "image_url")
    private String imageUrl;

    private String region;

    @Column(name = "is_business", nullable = false)
    private Boolean isBusiness;

    @Column(unique = true, nullable = true)
    private String ico = null;


    // BUSINESS ATTRIBUTES

    @Column(columnDefinition = "TEXT")
    private String businessDescription;

    @Type(JsonType.class)
    @Column(name = "info_json", columnDefinition = "jsonb")
    private Map<String, Object> businessInfo;


    // RELATIONSHIPS

    // JoinTable account_favorite_events is created connecting Account (account_id) and Category (category_id) table
    @ManyToMany
    @JoinTable(
            name = "account_favorite_events",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> favoriteEvents = new ArrayList<>();

    // JoinTable account_favorite_categories is created connecting Account (account_id) and Category (category_id) table
    @ManyToMany
    @JoinTable(
            name = "account_favorite_categories",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> favoriteCategories = new ArrayList<>();

    // In Notification table is (recipient_account_id FK) pointing to Account table
    @OneToMany(mappedBy = "recipient")
    private List<Notification> receivedNotifications = new ArrayList<>();

    // In DeviceToken table is (recipient_account_id FK) pointing to Account table
    @OneToMany(mappedBy = "tokenOwner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeviceToken> deviceTokens = new ArrayList<>();



    // BUSINESS RELATIONS

    // In Event table is (account_id FK) pointing to Account table
    @OneToMany(mappedBy = "createdBy")
    private List<Event> createdEvents = new ArrayList<>();

    // In Location table is (account_id FK) pointing to Account table
    @OneToOne(mappedBy = "headquartersOf")
    private Location headquartesLocation;

    // In Location table is (account_id FK) pointing to Account table
    @OneToMany(mappedBy = "createdBy")
    private List<Location> createdLocations = new ArrayList<>();


    // FUNCTIONS

    @PrePersist
    public void prePersist() {
        if (favoriteCategories == null) favoriteCategories = new ArrayList<>();
        if (favoriteEvents == null) favoriteEvents = new ArrayList<>();
        if (receivedNotifications == null) receivedNotifications = new ArrayList<>();
        if (createdEvents == null) createdEvents = new ArrayList<>();
        if (createdLocations == null) createdLocations = new ArrayList<>();
    }

    public Account(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public AccountDto toDto() {
        return AccountDto.builder()
                .id(this.getId())
                .email(this.getEmail())
                .name(this.getName())
                .imageUrl(this.getImageUrl())
                .region(this.getRegion())
                .isBusiness(this.getIsBusiness())
                .build();
    }
}

package com.friends.friends.Entity.Location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.Event.Event;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    //ATTRIBUTES

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;


    // RELATIONSHIPS

    // FK pointing to one row in Account table (account_id FK)
    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "headquarters_of_account_id")
    private Account headquartersOf;

    // FK pointing to one row in Account table (account_id FK)
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "created_by_account_id", nullable = false)
    private Account createdBy;

    // In Event table is (account_id FK) pointing to Account table
    @OneToMany(mappedBy = "happeningOn")
    @JsonIgnore
    private List<Event> hostingEvent = new ArrayList<>();


    // FUNCTIONS

    public Location(String address, Double latitude, Double longitude, Account createdBy) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdBy = createdBy;
    }

    public LocationDto toDto() {
        return LocationDto.builder()
                .id(this.getId())
                .address(this.getAddress())
                .latitude(this.getLatitude())
                .longitude(this.getLongitude())
                .build();
    }

    public static Location fromDto(LocationCreateOrUpdateDto dto, Account headquartersOf, Account createdBy) {
        return Location.builder()
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .headquartersOf(headquartersOf)
                .createdBy(createdBy)
                .build();
    }
}

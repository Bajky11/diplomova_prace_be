package com.friends.friends.Entity.Event;

import com.friends.friends.Entity.Category.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class EventPreferencesFilterDto {
    private String titleFilter;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> categories;
    private Double latitude;
    private Double longitude;
    private Double radius;

    private Integer page = 0;
    private Integer size = 100;

    public EventPreferencesFilterDto(String titleFilter, LocalDateTime startDate, LocalDateTime endDate, List<Long> categories) {
        this.titleFilter = titleFilter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.categories = categories;
    }

    // lat, log is from MapBox
    // radius is user defined in km
    public EventPreferencesFilterDto(String titleFilter, Double latitude, Double longitude, Double radius) {
        this.titleFilter = titleFilter;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }
}

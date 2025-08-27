package com.friends.friends.Entity.Event;

import com.friends.friends.Entity.Location.LocationCreateOrUpdateDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class EventUpdateDto {

    public String imageUrl;

    public String title;

    public String description;

    public LocalDateTime startTime;

    public LocalDateTime endTime;

    public BigDecimal price;

    public List<Long> selectedCategoryIds;

    public Long currencyId;

    public LocationCreateOrUpdateDto locationDto;
}

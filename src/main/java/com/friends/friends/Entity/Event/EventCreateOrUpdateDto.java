package com.friends.friends.Entity.Event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.vladmihalcea.hibernate.type.array.LongArrayType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventCreateOrUpdateDto {

    public String imageUrl;

    @NotEmpty(message = "Title must not be empty")
    public String title;

    @NotEmpty(message = "Description must not be empty")
    public String description;

    @NotNull(message = "Start time is required")
    public LocalDateTime startTime;

    public LocalDateTime endTime;

    @Positive(message = "Price must be positive")
    public BigDecimal price;

    @NotNull(message = "Category selection is required")
    @NotEmpty(message = "At least one category must be selected")
    public List<Long> selectedCategoryIds;

    @NotNull(message = "Location ID is required")
    public Long locationId;

    public Long currencyId;
}

package com.friends.friends.Entity.Event;

import com.friends.friends.Entity.Currency.CurrencyDto;
import com.friends.friends.Entity.Account.AccountDto;
import com.friends.friends.Entity.Category.CategoryDto;
import com.friends.friends.Entity.Location.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {
    private Long id;
    private String imageUrl;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private CurrencyDto currency;
    private LocalDateTime createdAt;
    private AccountDto createdBy;
    private List<CategoryDto> selectedCategories;
    private int favoriteByCount;
    private Location happeningOn;
}

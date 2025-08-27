package com.friends.friends.Entity.Currency;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyDto {
    
    private Long id;
    private String code;
    private String symbol;
    private String name;
}

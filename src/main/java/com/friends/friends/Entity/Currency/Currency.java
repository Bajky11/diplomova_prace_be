package com.friends.friends.Entity.Currency;

import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Entity.Location.LocationDto;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "currencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    // ATTRIBUTES

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 10)
    private String code; // např. CZK, EUR
    
    @Column(length = 10)
    private String symbol; // např. Kč, €
    
    @Column(length = 50)
    private String name; // např. Czech koruna
    

    // RELATIONSHIPS

    // there is (currency_id FK) in Event table pointing to Currency table
    @OneToMany(mappedBy = "currency")
    private List<Event> usedBy;

    public Currency(String code, String symbol, String name) {
        this.code = code;
        this.symbol = symbol;
        this.name = name;
    }

    // FUNCTIONS

    @PrePersist
    public void prePersist() {
        if (usedBy == null) usedBy = new ArrayList<>();
    }

    public CurrencyDto toDto() {
        return CurrencyDto.builder()
                .id(this.getId())
                .code(this.getCode())
                .symbol(this.getSymbol())
                .name(this.getName())
                .build();
    }
}

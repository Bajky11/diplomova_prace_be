package com.friends.friends.Entity.Category;

import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Entity.Account.Account;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    // ATTRIBUTES

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 100)
    private String name;


    // RELATIONS

    // Join table "account_favorite_categories" is created in Account table using attribute "favoriteCategories"
    @ManyToMany(mappedBy = "favoriteCategories")
    private List<Account> favoriteByAccount;

    // Join table "event_selected_categories" is created in Event table using attribute "selectedCategories"
    @ManyToMany(mappedBy = "selectedCategories")
    private List<Event> selectedByEvent;


    // FUNCTIONS

    public Category(String name) {
        this.name = name;
    }

    public CategoryDto toDto() {
        return CategoryDto.builder()
                .id(this.getId())
                .name(this.getName())
                .build();
    }
}

package com.friends.friends.Entity.Account;

import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Category.CategoryDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    
    private Long id;
    private String email;
    private String name;
    private String imageUrl;
    private String region;
    private Boolean isBusiness;
}

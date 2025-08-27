package com.friends.friends.Entity.Account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateDto {
    
    @Size(max = 100, message = "Jméno může mít maximálně 100 znaků")
    private String name;
    
    private String imageUrl;
    
    @Size(min = 6, message = "Heslo musí mít alespoň 6 znaků")
    private String password;
}

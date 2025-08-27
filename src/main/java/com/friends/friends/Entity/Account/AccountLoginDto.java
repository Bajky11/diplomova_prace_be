package com.friends.friends.Entity.Account;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountLoginDto {
    
    @NotBlank(message = "Email je povinný")
    @Email(message = "Neplatný formát emailu")
    private String email;
    
    @NotBlank(message = "Heslo je povinné")
    private String password;
}

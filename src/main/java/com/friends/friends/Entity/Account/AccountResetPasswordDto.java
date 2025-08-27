package com.friends.friends.Entity.Account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountResetPasswordDto {
    @Email
    @NotBlank
    @NotNull
    private String token;

    @NotBlank
    @NotNull
    private String newPassword;
}

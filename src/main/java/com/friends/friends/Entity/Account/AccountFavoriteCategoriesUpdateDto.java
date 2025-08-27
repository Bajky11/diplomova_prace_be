package com.friends.friends.Entity.Account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountFavoriteCategoriesUpdateDto {
    List<Long> categoryIds;
}

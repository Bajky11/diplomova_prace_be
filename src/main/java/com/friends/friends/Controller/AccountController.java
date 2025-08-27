package com.friends.friends.Controller;

import com.friends.friends.Entity.Account.AccountDto;
import com.friends.friends.Entity.Account.AccountFavoriteCategoriesUpdateDto;
import com.friends.friends.Entity.Account.AccountUpdateDto;
import com.friends.friends.Entity.Category.CategoryDto;
import com.friends.friends.Entity.Event.EventDto;
import com.friends.friends.Services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountDtoById(accountId));
    }

    // ME

    //tested
    @GetMapping("/me")
    public ResponseEntity<AccountDto> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        AccountDto user = accountService.getCurrentUserDto(email);
        return ResponseEntity.ok(user);
    }

    //tested
    @PutMapping("/me")
    public ResponseEntity<AccountDto> updateCurrentUser(@Valid @RequestBody AccountUpdateDto updateDto,
                                                        Authentication authentication) {
        String email = authentication.getName();
        AccountDto user = accountService.updateUser(email, updateDto);
        return ResponseEntity.ok(user);
    }

    // FAVORITES

    //tested
    @GetMapping("/{accountId}/favorite-events")
    public ResponseEntity<List<EventDto>> getFavoriteEventsByAccountId(@PathVariable Long accountId) {
        List<EventDto> events = accountService.getFavoriteEventsByAccountId(accountId);
        return ResponseEntity.ok(events);
    }

    // tested
    @PostMapping("/{accountId}/favorite-events/{eventId}")
    public ResponseEntity<Void> addEventToFavorites(@PathVariable Long accountId, @PathVariable Long eventId) {
        accountService.addEventToFavorites(accountId, eventId);
        return ResponseEntity.ok().build();
    }

    //tested
    @DeleteMapping("/{accountId}/favorite-events/{eventId}")
    public ResponseEntity<Void> removeEventFromFavorites(@PathVariable Long accountId, @PathVariable Long eventId) {
        accountService.removeEventFromFavorites(accountId, eventId);
        return ResponseEntity.ok().build();
    }

    // CATEGORIES
    @GetMapping("/favorite-categories")
    public ResponseEntity<List<CategoryDto>> getFavoriteCategoriesByAccountId(Authentication authentication) {
        List<CategoryDto> events = accountService.getFavoriteCategoriesByAccountId(authentication);
        return ResponseEntity.ok(events);
    }

    @PutMapping("/favorite-categories")
    public ResponseEntity<Void> updateFavoriteCategories(Authentication authentication, @RequestBody AccountFavoriteCategoriesUpdateDto dto) {
        accountService.updateFavoriteCategories(authentication, dto);
        return ResponseEntity.ok().build();
    }

    //tested
    @PostMapping("/favorite-categories/{categoryId}")
    public ResponseEntity<Void> addCategoryToFavoriteCategories(Authentication authentication, @PathVariable Long categoryId) {
        accountService.addCategoryToFavoriteCategories(authentication, categoryId);
        return ResponseEntity.ok().build();
    }

    // tested
    @DeleteMapping("/favorite-categories/{categoryId}")
    public ResponseEntity<Void> removeCategoryFromFavoriteCategories(Authentication authentication, @PathVariable Long categoryId) {
        accountService.removeCategoryFromFavoriteCategories(authentication, categoryId);
        return ResponseEntity.ok().build();
    }

}

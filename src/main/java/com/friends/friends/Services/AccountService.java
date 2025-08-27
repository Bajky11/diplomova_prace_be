package com.friends.friends.Services;

import com.friends.friends.Entity.Account.*;
import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Category.CategoryDto;
import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Entity.Event.EventDto;
import com.friends.friends.Exception.Account.AccountNotFoundException;
import com.friends.friends.Exception.Category.CategoryNotFoundException;
import com.friends.friends.Exception.Event.EventNotFoundException;
import com.friends.friends.Repository.AccountRepository;
import com.friends.friends.Repository.CategoryRepository;
import com.friends.friends.Repository.EventRepository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EventRepository eventRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private CategoryRepository categoryRepository;

    public AccountDto getAccountDtoById(Long accountId) {
        Account user = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        return user.toDto();
    }


    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
    }

    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email).orElseThrow(AccountNotFoundException::new);
    }


    public AccountDto getCurrentUserDto(String email) {
        Account user = accountRepository.findByEmail(email).orElseThrow(AccountNotFoundException::new);
        return user.toDto();
    }

    public Account getCurrentUser(String email) {
        return accountRepository.findByEmail(email).orElseThrow(AccountNotFoundException::new);
    }

    public AccountDto updateUser(String email, AccountUpdateDto updateDto) {
        Account user = accountRepository.findByEmail(email).orElseThrow(AccountNotFoundException::new);

        if (updateDto.getName() != null) {
            user.setName(updateDto.getName());
        }
        if (updateDto.getImageUrl() != null) {
            user.setImageUrl(updateDto.getImageUrl());
        }
        if (updateDto.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(updateDto.getPassword()));
        }

        Account savedUser = accountRepository.save(user);
        return savedUser.toDto();
    }


    public List<EventDto> getFavoriteEventsByAccountId(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);

        if (account.getFavoriteEvents() == null) {
            return List.of();
        }
        return account.getFavoriteEvents().stream().map(Event::toDto).toList();
    }

    public void addEventToFavorites(Long accountId, Long eventId) {
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        Event event = eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);

        if (!account.getFavoriteEvents().contains(event)) {
            account.getFavoriteEvents().add(event);
            accountRepository.save(account);
        }
    }

    public void removeEventFromFavorites(Long accountId, Long eventId) {
        Account account = accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
        Event event = eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);

        if (account.getFavoriteEvents().contains(event)) {
            account.getFavoriteEvents().remove(event);
            accountRepository.save(account);
        }
    }

    // CATEGORIES


    public List<CategoryDto> getFavoriteCategoriesByAccountId(Authentication authentication) {
        Account account = this.getCurrentUser(authentication.getName());

        if (account.getFavoriteCategories() == null) {
            return List.of();
        }
        return account.getFavoriteCategories().stream().map(Category::toDto).toList();
    }

    public void updateFavoriteCategories(Authentication authentication, AccountFavoriteCategoriesUpdateDto dto) {
        Account account = this.getCurrentUser(authentication.getName());
        List<Category> newCategories = new ArrayList<>();

        for (Long id : dto.getCategoryIds()) {
            Category category = categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
            newCategories.add(category);
        }

        account.setFavoriteCategories(newCategories);
        accountRepository.save(account);
    }

    public void addCategoryToFavoriteCategories(Authentication authentication, Long categoryId) {
        Account account = this.getCurrentUser(authentication.getName());
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);

        if (!account.getFavoriteCategories().contains(category)) {
            account.getFavoriteCategories().add(category);
            accountRepository.save(account);
        }
    }

    public void removeCategoryFromFavoriteCategories(Authentication authentication, Long categoryId) {
        Account account = this.getCurrentUser(authentication.getName());
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);

        if (account.getFavoriteCategories().contains(category)) {
            account.getFavoriteCategories().remove(category);
            accountRepository.save(account);
        }
    }


}

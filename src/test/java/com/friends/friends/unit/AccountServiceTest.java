package com.friends.friends.unit;


import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.Account.AccountDto;
import com.friends.friends.Entity.Account.AccountFavoriteCategoriesUpdateDto;
import com.friends.friends.Entity.Account.AccountUpdateDto;
import com.friends.friends.Entity.Category.Category;
import com.friends.friends.Entity.Category.CategoryDto;
import com.friends.friends.Entity.Event.Event;
import com.friends.friends.Exception.Account.AccountNotFoundException;
import com.friends.friends.Exception.Category.CategoryNotFoundException;
import com.friends.friends.Repository.AccountRepository;
import com.friends.friends.Repository.CategoryRepository;
import com.friends.friends.Repository.EventRepository.EventRepository;
import com.friends.friends.Services.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class AccountServiceTest {

    @Mock
    AccountRepository accountRepository;
    @Mock
    EventRepository eventRepository;
    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    AccountService service;

    // ---------- getAccountDtoById ----------
    @Test
    void getAccountDtoById_returnsDto() {
        Account acc = mock(Account.class);
        AccountDto dto = new AccountDto();
        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(acc.toDto()).thenReturn(dto);

        AccountDto result = service.getAccountDtoById(1L);

        assertSame(dto, result);
        verify(accountRepository).findById(1L);
        verify(acc).toDto();
    }

    @Test
    void getAccountDtoById_throwsWhenNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> service.getAccountDtoById(1L));
    }

    // ---------- updateUser ----------
    @Test
    void updateUser_updatesOnlyProvidedFields_andHashesPassword() {
        Account acc = new Account();
        acc.setEmail("user@x.cz");
        when(accountRepository.findByEmail("user@x.cz")).thenReturn(Optional.of(acc));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setName("New Name");
        dto.setImageUrl("img_123");
        dto.setPassword("secret");

        AccountDto outDto = new AccountDto();
        // Pokud toDto() je na Account, můžeš mocknout:
        Account spyAcc = spy(acc);
        when(spyAcc.toDto()).thenReturn(outDto);
        when(accountRepository.findByEmail("user@x.cz")).thenReturn(Optional.of(spyAcc));

        AccountDto result = service.updateUser("user@x.cz", dto);

        assertSame(outDto, result);
        assertEquals("New Name", spyAcc.getName());
        assertEquals("img_123", spyAcc.getImageUrl());
        assertNotEquals("secret", spyAcc.getPasswordHash());

        // Ověř, že hash opravdu odpovídá
        assertTrue(new BCryptPasswordEncoder().matches("secret", spyAcc.getPasswordHash()));
        verify(accountRepository).save(spyAcc);
    }

    @Test
    void updateUser_noPasswordProvided_doesNotChangeHash() {
        Account acc = new Account();
        acc.setEmail("user@x.cz");
        acc.setPasswordHash("$2a$10$existinghash...");
        Account spyAcc = spy(acc);

        when(accountRepository.findByEmail("user@x.cz")).thenReturn(Optional.of(spyAcc));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(spyAcc.toDto()).thenReturn(new AccountDto());

        AccountUpdateDto dto = new AccountUpdateDto();
        dto.setName("A"); dto.setImageUrl("B");
        // password je null

        service.updateUser("user@x.cz", dto);

        assertEquals("$2a$10$existinghash...", spyAcc.getPasswordHash());
        verify(accountRepository).save(spyAcc);
    }

    // ---------- addEventToFavorites ----------
    @Test
    void addEventToFavorites_addsWhenNotPresent_andSaves() {
        Account acc = new Account();
        acc.setFavoriteEvents(new ArrayList<>());
        Event ev = new Event();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(ev));

        service.addEventToFavorites(1L, 2L);

        assertTrue(acc.getFavoriteEvents().contains(ev));
        verify(accountRepository).save(acc);
    }

    @Test
    void addEventToFavorites_isIdempotent_noSaveWhenAlreadyPresent() {
        Event ev = new Event();
        Account acc = new Account();
        acc.setFavoriteEvents(new ArrayList<>(List.of(ev)));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(ev));

        service.addEventToFavorites(1L, 2L);

        verify(accountRepository, never()).save(any());
        assertEquals(1, acc.getFavoriteEvents().size());
    }

    // ---------- removeEventFromFavorites ----------
    @Test
    void removeEventFromFavorites_removesWhenPresent_andSaves() {
        Event ev = new Event();
        Account acc = new Account();
        acc.setFavoriteEvents(new ArrayList<>(List.of(ev)));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(ev));

        service.removeEventFromFavorites(1L, 2L);

        assertFalse(acc.getFavoriteEvents().contains(ev));
        verify(accountRepository).save(acc);
    }

    @Test
    void removeEventFromFavorites_noSaveWhenNotPresent() {
        Event ev = new Event();
        Account acc = new Account();
        acc.setFavoriteEvents(new ArrayList<>());

        when(accountRepository.findById(1L)).thenReturn(Optional.of(acc));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(ev));

        service.removeEventFromFavorites(1L, 2L);

        verify(accountRepository, never()).save(any());
    }

    // ---------- getFavoriteCategoriesByAccountId (Authentication) ----------
    @Test
    void getFavoriteCategoriesByAccountId_usesAuthenticationEmail_andNullSafe() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@x.cz");

        Account acc = new Account();
        acc.setFavoriteCategories(null); // null → očekáváme prázdný list

        when(accountRepository.findByEmail("user@x.cz")).thenReturn(Optional.of(acc));

        List<CategoryDto> out = service.getFavoriteCategoriesByAccountId(auth);
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    // ---------- updateFavoriteCategories ----------
    @Test
    void updateFavoriteCategories_replacesAll_andSaves() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@x.cz");
        Account acc = new Account();
        acc.setFavoriteCategories(new ArrayList<>());
        when(accountRepository.findByEmail("user@x.cz")).thenReturn(Optional.of(acc));

        Category c1 = new Category(); Category c2 = new Category();
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(c1));
        when(categoryRepository.findById(11L)).thenReturn(Optional.of(c2));

        AccountFavoriteCategoriesUpdateDto dto = new AccountFavoriteCategoriesUpdateDto();
        dto.setCategoryIds(List.of(10L, 11L));

        service.updateFavoriteCategories(auth, dto);

        assertEquals(List.of(c1, c2), acc.getFavoriteCategories());
        verify(accountRepository).save(acc);
    }

    @Test
    void updateFavoriteCategories_throwsWhenAnyCategoryMissing() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@x.cz");
        when(accountRepository.findByEmail("user@x.cz")).thenReturn(Optional.of(new Account()));
        when(categoryRepository.findById(10L)).thenReturn(Optional.empty());

        AccountFavoriteCategoriesUpdateDto dto = new AccountFavoriteCategoriesUpdateDto();
        dto.setCategoryIds(List.of(10L));

        assertThrows(CategoryNotFoundException.class, () -> service.updateFavoriteCategories(auth, dto));
    }
}
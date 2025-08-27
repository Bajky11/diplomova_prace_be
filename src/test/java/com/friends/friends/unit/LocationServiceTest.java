package com.friends.friends.unit;

import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.Location.Location;
import com.friends.friends.Entity.Location.LocationCreateOrUpdateDto;
import com.friends.friends.Entity.Location.LocationDto;
import com.friends.friends.Exception.Location.LocationNotFoundException;
import com.friends.friends.Repository.AccountRepository;
import com.friends.friends.Repository.LocationRepository;
import com.friends.friends.Services.AccountService;
import com.friends.friends.Services.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock LocationRepository locationRepository;
    @Mock AccountRepository accountRepository; // je @Autowired v service, ale v testech ho nevyužíváme
    @Mock
    AccountService accountService;

    @InjectMocks
    LocationService service;

    @Captor ArgumentCaptor<Location> locationCaptor;

    // -------- getLocationDtoById / getLocationById --------

    @Test
    void getLocationDtoById_returnsDto() {
        Location entity = spy(new Location());
        LocationDto dto = new LocationDto();
        doReturn(dto).when(entity).toDto();
        when(locationRepository.findById(10L)).thenReturn(Optional.of(entity));

        LocationDto out = service.getLocationDtoById(10L);

        assertSame(dto, out);
        verify(locationRepository).findById(10L);
        verify(entity).toDto();
    }

    @Test
    void getLocationDtoById_notFound_throws() {
        when(locationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(LocationNotFoundException.class, () -> service.getLocationDtoById(99L));
    }

    @Test
    void getLocationById_notFound_throws() {
        when(locationRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(LocationNotFoundException.class, () -> service.getLocationById(5L));
    }

    // -------- createLocation --------

    @Test
    void createLocation_success_setsFieldsAndCreatedBy_andReturnsDto() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        Account acc = new Account(); acc.setId(1L); acc.setEmail("user@example.com");
        when(accountService.getCurrentUser("user@example.com")).thenReturn(acc);

        LocationDto dtoOut = new LocationDto();
        when(locationRepository.save(any(Location.class))).thenAnswer(inv -> {
            Location saved = (Location) inv.getArgument(0);
            Location spySaved = spy(saved);
            doReturn(dtoOut).when(spySaved).toDto();
            return spySaved;
        });

        LocationCreateOrUpdateDto dto = new LocationCreateOrUpdateDto();
        dto.setAddress("Náměstí 1");
        dto.setLatitude(50.087);
        dto.setLongitude(14.421);

        LocationDto out = service.createLocation(dto, auth);

        assertSame(dtoOut, out);
        // zkontrolujeme obsah uložené entity
        verify(locationRepository).save(locationCaptor.capture());
        Location persisted = locationCaptor.getValue();
        assertEquals("Náměstí 1", persisted.getAddress());
        assertEquals(50.087, persisted.getLatitude());
        assertEquals(14.421, persisted.getLongitude());
        assertEquals(acc, persisted.getCreatedBy());

        verify(accountService).getCurrentUser("user@example.com");
    }

    // -------- updateLocation --------

    @Test
    void updateLocation_updatesFields_andReturnsDto() {
        Location existing = spy(new Location());
        existing.setId(7L);
        existing.setAddress("Old");
        existing.setLatitude(49.0);
        existing.setLongitude(16.0);

        // findById vrátí existující entitu
        when(locationRepository.findById(7L)).thenReturn(Optional.of(existing));

        // save vrátí stejnou instanci (běžný pattern) a toDto má vrátit náš dtoOut
        LocationDto dtoOut = new LocationDto();
        doReturn(dtoOut).when(existing).toDto();
        when(locationRepository.save(existing)).thenReturn(existing);

        LocationCreateOrUpdateDto dto = new LocationCreateOrUpdateDto();
        dto.setAddress("New");
        dto.setLatitude(50.1);
        dto.setLongitude(14.5);

        LocationDto out = service.updateLocation(7L, dto);

        assertSame(dtoOut, out);
        assertEquals("New", existing.getAddress());
        assertEquals(50.1, existing.getLatitude());
        assertEquals(14.5, existing.getLongitude());
        verify(locationRepository).save(existing);
    }

    @Test
    void updateLocation_notFound_throws() {
        when(locationRepository.findById(404L)).thenReturn(Optional.empty());
        LocationCreateOrUpdateDto dto = new LocationCreateOrUpdateDto();
        assertThrows(LocationNotFoundException.class, () -> service.updateLocation(404L, dto));
        verify(locationRepository, never()).save(any());
    }

    // -------- deleteLocation --------

    @Test
    void deleteLocation_ok() {
        when(locationRepository.existsById(3L)).thenReturn(true);
        service.deleteLocation(3L);
        verify(locationRepository).deleteById(3L);
    }

    @Test
    void deleteLocation_notFound_throws() {
        when(locationRepository.existsById(3L)).thenReturn(false);
        assertThrows(LocationNotFoundException.class, () -> service.deleteLocation(3L));
        verify(locationRepository, never()).deleteById(anyLong());
    }
}
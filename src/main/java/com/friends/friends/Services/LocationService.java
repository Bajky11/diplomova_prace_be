package com.friends.friends.Services;

import com.friends.friends.Entity.Account.AccountDto;
import com.friends.friends.Entity.Location.Location;
import com.friends.friends.Entity.Location.LocationCreateOrUpdateDto;
import com.friends.friends.Entity.Location.LocationDto;
import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Exception.Account.AccountNotFoundException;
import com.friends.friends.Exception.Location.LocationNotFoundException;
import com.friends.friends.Repository.LocationRepository;
import com.friends.friends.Repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;

    public LocationDto getLocationDtoById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(LocationNotFoundException::new);
        return location.toDto();
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(LocationNotFoundException::new);

    }

    public LocationDto createLocation(LocationCreateOrUpdateDto dto, Authentication authentication) {
        Account account = accountService.getCurrentUser(authentication.getName());

        Location location = new Location();
        location.setCreatedBy(account);
        location.setAddress(dto.getAddress());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        Location newLocation = locationRepository.save(location);
        return newLocation.toDto();
    }

    public LocationDto updateLocation(Long id, LocationCreateOrUpdateDto dto) {
        Location location = locationRepository.findById(id)
                .orElseThrow(LocationNotFoundException::new);
        location.setAddress(dto.getAddress());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location = locationRepository.save(location);
        return location.toDto();
    }

    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException();
        }
        locationRepository.deleteById(id);
    }
}

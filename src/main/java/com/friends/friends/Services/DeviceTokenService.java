package com.friends.friends.Services;

import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.DeviceToken.DeviceToken;
import com.friends.friends.Exception.Common.AlreadyExistsException;
import com.friends.friends.Repository.DeviceTokenRepository.DeviceTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceTokenService {

    DeviceTokenRepository deviceTokenRepository;
    AccountService accountService;

    public DeviceTokenService(DeviceTokenRepository deviceTokenRepository, AccountService accountService) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.accountService = accountService;
    }

    public void saveToken(Authentication authentication , String token, String platform) {
        if(deviceTokenRepository.findDeviceTokenByDeviceToken(token).isPresent()){
            throw new AlreadyExistsException("DeviceToken already exists");
        }
        Account account = accountService.getCurrentUser(authentication.getName());

        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setDeviceToken(token);
        deviceToken.setPlatform(platform);
        deviceToken.setTokenOwner(account);

        deviceTokenRepository.save(deviceToken);
    }

    @Transactional
    public void deleteToken(String token) {
        deviceTokenRepository.removeDeviceTokenByDeviceToken(token);
    }

    public List<DeviceToken> getAllDeviceTokensByAccountId(Long accountId) {
        Account account = accountService.getAccountById(accountId);
        return deviceTokenRepository.findDeviceTokenByTokenOwner(account);
    }
}

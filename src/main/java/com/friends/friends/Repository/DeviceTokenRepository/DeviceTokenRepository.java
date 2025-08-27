package com.friends.friends.Repository.DeviceTokenRepository;

import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Entity.DeviceToken.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findDeviceTokenByTokenOwner(Account tokenOwner);

    Optional<DeviceToken> findDeviceTokenByDeviceToken(String token);

    void removeDeviceTokenByDeviceToken(String deviceToken);

}

package com.friends.friends.Controller;

import com.friends.friends.Services.DeviceTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-token")
public class DeviceTokenController {

    DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping
    public ResponseEntity<Void> saveToken(Authentication authentication, @RequestParam String token,@RequestParam String platform){
        deviceTokenService.saveToken(authentication, token, platform);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteToken( @RequestParam String token){
        deviceTokenService.deleteToken(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

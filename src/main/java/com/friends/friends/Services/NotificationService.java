package com.friends.friends.Services;

import com.friends.friends.Entity.Notification.Notification;
import com.friends.friends.Entity.Notification.NotificationDto;
import com.friends.friends.Entity.Account.Account;
import com.friends.friends.Repository.NotificationRepository;
import com.friends.friends.Repository.AccountRepository;
import com.google.firebase.messaging.AndroidConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AccountRepository accountRepository;

    public void sendNotification(String deviceToken, String title, String body) {
        // sadly my entity is also called Notification...
        com.google.firebase.messaging.Notification notification = com.google.firebase.messaging.Notification.builder().setTitle(title).setBody(body).build();

        Message message = Message.builder().setToken(deviceToken).setNotification(notification).build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Notification sent successfully: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDataOnly(String deviceToken, String title, String body) {
        // Osekání pro jistotu: FCM data payload ≈ 4 KB limit
        String safeTitle = title != null ? title : "";
        String safeBody  = body  != null ? body  : "";
        if (safeBody.length() > 3500) { // konzervativní limit
            safeBody = safeBody.substring(0, 3500) + " …";
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", "daily_events");
        data.put("title", safeTitle);
        data.put("body",  safeBody);

        Message message = Message.builder()
                .setToken(deviceToken)
                .putAllData(data)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                // iOS (volitelné): pokud chceš „silent push“, přidej contentAvailable(true)
                // .setApnsConfig(ApnsConfig.builder()
                //         .setAps(Aps.builder().setContentAvailable(true).build())
                //         .build())
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Data-only push sent: " + response);
        } catch (Exception e) {
            // Chyby držíme v angličtině (požadavek projektu)
            System.err.println("Failed to send data-only push: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<NotificationDto> getUserNotifications(String userEmail) {
        Account user = accountRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("Uživatel nenalezen"));

        List<Notification> notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        return notifications.stream().map(Notification::toDto).collect(Collectors.toList());
    }
}

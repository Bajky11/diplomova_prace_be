package com.friends.friends.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class FirebaseInitializer {
    @PostConstruct
    public void init() {
        try (InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase/hotspot-ed94b-firebase-adminsdk-fbsvc-98fb6eba84.json")) {

            FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("✅ Firebase initialized");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

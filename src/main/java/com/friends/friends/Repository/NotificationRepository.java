package com.friends.friends.Repository;

import com.friends.friends.Entity.Notification.Notification;
import com.friends.friends.Entity.Account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {



    List<Notification> findByRecipientOrderByCreatedAtDesc(Account user);



}

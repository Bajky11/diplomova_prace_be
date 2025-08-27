package com.friends.friends.Entity.Notification;

import com.friends.friends.Entity.Account.Account;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    //ATTRIBUTES

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();


    //RELATIONS

    // FK pointing to one row in Account table (recipient_account_id FK)
    @ManyToOne
    @JoinColumn(name = "recipient_account_id")
    private Account recipient;

    // FUNCTIONS

    public NotificationDto toDto() {
        return NotificationDto.builder()
                .id(this.getId())
                .text(this.getText())
                .data(this.getData())
                .sentAt(this.getCreatedAt())
                .build();
    }
}

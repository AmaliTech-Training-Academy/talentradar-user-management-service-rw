package com.talentradar.user_service.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEvent implements Serializable {
    private EventType eventType; // USER_CREATED, USER_UPDATED, USER_DELETED
    private UUID userId;
    private UUID managerId;
    private String fullName;
    private String username;
    private String email;
    private EventRole role; // DEVELOPER, MANAGER
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String eventId;
    private String source;
}

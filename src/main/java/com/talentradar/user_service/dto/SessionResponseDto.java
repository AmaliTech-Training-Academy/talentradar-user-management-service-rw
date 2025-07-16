package com.talentradar.user_service.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SessionResponseDto {
    private UUID id;
    private String sessionId;
    private String ipAddress;
    private String deviceInfo;
    private LocalDateTime createdAt;
    private boolean isActive;

    private UserSummaryDto user;
}


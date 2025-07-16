package com.talentradar.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSummaryDto {
    private UUID id;
    private String fullName;
    private String email;
}


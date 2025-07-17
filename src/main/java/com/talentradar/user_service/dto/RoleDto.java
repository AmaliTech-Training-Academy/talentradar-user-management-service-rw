package com.talentradar.user_service.dto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.talentradar.user_service.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {
    private UUID id;
    private String roleName;

    public static List<RoleDto> fromRole(List<Role> roles) {
        return roles.stream()
                .map(role -> RoleDto.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .build())
                .collect(Collectors.toList());
    }

}

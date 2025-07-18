package com.talentradar.user_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.dto.RoleDto;
import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.repository.RoleRepository;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // Get All roles
    public ResponseDto getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDto> roleDtos = RoleDto.fromRole(roles);

        ResponseDto responseDto = new ResponseDto();
        responseDto.setMessage("Roles retrieved successfully");
        responseDto.setData(roleDtos);
        responseDto.setStatus(true);
        responseDto.setErrors(null);
        responseDto.setData(Map.of("roles", roleDtos));

        return responseDto;
    }
}

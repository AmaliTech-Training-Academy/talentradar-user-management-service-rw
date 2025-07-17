package com.talentradar.user_service.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Principal {

    private String id;
    private String email;
    private String fullName;
    private String role;

    @Override
    public String getName() {
        return id; // Return user ID as the principal name
    }

    public String getUserId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}

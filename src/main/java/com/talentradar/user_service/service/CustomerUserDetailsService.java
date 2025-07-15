package com.talentradar.user_service.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.talentradar.user_service.model.CustomUserDetails;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.repository.UserRepository;

public class CustomerUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomerUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First try to find user by email
        User user = userRepository.findByEmail(email)
                .orElse(null);

        // If not found, try to find by email
        if (user == null) {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with email: " + email));
        }

        return new CustomUserDetails(user);
    }

}

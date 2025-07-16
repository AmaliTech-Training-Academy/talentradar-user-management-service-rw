package com.talentradar.user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.repository.RoleRepository;
import com.talentradar.user_service.repository.UserRepository;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(RoleRepository roleRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed roles and users here
        // 1) Seed Roles (developer, admin, and manager)
        seedRoles();
    }

    private void seedRoles() {
        Role developerRole = new Role();
        developerRole.setRoleName("DEVELOPER");

        Role adminRole = new Role();
        adminRole.setRoleName("ADMIN");

        Role managerRole = new Role();
        managerRole.setRoleName("MANAGER");

        // Check if role already exists before saving
        if (!roleRepository.findByRoleName("DEVELOPER").isPresent()) {
            roleRepository.save(developerRole);
        }
        if (!roleRepository.findByRoleName("ADMIN").isPresent()) {
            roleRepository.save(adminRole);
        }
        if (!roleRepository.findByRoleName("MANAGER").isPresent()) {
            roleRepository.save(managerRole);
        }

        // 2) Seed Users with the roles created in step 1
        // Create admin user
        Role savedAdminRole = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found after seeding"));
        User adminUser = new User();
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("test123"));
        adminUser.setRole(savedAdminRole);

        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            userRepository.save(adminUser);
        }
    }

}

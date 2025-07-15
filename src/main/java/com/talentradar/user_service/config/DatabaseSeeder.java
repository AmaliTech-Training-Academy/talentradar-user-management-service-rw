package com.talentradar.user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.repository.RoleRepository;

@Configuration
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DatabaseSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
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
        // Additional seeding logic can be added here
        // 2) Seed Users with the roles created in step 1
    }

}

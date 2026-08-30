package com.resourcebooking.config;

import com.resourcebooking.entity.User;
import com.resourcebooking.enums.Role;
import com.resourcebooking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {

                User admin = new User();

                admin.setUsername("admin");
                admin.setEmail("admin@example.com");

                admin.setPassword(
                        passwordEncoder.encode("Admin@123")
                );

                admin.setRole(Role.ADMIN);

                userRepository.save(admin);
            }

            if (userRepository.findByUsername("user").isEmpty()) {

                User user = new User();

                user.setUsername("user");
                user.setEmail("user@example.com");

                user.setPassword(
                        passwordEncoder.encode("User@123")
                );

                user.setRole(Role.USER);

                userRepository.save(user);
            }
        };
    }
}

package com.arsw.ids_ia.config;

import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.utils.enums.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class AdminSeeder {

    private static final Logger logger = LoggerFactory.getLogger(AdminSeeder.class);

    @Value("${app.initial-admins:}")
    private String initialAdmins;

    @Bean
    public CommandLineRunner seedAdmins(UserRepository userRepository) {
        return args -> {
            if (initialAdmins == null || initialAdmins.isBlank()) {
                logger.info("No initial admins configured (app.initial-admins)");
                return;
            }

            List<String> admins = Arrays.stream(initialAdmins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            for (String email : admins) {
                if (userRepository.existsByEmail(email)) {
                    logger.info("Admin {} already exists", email);
                    continue;
                }

                User u = User.builder()
                        .email(email)
                        .name(email)
                        .role(Role.ADMIN)
                        .build();

                userRepository.save(u);
                logger.info("Created initial admin {}", email);
            }
        };
    }
}

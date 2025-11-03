package com.arsw.ids_ia.security;

import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JwtUserAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Logger logger = LoggerFactory.getLogger(JwtUserAuthoritiesConverter.class);

    private final UserRepository userRepository;

    public JwtUserAuthoritiesConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object emailObj = jwt.getClaim("email");
        if (emailObj == null) {
            logger.warn("JWT does not contain 'email' claim; cannot map to local user");
            return Collections.emptyList();
        }

        String email = String.valueOf(emailObj).toLowerCase();
        Optional<User> opt = userRepository.findByEmail(email);
        User user;
        if (opt.isPresent()) {
            user = opt.get();
        } else {
            // Create a new user with default ROLE_USER
            user = User.builder()
                    .email(email)
                    .name(email)
                    .role(com.arsw.ids_ia.utils.enums.Role.USER)
                    .build();
            try {
                user = userRepository.save(user);
                logger.info("Created new user for email {} with ROLE_USER", email);
            } catch (Exception e) {
                logger.error("Failed to create user for email {}: {}", email, e.getMessage());
                return Collections.emptyList();
            }
        }

        if (user.getRole() == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}

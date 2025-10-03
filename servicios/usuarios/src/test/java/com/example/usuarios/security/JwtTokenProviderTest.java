package com.example.usuarios.security;

import com.example.usuarios.config.JwtProperties;
import com.example.usuarios.entity.AuthProvider;
import com.example.usuarios.entity.Role;
import com.example.usuarios.entity.UserEntity;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    @Test
    void generate_and_parse_token_contains_role_and_email() {
        JwtProperties props = new JwtProperties();
        props.setSecret("abcdefghijklmnopqrstuvwxyz1234567890abcd");
        props.setAccessTokenValiditySeconds(3600);
        props.setRefreshTokenValiditySeconds(86400);

        JwtTokenProvider provider = new JwtTokenProvider(props);

        UserEntity user = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .fullName("User Test")
                .role(Role.USER)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        String token = provider.generateToken(user);
        assertThat(token).isNotBlank();
        assertThat(provider.validate(token)).isTrue();

        Authentication auth = provider.buildAuthentication(token);
        assertThat(auth.getName()).isEqualTo("user@test.com");
        assertThat(auth.getAuthorities()).extracting("authority").contains("ROLE_USER");
    }
}


package com.example.usuarios.service;

import com.example.usuarios.dto.RegisterUserRequest;
import com.example.usuarios.dto.UserResponse;
import com.example.usuarios.entity.AuthProvider;
import com.example.usuarios.entity.Role;
import com.example.usuarios.entity.UserEntity;
import com.example.usuarios.repository.UserRepository;
import com.example.usuarios.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenProvider = mock(JwtTokenProvider.class);
        authService = new AuthService(authenticationManager, userRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void register_creates_user_with_encoded_password_and_local_provider() {
        // given
        when(userRepository.existsByEmail("new@portal.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("ENCODED");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            if (u.getId() == null) {
                u.setId(UUID.randomUUID());
            }
            return u;
        });

        RegisterUserRequest request = new RegisterUserRequest(
                "new@portal.com",
                "Secret123!",
                "Nuevo Usuario",
                Role.INSTRUCTOR
        );

        // when
        UserResponse response = authService.register(request);

        // then
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertThat(saved.getPassword()).isEqualTo("ENCODED");
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(saved.getRole()).isEqualTo(Role.INSTRUCTOR);

        assertThat(response.email()).isEqualTo("new@portal.com");
        assertThat(response.role()).isEqualTo(Role.INSTRUCTOR);
        assertThat(response.provider()).isEqualTo(AuthProvider.LOCAL);
    }
}


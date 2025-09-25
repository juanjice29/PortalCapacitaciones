package com.example.usuarios.service;

import com.example.usuarios.dto.AuthRequest;
import com.example.usuarios.dto.AuthResponse;
import com.example.usuarios.dto.RegisterUserRequest;
import com.example.usuarios.dto.UserResponse;
import com.example.usuarios.entity.AuthProvider;
import com.example.usuarios.entity.Role;
import com.example.usuarios.entity.UserEntity;
import com.example.usuarios.exception.BusinessRuleException;
import com.example.usuarios.exception.ResourceNotFoundException;
import com.example.usuarios.mapper.UserMapper;
import com.example.usuarios.repository.UserRepository;
import com.example.usuarios.security.JwtTokenProvider;
import com.example.usuarios.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            UserEntity user = userRepository.findByEmail(principal.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            String token = tokenProvider.generateToken(user);
            return new AuthResponse(token, tokenValidity());
        } catch (BadCredentialsException ex) {
            throw new BusinessRuleException("Credenciales invalidas");
        }
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("El email ya esta registrado");
        }
        if (request.password() == null || request.password().length() < 8) {
            throw new BusinessRuleException("La contrasena debe tener al menos 8 caracteres");
        }
        Role role = request.role() != null ? request.role() : Role.USER;
        UserEntity user = UserEntity.builder()
                .email(request.email())
                .fullName(request.fullName())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();
        UserEntity saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    private long tokenValidity() {
        return tokenProvider.getAccessTokenValiditySeconds();
    }
}

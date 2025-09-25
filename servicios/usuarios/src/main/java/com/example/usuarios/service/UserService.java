package com.example.usuarios.service;

import com.example.usuarios.dto.RegisterUserRequest;
import com.example.usuarios.dto.UpdateUserRequest;
import com.example.usuarios.dto.UserResponse;
import com.example.usuarios.entity.AuthProvider;
import com.example.usuarios.entity.UserEntity;
import com.example.usuarios.exception.BusinessRuleException;
import com.example.usuarios.exception.ResourceNotFoundException;
import com.example.usuarios.mapper.UserMapper;
import com.example.usuarios.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse findById(UUID id) {
        return UserMapper.toResponse(loadEntity(id));
    }

    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Transactional
    public UserResponse create(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("El email ya esta registrado");
        }
        UserEntity entity = UserEntity.builder()
                .email(request.email())
                .fullName(request.fullName())
                .role(request.role())
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .build();
        if (request.password() != null) {
            entity.setPassword(passwordEncoder.encode(request.password()));
        }
        UserEntity saved = userRepository.save(entity);
        return UserMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        UserEntity user = loadEntity(id);
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setEnabled(request.enabled());
        return UserMapper.toResponse(user);
    }

    @Transactional
    public void delete(UUID id) {
        UserEntity user = loadEntity(id);
        userRepository.delete(user);
    }

    public UserEntity loadEntity(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}

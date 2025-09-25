package com.example.usuarios.security;

import com.example.usuarios.entity.AuthProvider;
import com.example.usuarios.entity.Role;
import com.example.usuarios.entity.UserEntity;
import com.example.usuarios.repository.UserRepository;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email = extractEmail(oauth2User.getAttributes());
        if (!StringUtils.hasText(email)) {
            throw new OAuth2AuthenticationException("No se pudo obtener el email del proveedor OAuth2");
        }

        AuthProvider provider = mapProvider(registrationId);
        String providerId = extractProviderId(oauth2User.getAttributes());

        UserEntity user = userRepository.findByEmail(email)
                .map(existing -> updateExisting(existing, provider, providerId))
                .orElseGet(() -> createNewUser(email, provider, providerId, oauth2User.getAttributes()));

        Collection<SimpleGrantedAuthority> authorities = java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new DefaultOAuth2User(authorities, oauth2User.getAttributes(), "sub");
    }

    private UserEntity updateExisting(UserEntity user, AuthProvider provider, String providerId) {
        user.setProvider(provider);
        user.setProviderId(providerId);
        return userRepository.save(user);
    }

    private UserEntity createNewUser(String email, AuthProvider provider, String providerId, Map<String, Object> attributes) {
        String fullName = Optional.ofNullable(attributes.get("name"))
                .map(Object::toString)
                .orElse(email);
        Role role = resolveRole(attributes).orElse(Role.USER);
        UserEntity user = UserEntity.builder()
                .email(email)
                .fullName(fullName)
                .provider(provider)
                .providerId(providerId)
                .role(role)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    private Optional<Role> resolveRole(Map<String, Object> attributes) {
        // Intenta mapear roles de Keycloak si vienen en el userinfo
        // Formatos posibles: realm_access.roles ["admin","instructor","user"] o roles [..] o groups [..]
        try {
            Object realmAccess = attributes.get("realm_access");
            if (realmAccess instanceof Map<?, ?> map) {
                Object rolesObj = map.get("roles");
                if (rolesObj instanceof Iterable<?> roles) {
                    for (Object r : roles) {
                        Role mapped = mapRoleName(String.valueOf(r));
                        if (mapped != null) return Optional.of(mapped);
                    }
                }
            }
            Object roles = attributes.get("roles");
            if (roles instanceof Iterable<?> it) {
                for (Object r : it) {
                    Role mapped = mapRoleName(String.valueOf(r));
                    if (mapped != null) return Optional.of(mapped);
                }
            }
            Object groups = attributes.get("groups");
            if (groups instanceof Iterable<?> it) {
                for (Object g : it) {
                    Role mapped = mapRoleName(String.valueOf(g));
                    if (mapped != null) return Optional.of(mapped);
                }
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private Role mapRoleName(String raw) {
        if (!StringUtils.hasText(raw)) return null;
        String s = raw.trim().toUpperCase();
        if (s.contains("ADMIN")) return Role.ADMIN;
        if (s.contains("INSTRUCTOR")) return Role.INSTRUCTOR;
        if (s.contains("USER") || s.contains("USUARIO")) return Role.USER;
        return null;
    }

    private String extractEmail(Map<String, Object> attributes) {
        Object email = attributes.get("email");
        if (email == null) {
            email = attributes.get("preferred_username");
        }
        return email != null ? email.toString() : null;
    }

    private String extractProviderId(Map<String, Object> attributes) {
        Object sub = attributes.get("sub");
        if (sub == null) {
            sub = attributes.get("oid");
        }
        return sub != null ? sub.toString() : null;
    }

    private AuthProvider mapProvider(String registrationId) {
        if (!StringUtils.hasText(registrationId)) {
            return AuthProvider.LOCAL;
        }
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "azure", "azuread" -> AuthProvider.AZURE_AD;
            case "github" -> AuthProvider.GITHUB;
            case "keycloak" -> AuthProvider.KEYCLOAK;
            default -> AuthProvider.LOCAL;
        };
    }
}


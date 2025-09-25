package com.example.usuarios.security;

import com.example.usuarios.entity.AuthProvider;
import com.example.usuarios.entity.Role;
import com.example.usuarios.entity.UserEntity;
import com.example.usuarios.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = resolveEmail(oidcUser);
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("No se pudo obtener el email del proveedor OIDC");
        }

        String providerId = oidcUser.getSubject();
        AuthProvider provider = AuthProvider.KEYCLOAK;

        // Preferir roles del Access Token; si no, del ID Token (claims del OIDC user)
        Role resolvedRole =
                resolveRoleFromAccessToken(userRequest)
                        .or(() -> resolveRole(oidcUser.getClaims()))
                        .orElse(Role.USER);

        UserEntity user = userRepository.findByEmail(email)
                .map(existing -> updateExisting(existing, provider, providerId, resolvedRole))
                .orElseGet(() -> createNewUser(email, provider, providerId, oidcUser.getClaims(), resolvedRole));

        Collection<SimpleGrantedAuthority> authorities = java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub");
    }

    private UserEntity updateExisting(UserEntity user, AuthProvider provider, String providerId, Role resolvedRole) {
        user.setProvider(provider);
        user.setProviderId(providerId);
        if (resolvedRole != null && user.getRole() != resolvedRole) {
            user.setRole(resolvedRole);
        }
        return userRepository.save(user);
    }

    private UserEntity createNewUser(String email, AuthProvider provider, String providerId, Map<String, Object> claims, Role role) {
        String fullName = Optional.ofNullable(claims.get("name")).map(Object::toString).orElse(email);
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

    private Optional<Role> resolveRoleFromAccessToken(OidcUserRequest userRequest) {
        try {
            String token = userRequest.getAccessToken().getTokenValue();
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return Optional.empty();
            }
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> claims = mapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
            return resolveRole(claims);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String resolveEmail(OidcUser user) {
        Object email = user.getClaims().get("email");
        if (email == null) {
            email = user.getClaims().get("preferred_username");
        }
        return email != null ? email.toString() : null;
    }
}

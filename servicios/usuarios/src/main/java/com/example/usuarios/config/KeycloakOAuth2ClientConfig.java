package com.example.usuarios.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Configuration
public class KeycloakOAuth2ClientConfig {

    @Value("${KEYCLOAK_BASE_URL:}")
    private String keycloakBaseUrl;

    @Value("${KEYCLOAK_PUBLIC_URL:}")
    private String keycloakPublicUrl;

    @Value("${KEYCLOAK_REALM:}")
    private String keycloakRealm;

    @Value("${KEYCLOAK_CLIENT_ID:}")
    private String keycloakClientId;

    @Value("${KEYCLOAK_CLIENT_SECRET:}")
    private String keycloakClientSecret;

    @Value("${KEYCLOAK_CLIENT_SCOPES:openid,profile,email}")
    private String keycloakClientScopes;

    @Value("${security.oauth2.client-redirect-uri:http://localhost:8081/login/oauth2/code/keycloak}")
    private String clientRedirectUri;

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = buildRegistration();
        if (registration == null) {
            return registrationId -> null;
        }
        return new InMemoryClientRegistrationRepository(registration);
    }

    private ClientRegistration buildRegistration() {
        if (!StringUtils.hasText(keycloakRealm) || !StringUtils.hasText(keycloakClientId)) {
            return null;
        }

        String internalBase = normalizeBaseUrl(StringUtils.hasText(keycloakBaseUrl) ? keycloakBaseUrl : keycloakPublicUrl);
        String publicBase = normalizeBaseUrl(StringUtils.hasText(keycloakPublicUrl) ? keycloakPublicUrl : internalBase);

        Assert.hasText(internalBase, "Debe definir KEYCLOAK_BASE_URL o KEYCLOAK_PUBLIC_URL");

        String realmPath = "/realms/" + keycloakRealm;

        String authorizationUri = publicBase + realmPath + "/protocol/openid-connect/auth";
        String issuerUri = publicBase + realmPath;
        String tokenUri = internalBase + realmPath + "/protocol/openid-connect/token";
        String jwkSetUri = internalBase + realmPath + "/protocol/openid-connect/certs";

        List<String> scopes = Arrays.stream(keycloakClientScopes.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        ClientAuthenticationMethod authenticationMethod = StringUtils.hasText(keycloakClientSecret)
                ? ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                : ClientAuthenticationMethod.NONE;

        return ClientRegistration.withRegistrationId("keycloak")
                .clientId(keycloakClientId)
                .clientSecret(StringUtils.hasText(keycloakClientSecret) ? keycloakClientSecret : null)
                .clientAuthenticationMethod(authenticationMethod)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(scopes.toArray(String[]::new))
                // Redirección del proveedor hacia este backend
                .redirectUri(clientRedirectUri)
                .authorizationUri(authorizationUri)
                .tokenUri(tokenUri)
                .userNameAttributeName("preferred_username")
                .jwkSetUri(jwkSetUri)
                .issuerUri(issuerUri)
                .clientName("Keycloak")
                .build();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return baseUrl;
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}

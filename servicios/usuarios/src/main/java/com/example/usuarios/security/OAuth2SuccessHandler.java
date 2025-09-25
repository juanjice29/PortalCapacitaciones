package com.example.usuarios.security;

import com.example.usuarios.entity.UserEntity;
import com.example.usuarios.repository.UserRepository;
import com.example.usuarios.exception.ResourceNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final String redirectUri;

    public OAuth2SuccessHandler(JwtTokenProvider tokenProvider,
                                UserRepository userRepository,
                                @Value("${security.oauth2.redirect-uri}") String redirectUri) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.redirectUri = redirectUri;
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = (String) oauthUser.getAttributes().getOrDefault("email", oauthUser.getAttributes().get("preferred_username"));
        if (email == null) {
            throw new ResourceNotFoundException("El proveedor OAuth2 no envio el email del usuario");
        }
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario OAuth2 no encontrado"));
        String token = tokenProvider.generateToken(user);
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

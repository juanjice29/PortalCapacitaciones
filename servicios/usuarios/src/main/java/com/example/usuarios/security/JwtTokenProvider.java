package com.example.usuarios.security;

import com.example.usuarios.config.JwtProperties;
import com.example.usuarios.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final Key signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        Assert.hasText(properties.getSecret(), "La propiedad jwt.secret es obligatoria");
        Assert.isTrue(properties.getSecret().length() >= 32, "La propiedad jwt.secret debe tener al menos 32 caracteres");
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(properties.getAccessTokenValiditySeconds());
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .addClaims(Map.of(
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "fullName", user.getFullName()
                ))
                .signWith(signingKey)
                .compact();
    }

    public Authentication buildAuthentication(String token) {
        Claims claims = parse(token);
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class));
        User principal = new User(claims.get("email", String.class), token, List.of(authority));
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getAccessTokenValiditySeconds() {
        return properties.getAccessTokenValiditySeconds();
    }

    private Claims parse(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }
}

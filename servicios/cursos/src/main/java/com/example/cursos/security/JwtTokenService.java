package com.example.cursos.security;

import com.example.cursos.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class JwtTokenService {

    private final Key signingKey;

    public JwtTokenService(JwtProperties properties) {
        Assert.hasText(properties.getSecret(), "La propiedad jwt.secret es obligatoria");
        Assert.isTrue(properties.getSecret().length() >= 32, "La propiedad jwt.secret debe tener al menos 32 caracteres");
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public JwtUserPrincipal parse(String token) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            Claims claims = jws.getBody();
            UUID userId = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            return new JwtUserPrincipal(userId, email, role);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Token invalido", ex);
        }
    }
}

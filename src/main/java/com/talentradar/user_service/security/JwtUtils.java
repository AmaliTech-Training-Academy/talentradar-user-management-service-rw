package com.talentradar.user_service.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.talentradar.user_service.model.CustomUserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.jwt.expirationMs:86400000}")
    private String jwtExpirationMS;

    // Generate Jwt token
    public String generateJwtTokenFromUserId(CustomUserDetails userDetails) {
        String userRole = userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + Integer.parseInt(jwtExpirationMS));
        return Jwts.builder().subject(String.valueOf(userDetails
                .getUserId())).issuedAt(new Date())
                .issuedAt(now)
                .claim("email", userDetails.getEmail())
                .claim("fullName", userDetails.getUser().getFullName())
                .claim("role", userRole)
                .expiration(expiryDate)
                .signWith(key()).compact();
    }

    // Key encription
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

}

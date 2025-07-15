package com.talentradar.user_service.security;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;

import com.talentradar.user_service.model.CustomUserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.security.jwt.expirationMs}")
    private String jwtExpirationMS;

    // Get Jwt from header of the request
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        logger.info("Authorization Header: { }", bearerToken);
        if (bearerToken == null) {
            return null;
        }

        String token = bearerToken.substring(7);

        logger.info("Token: { }", token);

        return token;
    }

    // Generate Jwt token
    public String generateJwtTokenFromEmail(CustomUserDetails userDetails) {
        String userRole = userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
        return Jwts.builder().subject(String.valueOf(userDetails
                .getUserId())).issuedAt(new Date())
                .claim("email", userDetails.getEmail())
                .claim("fullName", userDetails.getUser().getFullName())
                .claim("role", userRole)
                .claim("userId", userDetails.getUserId())
                .expiration(new Date((new Date()).getTime() + Long.parseLong(jwtExpirationMS)))
                .signWith(key()).compact();

    }

    // Key encription
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

}

package com.recipe.backend.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // 32+ character secret key for HMAC SHA-256
    private static final String SECRET = "mysecretkeymysecretkeymysecretkeymysecretkey";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // =====================
    // GENERATE JWT TOKEN
    // =====================
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_" + role); // Store role as claim

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)               // ← Use EMAIL as principal
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // =====================
    // EXTRACT EMAIL (SUBJECT)
    // =====================
    public String extractUsername(String token) {

        return getClaims(token).getSubject(); // returns email
    }

    // =====================
    // EXTRACT ROLE
    // =====================
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // =====================
    // VALIDATE TOKEN
    // =====================
    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // =====================
    // GET CLAIMS
    // =====================
    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}


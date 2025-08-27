package com.friends.friends.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtResetTokenService {

    @Value("${jwt.reset.secret}")
    private String secretBase64;
    @Value("${jwt.reset.ttl-minutes}")
    private int ttlMinutes;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
    }

    /**
     * Vydá krátkodobý reset token. Claims: sub, aud, exp.
     */
    public String issue(long userId) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlMinutes * 60L);

        return Jwts.builder()
                .setSubject(Long.toString(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key(), SignatureAlgorithm.HS256)  // JJWT 0.11.x
                .compact();
    }

    /**
     * Ověří podpis/exp/audience a vrátí userId. Vyhazuje anglické hlášky.
     */
    public long verifyAndGetUserId(String token) {
        try {
            var jws = Jwts.parserBuilder()
                    .setSigningKey(key())     // DOPLNĚNO
                    .build()
                    .parseClaimsJws(token);   // DOPLNĚNO

            Claims c = jws.getBody();

            return Long.parseLong(c.getSubject());

        } catch (ExpiredJwtException e) {
            throw new JwtValidationException("Token expired");
        } catch (io.jsonwebtoken.security.SecurityException e) {
            throw new JwtValidationException("Invalid token signature");
        } catch (JwtException e) {
            throw new JwtValidationException("Malformed token");
        } catch (Exception e) {
            throw new JwtValidationException("Token validation failed");
        }
    }

    public static class JwtValidationException extends RuntimeException {
        public JwtValidationException(String message) {
            super(message);
        }
    }
}
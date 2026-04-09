package com.app.mie.ayam.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.app.mie.ayam.config.AppJwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final String secret;
	private final long expirationSeconds;

	public JwtService(AppJwtProperties properties) {
		this.secret = properties.secret();
		this.expirationSeconds = properties.expirationSeconds();
	}

	public String generateToken(UserDetails userDetails) {
		Instant now = Instant.now();
		Instant expiration = now.plusSeconds(expirationSeconds);
		return Jwts.builder()
			.subject(userDetails.getUsername())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiration))
			.claims(Map.of())
			.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
			.compact();
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		Date expiration = extractAllClaims(token).getExpiration();
		return expiration.before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
			.verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}

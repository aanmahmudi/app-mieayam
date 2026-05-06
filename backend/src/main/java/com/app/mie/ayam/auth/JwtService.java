package com.app.mie.ayam.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.springframework.core.env.Environment;
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

	public JwtService(AppJwtProperties properties, Environment environment) {
		boolean prod = isProd(environment);

		String configuredSecret = properties.secret();
		if (configuredSecret == null || configuredSecret.isBlank()) {
			configuredSecret = "dev-secret-change-me-please-32-chars-min";
		}
		// if (configuredSecret.length() < 32) {
		// 	throw new IllegalStateException("JWT secret minimal 32 karakter.");
		// }

		long configuredExpiration = properties.expirationSeconds();
		if (configuredExpiration <= 0) {
			configuredExpiration = prod ? 86_400L : 315_360_000L;
		}
		if (prod) {
			configuredExpiration = Math.min(configuredExpiration, 2_592_000L);
		}

		this.secret = configuredSecret;
		this.expirationSeconds = configuredExpiration;
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

	private static boolean isProd(Environment environment) {
		for (String p : environment.getActiveProfiles()) {
			if ("prod".equalsIgnoreCase(p) || "production".equalsIgnoreCase(p)) return true;
		}
		String env = environment.getProperty("APP_ENV");
		if (env == null || env.isBlank()) env = environment.getProperty("app.env");
		if (env == null || env.isBlank()) env = System.getenv("APP_ENV");
		return env != null && (env.equalsIgnoreCase("prod") || env.equalsIgnoreCase("production"));
	}
}

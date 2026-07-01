package com.app.mie.ayam.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.app.mie.ayam.auth.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtAuthenticationFilter jwtAuthenticationFilter
	) throws Exception {
		return http
			.csrf(csrf -> csrf.disable())
			.cors(withDefaults())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(eh -> eh
				.authenticationEntryPoint((request, response, ex) -> {
					response.setStatus(401);
					response.setCharacterEncoding(StandardCharsets.UTF_8.name());
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.getWriter().write("{\"message\":\"Sesi habis. Silakan login lagi.\"}");
				})
				.accessDeniedHandler((request, response, ex) -> {
					response.setStatus(403);
					response.setCharacterEncoding(StandardCharsets.UTF_8.name());
					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.getWriter().write("{\"message\":\"Akses ditolak.\"}");
				})
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
				.requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
				.requestMatchers("/uploads/**").permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource(AppCorsProperties properties, Environment environment) {
		List<String> allowedOrigins = properties.allowedOrigins();
		boolean prod = isProd(environment);
		if (allowedOrigins == null || allowedOrigins.isEmpty()) {
			allowedOrigins = List.of("*");
		}
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
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

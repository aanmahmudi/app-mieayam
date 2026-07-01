package com.app.mie.ayam.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.auth.dto.LoginRequest;
import com.app.mie.ayam.auth.dto.RegisterRequest;
import com.app.mie.ayam.auth.security.JwtService;
import com.app.mie.ayam.user.entity.AppUser;
import com.app.mie.ayam.user.repository.AppUserRepository;

@Service
public class AuthService {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;

	public AuthService(
		AppUserRepository appUserRepository,
		PasswordEncoder passwordEncoder,
		UserDetailsService userDetailsService,
		JwtService jwtService
	) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
	}

	public void register(RegisterRequest request) {
		if (appUserRepository.existsByUsername(request.username())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username sudah dipakai");
		}
		AppUser user = new AppUser(request.username(), passwordEncoder.encode(request.password()), "ROLE_USER");
		appUserRepository.save(user);
	}

	public String login(LoginRequest request) {
		AppUser user = appUserRepository.findByUsername(request.username())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username/password salah"));
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username/password salah");
		}
		UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
		return jwtService.generateToken(userDetails);
	}
}

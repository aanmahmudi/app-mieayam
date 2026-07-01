package com.app.mie.ayam.auth.controller;

import java.security.Principal;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.app.mie.ayam.auth.dto.AuthResponse;
import com.app.mie.ayam.auth.dto.LoginRequest;
import com.app.mie.ayam.auth.dto.MeResponse;
import com.app.mie.ayam.auth.dto.RegisterRequest;
import com.app.mie.ayam.auth.service.AuthService;
import com.app.mie.ayam.user.repository.AppUserRepository;

import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final AppUserRepository appUserRepository;

	public AuthController(AuthService authService, AppUserRepository appUserRepository) {
		this.authService = authService;
		this.appUserRepository = appUserRepository;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public void register(@Valid @RequestBody RegisterRequest request) {
		authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		String token = authService.login(request);
		return new AuthResponse(token);
	}

	@GetMapping("/me")
	public MeResponse me(Principal principal) {
		String username = requireUsername(principal);
		String rolesCsv = appUserRepository.findByUsername(username).orElseThrow().getRolesCsv();
		return new MeResponse(username, Arrays.stream(rolesCsv.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList());
	}

	private static String requireUsername(Principal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesi habis. Silakan login lagi.");
		}
		return principal.getName();
	}
}

package com.app.mie.ayam.user;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements ApplicationRunner {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;

	public AdminSeeder(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		String username = System.getenv("ADMIN_USERNAME");
		String password = System.getenv("ADMIN_PASSWORD");
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			return;
		}

		if (!appUserRepository.existsByUsername(username)) {
			AppUser admin = new AppUser(username, passwordEncoder.encode(password), "ROLE_ADMIN,ROLE_USER");
			appUserRepository.save(admin);
		}
	}
}

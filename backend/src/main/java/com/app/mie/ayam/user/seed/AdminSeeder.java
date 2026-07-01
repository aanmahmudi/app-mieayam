package com.app.mie.ayam.user.seed;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.mie.ayam.user.entity.AppUser;
import com.app.mie.ayam.user.repository.AppUserRepository;

@Component
public class AdminSeeder implements ApplicationRunner {

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final Environment environment;

	public AdminSeeder(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, Environment environment) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.environment = environment;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		boolean prod = isProd(environment);
		String username = System.getenv("ADMIN_USERNAME");
		String password = System.getenv("ADMIN_PASSWORD");
		if (!prod && (username == null || username.isBlank())) username = "admin";
		if (!prod && (password == null || password.isBlank())) password = "admin";
		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			return;
		}

		String rolesCsv = System.getenv("ADMIN_ROLES");
		if (rolesCsv == null || rolesCsv.isBlank()) rolesCsv = "ROLE_ADMIN,ROLE_USER";

		AppUser user = appUserRepository.findByUsername(username).orElse(null);
		String passwordHash = passwordEncoder.encode(password);
		if (user == null) {
			appUserRepository.save(new AppUser(username, passwordHash, rolesCsv));
			return;
		}
		user.setPasswordHash(passwordHash);
		user.setRolesCsv(rolesCsv);
		appUserRepository.save(user);
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

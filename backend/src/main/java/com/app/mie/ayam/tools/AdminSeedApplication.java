package com.app.mie.ayam.tools;

import java.util.List;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.mie.ayam.AppMieAyamApplication;
import com.app.mie.ayam.user.AppUser;
import com.app.mie.ayam.user.AppUserRepository;

@Component
public class AdminSeedApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(AppMieAyamApplication.class)
			.web(WebApplicationType.NONE)
			.logStartupInfo(false)
			.run(args);
		int code = org.springframework.boot.SpringApplication.exit(ctx);
		System.exit(code);
	}

	@Component
	public static class Runner implements org.springframework.boot.ApplicationRunner {

		private final AppUserRepository userRepository;
		private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

		public Runner(AppUserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
			this.userRepository = userRepository;
			this.passwordEncoder = passwordEncoder;
		}

		@Override
		@Transactional
		public void run(org.springframework.boot.ApplicationArguments args) {
			String username = getOption(args, "username");
			String password = getOption(args, "password");
			if (username == null || username.isBlank() || password == null || password.isBlank()) {
				return;
			}

			String roles = "ROLE_ADMIN,ROLE_USER";
			AppUser user = userRepository.findByUsername(username).orElse(null);
			String passwordHash = passwordEncoder.encode(password);
			if (user == null) {
				userRepository.save(new AppUser(username, passwordHash, roles));
				return;
			}

			user.setPasswordHash(passwordHash);
			user.setRolesCsv(roles);
			userRepository.save(user);
		}

		private static String getOption(org.springframework.boot.ApplicationArguments args, String name) {
			List<String> values = args.getOptionValues(name);
			if (values == null || values.isEmpty()) return null;
			return values.get(0);
		}
	}
}

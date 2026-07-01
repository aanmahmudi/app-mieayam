package com.app.mie.ayam.user.service;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.app.mie.ayam.user.entity.AppUser;
import com.app.mie.ayam.user.repository.AppUserRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

	private final AppUserRepository appUserRepository;

	public AppUserDetailsService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AppUser user = appUserRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return new User(user.getUsername(), user.getPasswordHash(), authoritiesFromCsv(user.getRolesCsv()));
	}

	private static Collection<? extends GrantedAuthority> authoritiesFromCsv(String csv) {
		return Arrays.stream(csv.split(","))
			.map(String::trim)
			.filter(s -> !s.isBlank())
			.map(SimpleGrantedAuthority::new)
			.toList();
	}
}

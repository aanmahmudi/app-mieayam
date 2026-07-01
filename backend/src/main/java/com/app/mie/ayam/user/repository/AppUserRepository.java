package com.app.mie.ayam.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.app.mie.ayam.user.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
	Optional<AppUser> findByUsername(String username);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select u from AppUser u where u.username = :username")
	Optional<AppUser> findByUsernameForUpdate(@Param("username") String username);

	boolean existsByUsername(String username);
}

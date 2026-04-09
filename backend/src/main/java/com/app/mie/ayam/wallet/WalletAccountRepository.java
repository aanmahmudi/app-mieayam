package com.app.mie.ayam.wallet;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, Long> {
	Optional<WalletAccount> findByUserUsername(String username);
}


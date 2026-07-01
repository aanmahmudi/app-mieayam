package com.app.mie.ayam.wallet.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.mie.ayam.wallet.entity.WalletAccount;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, Long> {
	Optional<WalletAccount> findByUserUsername(String username);
}

package com.app.mie.ayam.wallet;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
	List<WalletTransaction> findAllByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}


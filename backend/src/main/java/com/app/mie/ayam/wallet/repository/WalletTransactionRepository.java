package com.app.mie.ayam.wallet.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.app.mie.ayam.wallet.entity.WalletTransaction;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
	List<WalletTransaction> findAllByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}

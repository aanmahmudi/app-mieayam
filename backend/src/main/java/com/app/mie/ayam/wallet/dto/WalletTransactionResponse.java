package com.app.mie.ayam.wallet.dto;

import java.time.Instant;

import com.app.mie.ayam.wallet.WalletTransaction;
import com.app.mie.ayam.wallet.WalletTransactionType;

public record WalletTransactionResponse(
	Long id,
	WalletTransactionType type,
	int amount,
	int balanceBefore,
	int balanceAfter,
	Long orderId,
	Instant createdAt
) {
	public static WalletTransactionResponse from(WalletTransaction tx) {
		return new WalletTransactionResponse(
			tx.getId(),
			tx.getType(),
			tx.getAmount(),
			tx.getBalanceBefore(),
			tx.getBalanceAfter(),
			tx.getOrderId(),
			tx.getCreatedAt()
		);
	}
}


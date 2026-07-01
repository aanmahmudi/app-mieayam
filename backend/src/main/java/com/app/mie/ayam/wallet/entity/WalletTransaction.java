package com.app.mie.ayam.wallet.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private WalletAccount account;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private WalletTransactionType type;

	@Column(nullable = false)
	private int amount;

	@Column(nullable = false)
	private int balanceBefore;

	@Column(nullable = false)
	private int balanceAfter;

	private Long orderId;

	@Column(nullable = false)
	private Instant createdAt;

	protected WalletTransaction() {
	}

	public WalletTransaction(
		WalletAccount account,
		WalletTransactionType type,
		int amount,
		int balanceBefore,
		int balanceAfter,
		Long orderId,
		Instant createdAt
	) {
		this.account = account;
		this.type = type;
		this.amount = amount;
		this.balanceBefore = balanceBefore;
		this.balanceAfter = balanceAfter;
		this.orderId = orderId;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public WalletAccount getAccount() {
		return account;
	}

	public WalletTransactionType getType() {
		return type;
	}

	public int getAmount() {
		return amount;
	}

	public int getBalanceBefore() {
		return balanceBefore;
	}

	public int getBalanceAfter() {
		return balanceAfter;
	}

	public Long getOrderId() {
		return orderId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}

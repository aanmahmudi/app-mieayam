package com.app.mie.ayam.wallet.entity;

import java.time.Instant;

import com.app.mie.ayam.user.entity.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallet_account")
public class WalletAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private AppUser user;

	@Column(nullable = false)
	private int balance;

	@Column(nullable = false)
	private Instant createdAt;

	protected WalletAccount() {
	}

	public WalletAccount(AppUser user, int balance, Instant createdAt) {
		this.user = user;
		this.balance = balance;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public AppUser getUser() {
		return user;
	}

	public int getBalance() {
		return balance;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}
}

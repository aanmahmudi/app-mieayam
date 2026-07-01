package com.app.mie.ayam.wallet.service;

import java.time.Instant;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.user.entity.AppUser;
import com.app.mie.ayam.user.repository.AppUserRepository;
import com.app.mie.ayam.wallet.entity.WalletAccount;
import com.app.mie.ayam.wallet.entity.WalletTransaction;
import com.app.mie.ayam.wallet.entity.WalletTransactionType;
import com.app.mie.ayam.wallet.repository.WalletAccountRepository;
import com.app.mie.ayam.wallet.repository.WalletTransactionRepository;

@Service
public class WalletService {

	private static final int DUMMY_INITIAL_BALANCE = 50_000_000;
	private final boolean devMode;

	private final WalletAccountRepository accountRepository;
	private final WalletTransactionRepository transactionRepository;
	private final AppUserRepository userRepository;

	public WalletService(
		WalletAccountRepository accountRepository,
		WalletTransactionRepository transactionRepository,
		AppUserRepository userRepository,
		Environment environment
	) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository;
		this.devMode = !isProd(environment);
	}

	@Transactional
	public WalletAccount getOrCreateAccount(String username) {
		AppUser user = userRepository.findByUsernameForUpdate(username).orElseThrow();
		WalletAccount existing = accountRepository.findByUserUsername(username).orElse(null);
		if (existing != null) {
			if (devMode && existing.getBalance() == 0) {
				existing.setBalance(DUMMY_INITIAL_BALANCE);
				return accountRepository.save(existing);
			}
			return existing;
		}

		int initialBalance = devMode ? DUMMY_INITIAL_BALANCE : 0;
		WalletAccount created = new WalletAccount(user, initialBalance, Instant.now());
		return accountRepository.save(created);
	}

	@Transactional
	public int getBalance(String username) {
		return getOrCreateAccount(username).getBalance();
	}

	@Transactional
	public WalletTransaction topUp(String username, int amount) {
		if (amount <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nominal top up tidak valid.");
		}

		WalletAccount account = getOrCreateAccount(username);
		int before = account.getBalance();
		int after = before + amount;
		account.setBalance(after);
		accountRepository.save(account);

		WalletTransaction tx = new WalletTransaction(
			account,
			WalletTransactionType.TOP_UP,
			amount,
			before,
			after,
			null,
			Instant.now()
		);
		return transactionRepository.save(tx);
	}

	@Transactional
	public WalletTransaction payFromBalance(String username, int total, Long orderId) {
		if (total <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total tidak valid.");
		}

		WalletAccount account = getOrCreateAccount(username);
		int before = account.getBalance();
		if (before < total) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo tidak cukup.");
		}

		int after = before - total;
		account.setBalance(after);
		accountRepository.save(account);

		WalletTransaction tx = new WalletTransaction(
			account,
			WalletTransactionType.PAYMENT,
			-total,
			before,
			after,
			orderId,
			Instant.now()
		);
		return transactionRepository.save(tx);
	}

	@Transactional
	public WalletTransaction refundToBalance(String username, int amount, Long orderId) {
		if (amount <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nominal refund tidak valid.");
		}

		WalletAccount account = getOrCreateAccount(username);
		int before = account.getBalance();
		int after = before + amount;
		account.setBalance(after);
		accountRepository.save(account);

		WalletTransaction tx = new WalletTransaction(
			account,
			WalletTransactionType.REFUND,
			amount,
			before,
			after,
			orderId,
			Instant.now()
		);
		return transactionRepository.save(tx);
	}

	@Transactional
	public List<WalletTransaction> listTransactions(String username, int limit) {
		WalletAccount account = getOrCreateAccount(username);
		int safeLimit = Math.max(1, Math.min(50, limit));
		return transactionRepository.findAllByAccountIdOrderByCreatedAtDesc(account.getId(), PageRequest.of(0, safeLimit));
	}

	private static boolean isProd(Environment environment) {
		for (String p : environment.getActiveProfiles()) {
			if ("prod".equalsIgnoreCase(p) || "production".equalsIgnoreCase(p)) return true;
		}
		String env = environment.getProperty("APP_ENV");
		if (env == null || env.isBlank()) env = environment.getProperty("app.env");
		if (env == null || env.isBlank()) env = System.getenv("APP_ENV");
		return env != null && (env.equalsIgnoreCase("prod") || env.equalsIgnoreCase("production"));
	}
}

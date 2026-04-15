package com.app.mie.ayam.wallet;

import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.user.AppUser;
import com.app.mie.ayam.user.AppUserRepository;

@Service
public class WalletService {

	private static final int DUMMY_INITIAL_BALANCE = 50_000_000;

	private final WalletAccountRepository accountRepository;
	private final WalletTransactionRepository transactionRepository;
	private final AppUserRepository userRepository;

	public WalletService(
		WalletAccountRepository accountRepository,
		WalletTransactionRepository transactionRepository,
		AppUserRepository userRepository
	) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public WalletAccount getOrCreateAccount(String username) {
		return accountRepository.findByUserUsername(username).map(existing -> {
			if (existing.getBalance() == 0) {
				existing.setBalance(DUMMY_INITIAL_BALANCE);
				return accountRepository.save(existing);
			}
			return existing;
		}).orElseGet(() -> {
			AppUser user = userRepository.findByUsername(username).orElseThrow();
			try {
				WalletAccount created = new WalletAccount(user, DUMMY_INITIAL_BALANCE, Instant.now());
				return accountRepository.save(created);
			} catch (DataIntegrityViolationException ex) {
				WalletAccount existing = accountRepository.findByUserUsername(username).orElseThrow(() -> ex);
				if (existing.getBalance() == 0) {
					existing.setBalance(DUMMY_INITIAL_BALANCE);
					return accountRepository.save(existing);
				}
				return existing;
			}
		});
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
	public List<WalletTransaction> listTransactions(String username, int limit) {
		WalletAccount account = getOrCreateAccount(username);
		int safeLimit = Math.max(1, Math.min(50, limit));
		return transactionRepository.findAllByAccountIdOrderByCreatedAtDesc(account.getId(), PageRequest.of(0, safeLimit));
	}
}

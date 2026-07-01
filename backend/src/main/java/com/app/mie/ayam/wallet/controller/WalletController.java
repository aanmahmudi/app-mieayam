package com.app.mie.ayam.wallet.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.wallet.service.WalletService;
import com.app.mie.ayam.wallet.dto.TopUpRequest;
import com.app.mie.ayam.wallet.dto.WalletBalanceResponse;
import com.app.mie.ayam.wallet.dto.WalletTransactionResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

	private final WalletService walletService;

	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}

	@GetMapping("/me")
	public WalletBalanceResponse me(Principal principal) {
		int balance = walletService.getBalance(requireUsername(principal));
		return new WalletBalanceResponse(balance);
	}

	@PostMapping("/topup")
	public WalletBalanceResponse topUp(Principal principal, @Valid @RequestBody TopUpRequest request) {
		String username = requireUsername(principal);
		walletService.topUp(username, request.amount());
		int balance = walletService.getBalance(username);
		return new WalletBalanceResponse(balance);
	}

	@GetMapping("/transactions")
	public List<WalletTransactionResponse> transactions(Principal principal, @RequestParam(defaultValue = "10") int limit) {
		return walletService.listTransactions(requireUsername(principal), limit).stream().map(WalletTransactionResponse::from).toList();
	}

	private static String requireUsername(Principal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesi habis. Silakan login lagi.");
		}
		return principal.getName();
	}
}

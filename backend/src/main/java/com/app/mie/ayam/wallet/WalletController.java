package com.app.mie.ayam.wallet;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
		int balance = walletService.getBalance(principal.getName());
		return new WalletBalanceResponse(balance);
	}

	@PostMapping("/topup")
	public WalletBalanceResponse topUp(Principal principal, @Valid @RequestBody TopUpRequest request) {
		walletService.topUp(principal.getName(), request.amount());
		int balance = walletService.getBalance(principal.getName());
		return new WalletBalanceResponse(balance);
	}

	@GetMapping("/transactions")
	public List<WalletTransactionResponse> transactions(Principal principal, @RequestParam(defaultValue = "10") int limit) {
		return walletService.listTransactions(principal.getName(), limit).stream().map(WalletTransactionResponse::from).toList();
	}
}


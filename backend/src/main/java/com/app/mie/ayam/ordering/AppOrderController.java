package com.app.mie.ayam.ordering;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.ordering.dto.CreateOrderRequest;
import com.app.mie.ayam.ordering.dto.OrderResponse;
import com.app.mie.ayam.ordering.dto.PayOrderRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

@RestController
@RequestMapping("/api/orders")
public class AppOrderController {

	private final AppOrderService orderService;

	public AppOrderController(AppOrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponse create(Principal principal, @Valid @RequestBody CreateOrderRequest request) {
		return orderService.createOrder(requireUsername(principal), request);
	}

	@GetMapping("/my")
	public List<OrderResponse> listMyOrders(Principal principal) {
		return orderService.listOrders(requireUsername(principal));
	}

	@PostMapping("/{orderId}/pay")
	public OrderResponse pay(Principal principal, @PathVariable Long orderId, @Valid @RequestBody PayOrderRequest request) {
		return orderService.payOrder(requireUsername(principal), orderId, request);
	}

	@PostMapping("/{orderId}/cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel(Principal principal, @PathVariable Long orderId) {
		orderService.cancelOrder(requireUsername(principal), orderId);
	}

	public record ShareReceiptRequest(
		@Email String email,
		String whatsapp
	) {
	}

	public record ShareReceiptResponse(
		boolean emailSent,
		boolean whatsappSent
	) {
	}

	@PostMapping("/{orderId}/share-receipt")
	public ShareReceiptResponse shareReceipt(Principal principal, @PathVariable Long orderId, @Valid @RequestBody ShareReceiptRequest request) {
		String username = requireUsername(principal);
		String email = request == null ? null : request.email();
		String whatsapp = request == null ? null : request.whatsapp();
		if ((email == null || email.isBlank()) && (whatsapp == null || whatsapp.isBlank())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Isi email atau nomor WhatsApp.");
		}
		return orderService.shareReceipt(username, orderId, email, whatsapp);
	}

	private static String requireUsername(Principal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesi habis. Silakan login lagi.");
		}
		return principal.getName();
	}
}

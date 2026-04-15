package com.app.mie.ayam.ordering.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.mie.ayam.ordering.admin.dto.AdminPendingOrderResponse;
import com.app.mie.ayam.ordering.admin.dto.ConfirmCashPaymentRequest;
import com.app.mie.ayam.ordering.dto.OrderResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

	private final AdminOrderService adminOrderService;

	public AdminOrderController(AdminOrderService adminOrderService) {
		this.adminOrderService = adminOrderService;
	}

	@GetMapping("/pending")
	public List<AdminPendingOrderResponse> pending(@RequestParam(defaultValue = "30") int limit) {
		return adminOrderService.listPendingOrders(limit);
	}

	@PostMapping("/{orderId}/confirm-cash")
	public OrderResponse confirmCash(
		@PathVariable Long orderId,
		@Valid @RequestBody(required = false) ConfirmCashPaymentRequest request
	) {
		return adminOrderService.confirmCashPayment(orderId, request);
	}
}


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

import com.app.mie.ayam.ordering.dto.CreateOrderRequest;
import com.app.mie.ayam.ordering.dto.OrderResponse;
import com.app.mie.ayam.ordering.dto.PayOrderRequest;

import jakarta.validation.Valid;

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
		return orderService.createOrder(principal.getName(), request);
	}

	@GetMapping("/my")
	public List<OrderResponse> listMyOrders(Principal principal) {
		return orderService.listOrders(principal.getName());
	}

	@PostMapping("/{orderId}/pay")
	public OrderResponse pay(Principal principal, @PathVariable Long orderId, @Valid @RequestBody PayOrderRequest request) {
		return orderService.payOrder(principal.getName(), orderId, request);
	}
}

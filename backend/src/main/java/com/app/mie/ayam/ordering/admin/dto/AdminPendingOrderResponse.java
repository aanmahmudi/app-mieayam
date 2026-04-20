package com.app.mie.ayam.ordering.admin.dto;

import java.time.Instant;
import java.util.List;

import com.app.mie.ayam.ordering.AppOrder;
import com.app.mie.ayam.ordering.AppOrderPayment;
import com.app.mie.ayam.ordering.PaymentMethod;
import com.app.mie.ayam.ordering.dto.OrderItemResponse;

public record AdminPendingOrderResponse(
	Long id,
	Instant createdAt,
	String username,
	PaymentMethod paymentMethod,
	int total,
	List<OrderItemResponse> items
) {
	public static AdminPendingOrderResponse from(AppOrder order, AppOrderPayment payment) {
		String username = order.getUser().getUsername();
		return new AdminPendingOrderResponse(
			order.getId(),
			order.getCreatedAt(),
			username,
			payment.getMethod(),
			order.getTotal(),
			order.getItems().stream().map(OrderItemResponse::from).toList()
		);
	}
}

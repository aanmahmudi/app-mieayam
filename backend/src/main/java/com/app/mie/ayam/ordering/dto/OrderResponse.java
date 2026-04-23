package com.app.mie.ayam.ordering.dto;

import java.time.Instant;
import java.util.List;

import com.app.mie.ayam.ordering.AppOrderPayment;
import com.app.mie.ayam.ordering.OrderStatus;
import com.app.mie.ayam.ordering.PaymentMethod;
import com.app.mie.ayam.ordering.AppOrder;

public record OrderResponse(
	Long id,
	Instant createdAt,
	OrderStatus status,
	int total,
	PaymentMethod paymentMethod,
	String bank,
	Instant paidAt,
	int amountPaid,
	int changeAmount,
	List<OrderItemResponse> items
) {
	public static OrderResponse from(AppOrder order) {
		return from(order, null);
	}

	public static OrderResponse from(AppOrder order, AppOrderPayment payment) {
		OrderStatus status = payment != null && payment.isConfirmed() ? OrderStatus.PAID : OrderStatus.CREATED;
		return new OrderResponse(
			order.getId(),
			order.getCreatedAt(),
			status,
			order.getTotal(),
			payment == null ? null : payment.getMethod(),
			payment == null ? null : payment.getBank(),
			payment == null ? null : payment.getPaidAt(),
			payment == null ? 0 : payment.getAmountPaid(),
			payment == null ? 0 : payment.getChangeAmount(),
			order.getItems().stream().map(OrderItemResponse::from).toList()
		);
	}
}

package com.app.mie.ayam.ordering.admin.dto;

import java.time.Instant;

import com.app.mie.ayam.ordering.OrderStatus;
import com.app.mie.ayam.ordering.PaymentMethod;

public record AdminOrderSummaryResponse(
	Long id,
	String username,
	Instant createdAt,
	OrderStatus status,
	int total,
	PaymentMethod paymentMethod,
	String bank,
	Instant paidAt
) {
}


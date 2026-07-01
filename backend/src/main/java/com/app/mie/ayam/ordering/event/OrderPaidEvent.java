package com.app.mie.ayam.ordering.event;

import java.time.Instant;
import java.util.List;

public record OrderPaidEvent(
	Long orderId,
	String username,
	String paymentMethod,
	String bank,
	int total,
	int amountPaid,
	int changeAmount,
	Instant createdAt,
	Instant paidAt,
	List<OrderPaidItemEvent> items
) {
}

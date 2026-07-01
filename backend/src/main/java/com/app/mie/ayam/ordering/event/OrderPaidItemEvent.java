package com.app.mie.ayam.ordering.event;

public record OrderPaidItemEvent(
	String name,
	String category,
	int quantity,
	int priceEach,
	int subtotal
) {
}

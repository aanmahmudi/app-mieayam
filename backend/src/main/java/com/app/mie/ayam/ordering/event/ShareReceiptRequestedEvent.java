package com.app.mie.ayam.ordering.event;

public record ShareReceiptRequestedEvent(
	Long orderId,
	String username,
	String email,
	String whatsapp
) {
}

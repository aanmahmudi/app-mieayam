package com.app.mie.ayam.ordering.dto;

import com.app.mie.ayam.menu.MenuCategory;
import com.app.mie.ayam.ordering.AppOrderItem;

public record OrderItemResponse(
	Long menuItemId,
	String name,
	MenuCategory category,
	String unit,
	int priceEach,
	int quantity,
	int subtotal,
	String imageUrl
) {
	public static OrderItemResponse from(AppOrderItem item) {
		Long menuItemId = item.getMenuItem() != null ? item.getMenuItem().getId() : null;
		int subtotal = item.getPriceEach() * item.getQuantity();
		return new OrderItemResponse(
			menuItemId,
			item.getName(),
			item.getCategory(),
			item.getUnit(),
			item.getPriceEach(),
			item.getQuantity(),
			subtotal,
			item.getImageUrl()
		);
	}
}


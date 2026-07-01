package com.app.mie.ayam.menu.dto;

import com.app.mie.ayam.menu.entity.MenuCategory;
import com.app.mie.ayam.menu.entity.MenuItem;

public record MenuItemResponse(Long id, String name, MenuCategory category, String unit, int price, String imageUrl) {
	public static MenuItemResponse from(MenuItem item) {
		return new MenuItemResponse(
			item.getId(),
			item.getName(),
			item.getCategory(),
			item.getUnit(),
			item.getPrice(),
			item.getImageUrl()
		);
	}
}

package com.app.mie.ayam.menu.admin.dto;

import com.app.mie.ayam.menu.entity.MenuCategory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminUpsertMenuItemRequest(
	@NotBlank String name,
	@NotNull MenuCategory category,
	String unit,
	@Min(0) int price,
	String imageUrl
) {
}

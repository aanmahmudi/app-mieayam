package com.app.mie.ayam.ordering.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
	@NotNull Long menuItemId,
	@NotNull @Min(1) @Max(99) Integer quantity
) {
}


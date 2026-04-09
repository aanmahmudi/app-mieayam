package com.app.mie.ayam.ordering.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(@NotEmpty @Valid List<CreateOrderItemRequest> items) {
}


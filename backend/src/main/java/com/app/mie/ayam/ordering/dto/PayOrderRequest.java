package com.app.mie.ayam.ordering.dto;

import com.app.mie.ayam.ordering.PaymentMethod;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PayOrderRequest(@NotNull PaymentMethod method, @Min(0) Integer amountPaid, String bank) {
}

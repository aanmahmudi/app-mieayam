package com.app.mie.ayam.ordering.admin.dto;

import jakarta.validation.constraints.Min;

public record ConfirmCashPaymentRequest(@Min(0) Integer amountPaid) {
}


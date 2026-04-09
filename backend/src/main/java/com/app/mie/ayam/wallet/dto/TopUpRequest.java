package com.app.mie.ayam.wallet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TopUpRequest(@Min(1000) @Max(10000000) int amount) {
}


package com.app.mie.ayam.ordering.admin.dto;

import java.time.Instant;

public record AdminWeeklyStatsResponse(
	Instant weekStart,
	long totalOrders,
	long paidOrders,
	long paidTotal
) {
}


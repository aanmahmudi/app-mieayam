package com.app.mie.ayam.ordering.admin.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.ordering.admin.dto.AdminOrderSummaryResponse;
import com.app.mie.ayam.ordering.admin.dto.AdminPendingOrderResponse;
import com.app.mie.ayam.ordering.admin.dto.AdminWeeklyStatsResponse;
import com.app.mie.ayam.ordering.admin.dto.ConfirmCashPaymentRequest;
import com.app.mie.ayam.ordering.admin.service.AdminOrderService;
import com.app.mie.ayam.ordering.dto.OrderResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

	private final AdminOrderService adminOrderService;
	private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Jakarta");

	public AdminOrderController(AdminOrderService adminOrderService) {
		this.adminOrderService = adminOrderService;
	}

	@GetMapping("/pending")
	public List<AdminPendingOrderResponse> pending(@RequestParam(defaultValue = "30") int limit) {
		return adminOrderService.listPendingOrders(limit);
	}

	@GetMapping("/recent")
	public List<AdminOrderSummaryResponse> recent(
		@RequestParam(defaultValue = "50") int limit,
		@RequestParam(required = false) String date,
		@RequestParam(required = false) String fromDate,
		@RequestParam(required = false) String toDate
	) {
		Instant[] range = resolveDateRange(date, fromDate, toDate);
		if (range == null) return adminOrderService.listRecentOrders(limit);
		return adminOrderService.listRecentOrdersRange(limit, range[0], range[1]);
	}

	@GetMapping("/weekly-stats")
	public List<AdminWeeklyStatsResponse> weeklyStats(
		@RequestParam(defaultValue = "12") int weeks,
		@RequestParam(required = false) String date,
		@RequestParam(required = false) String fromDate,
		@RequestParam(required = false) String toDate
	) {
		Instant[] range = resolveDateRange(date, fromDate, toDate);
		if (range == null) return adminOrderService.weeklyStats(weeks);
		return adminOrderService.weeklyStatsRange(range[0], range[1]);
	}

	@PostMapping("/{orderId}/confirm")
	public OrderResponse confirm(
		@PathVariable Long orderId,
		@Valid @RequestBody(required = false) ConfirmCashPaymentRequest request
	) {
		return adminOrderService.confirmPayment(orderId, request);
	}

	private static Instant[] resolveDateRange(String date, String fromDate, String toDate) {
		if ((date == null || date.isBlank()) && (fromDate == null || fromDate.isBlank()) && (toDate == null || toDate.isBlank())) {
			return null;
		}
		try {
			LocalDate from;
			LocalDate to;
			if (date != null && !date.isBlank()) {
				from = LocalDate.parse(date);
				to = from;
			} else {
				from = (fromDate != null && !fromDate.isBlank())
					? LocalDate.parse(fromDate)
					: LocalDate.parse(toDate);
				to = (toDate != null && !toDate.isBlank())
					? LocalDate.parse(toDate)
					: from;
			}
			Instant fromInstant = from.atStartOfDay(DEFAULT_ZONE).toInstant();
			Instant toExclusive = to.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();
			return new Instant[] { fromInstant, toExclusive };
		} catch (RuntimeException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tanggal tidak valid.");
		}
	}
}

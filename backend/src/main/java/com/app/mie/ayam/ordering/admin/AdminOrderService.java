package com.app.mie.ayam.ordering.admin;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.ordering.AppOrder;
import com.app.mie.ayam.ordering.AppOrderPayment;
import com.app.mie.ayam.ordering.AppOrderPaymentRepository;
import com.app.mie.ayam.ordering.AppOrderRepository;
import com.app.mie.ayam.ordering.OrderStatus;
import com.app.mie.ayam.ordering.PaymentMethod;
import com.app.mie.ayam.ordering.admin.dto.AdminOrderSummaryResponse;
import com.app.mie.ayam.ordering.admin.dto.AdminPendingOrderResponse;
import com.app.mie.ayam.ordering.admin.dto.AdminWeeklyStatsResponse;
import com.app.mie.ayam.ordering.admin.dto.ConfirmCashPaymentRequest;
import com.app.mie.ayam.ordering.dto.OrderResponse;

@Service
public class AdminOrderService {

	private final AppOrderRepository orderRepository;
	private final AppOrderPaymentRepository paymentRepository;

	public AdminOrderService(AppOrderRepository orderRepository, AppOrderPaymentRepository paymentRepository) {
		this.orderRepository = orderRepository;
		this.paymentRepository = paymentRepository;
	}

	@Transactional(readOnly = true)
	public List<AdminPendingOrderResponse> listPendingOrders(int limit) {
		int safeLimit = Math.max(1, Math.min(100, limit));
		List<AppOrder> orders = orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit));
		return orders.stream()
			.map(order -> new java.util.AbstractMap.SimpleEntry<>(order, paymentRepository.findByOrderId(order.getId()).orElse(null)))
			.filter(entry -> entry.getValue() != null)
			.filter(entry -> !entry.getValue().isConfirmed())
			.filter(entry -> entry.getValue().getMethod() == PaymentMethod.QRIS)
			.map(entry -> AdminPendingOrderResponse.from(entry.getKey(), entry.getValue()))
			.toList();
	}

	@Transactional
	public OrderResponse confirmPayment(Long orderId, ConfirmCashPaymentRequest request) {
		AppOrder order = orderRepository.findById(orderId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order tidak ditemukan."));

		AppOrderPayment payment = paymentRepository.findByOrderId(order.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order belum memiliki data pembayaran."));
		if (payment.isConfirmed()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Order sudah dikonfirmasi.");
		}
		if (payment.getMethod() != PaymentMethod.QRIS) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hanya QRIS yang perlu konfirmasi.");
		}

		int total = order.getTotal();
		int amountPaid = request != null && request.amountPaid() != null ? request.amountPaid() : payment.getAmountPaid();
		if (amountPaid < total) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uang dibayar kurang.");
		}
		int changeAmount = amountPaid - total;

		payment.setAmountPaid(amountPaid);
		payment.setChangeAmount(changeAmount);
		payment.setConfirmed(true);
		AppOrderPayment saved = paymentRepository.save(payment);
		return OrderResponse.from(order, saved);
	}

	@Transactional(readOnly = true)
	public List<AdminOrderSummaryResponse> listRecentOrders(int limit) {
		int safeLimit = Math.max(1, Math.min(200, limit));
		List<AppOrder> orders = orderRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit));
		return orders.stream()
			.map(order -> new java.util.AbstractMap.SimpleEntry<>(order, paymentRepository.findByOrderId(order.getId()).orElse(null)))
			.map(entry -> {
				AppOrder order = entry.getKey();
				AppOrderPayment payment = entry.getValue();
				OrderStatus status = payment != null && payment.isConfirmed() ? OrderStatus.PAID : OrderStatus.CREATED;
				return new AdminOrderSummaryResponse(
					order.getId(),
					order.getUser().getUsername(),
					order.getCreatedAt(),
					status,
					order.getTotal(),
					payment == null ? null : payment.getMethod(),
					payment == null ? null : payment.getBank(),
					payment == null ? null : payment.getPaidAt()
				);
			})
			.toList();
	}

	@Transactional(readOnly = true)
	public List<AdminWeeklyStatsResponse> weeklyStats(int weeks) {
		int safeWeeks = Math.max(1, Math.min(104, weeks));
		Instant from = Instant.now().minus(safeWeeks * 7L, ChronoUnit.DAYS);
		return orderRepository.findWeeklyAdminStats(from).stream()
			.map(row -> new AdminWeeklyStatsResponse(
				row.getWeekStart().toInstant(),
				row.getTotalOrders(),
				row.getPaidOrders(),
				row.getPaidTotal()
			))
			.toList();
	}
}

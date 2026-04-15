package com.app.mie.ayam.ordering.admin;

import java.time.Instant;
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
import com.app.mie.ayam.ordering.PaymentMethod;
import com.app.mie.ayam.ordering.admin.dto.AdminPendingOrderResponse;
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
			.filter(order -> paymentRepository.findByOrderId(order.getId()).isEmpty())
			.map(AdminPendingOrderResponse::from)
			.toList();
	}

	@Transactional
	public OrderResponse confirmCashPayment(Long orderId, ConfirmCashPaymentRequest request) {
		AppOrder order = orderRepository.findById(orderId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order tidak ditemukan."));

		if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Order sudah dibayar.");
		}

		int total = order.getTotal();
		int amountPaid = request != null && request.amountPaid() != null ? request.amountPaid() : total;
		if (amountPaid < total) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uang dibayar kurang.");
		}
		int changeAmount = amountPaid - total;

		AppOrderPayment payment = new AppOrderPayment(order, PaymentMethod.CASH, Instant.now(), amountPaid, changeAmount);
		AppOrderPayment saved = paymentRepository.save(payment);
		return OrderResponse.from(order, saved);
	}
}


package com.app.mie.ayam.ordering;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.menu.MenuItem;
import com.app.mie.ayam.menu.MenuItemRepository;
import com.app.mie.ayam.ordering.dto.CreateOrderItemRequest;
import com.app.mie.ayam.ordering.dto.CreateOrderRequest;
import com.app.mie.ayam.ordering.dto.OrderResponse;
import com.app.mie.ayam.ordering.dto.PayOrderRequest;
import com.app.mie.ayam.user.AppUser;
import com.app.mie.ayam.user.AppUserRepository;
import com.app.mie.ayam.wallet.WalletService;

@Service
public class AppOrderService {

	private final AppOrderRepository orderRepository;
	private final AppOrderPaymentRepository paymentRepository;
	private final AppUserRepository userRepository;
	private final MenuItemRepository menuItemRepository;
	private final WalletService walletService;

	public AppOrderService(
		AppOrderRepository orderRepository,
		AppOrderPaymentRepository paymentRepository,
		AppUserRepository userRepository,
		MenuItemRepository menuItemRepository,
		WalletService walletService
	) {
		this.orderRepository = orderRepository;
		this.paymentRepository = paymentRepository;
		this.userRepository = userRepository;
		this.menuItemRepository = menuItemRepository;
		this.walletService = walletService;
	}

	@Transactional
	public OrderResponse createOrder(String username, CreateOrderRequest request) {
		AppUser user = userRepository.findByUsername(username).orElseThrow();

		Map<Long, Integer> quantitiesByMenuItemId = new HashMap<>();
		for (CreateOrderItemRequest item : request.items()) {
			quantitiesByMenuItemId.merge(item.menuItemId(), item.quantity(), Integer::sum);
		}

		List<Long> ids = new ArrayList<>(quantitiesByMenuItemId.keySet());
		List<MenuItem> menuItems = menuItemRepository.findAllById(ids);
		if (menuItems.size() != ids.size()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ada menu yang tidak ditemukan.");
		}

		int total = 0;
		List<AppOrderItem> orderItems = new ArrayList<>();
		for (MenuItem menuItem : menuItems) {
			int qty = quantitiesByMenuItemId.get(menuItem.getId());
			int subtotal = menuItem.getPrice() * qty;
			total += subtotal;

			AppOrderItem orderItem = new AppOrderItem(
				menuItem,
				menuItem.getName(),
				menuItem.getCategory(),
				menuItem.getUnit(),
				menuItem.getPrice(),
				qty,
				menuItem.getImageUrl()
			);
			orderItems.add(orderItem);
		}

		AppOrder order = new AppOrder(user, Instant.now(), total);
		for (AppOrderItem item : orderItems) {
			order.addItem(item);
		}

		AppOrder saved = orderRepository.save(order);
		return OrderResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public List<OrderResponse> listOrders(String username) {
		List<AppOrder> orders = orderRepository.findAllByUserUsernameOrderByCreatedAtDesc(username);
		return orders.stream()
			.map(order -> OrderResponse.from(order, paymentRepository.findByOrderId(order.getId()).orElse(null)))
			.toList();
	}

	@Transactional
	public OrderResponse payOrder(String username, Long orderId, PayOrderRequest request) {
		Optional<AppOrder> maybeOrder = orderRepository.findByIdAndUserUsername(orderId, username);
		AppOrder order = maybeOrder.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order tidak ditemukan."));

		AppOrderPayment existingPayment = paymentRepository.findByOrderId(order.getId()).orElse(null);
		if (existingPayment != null) {
			if (existingPayment.isConfirmed()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Order sudah dibayar.");
			}
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pembayaran sedang diproses.");
		}

		int total = order.getTotal();
		PaymentMethod method = request.method();

		int amountPaid;
		int changeAmount;
		if (method == PaymentMethod.CASH) {
			amountPaid = request.amountPaid() == null ? total : request.amountPaid();
			if (amountPaid < total) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uang dibayar kurang.");
			}
			changeAmount = amountPaid - total;
		} else if (method == PaymentMethod.QRIS) {
			amountPaid = total;
			changeAmount = 0;
		} else {
			walletService.payFromBalance(username, total, order.getId());
			amountPaid = total;
			changeAmount = 0;
		}

		AppOrderPayment payment = new AppOrderPayment(order, method, Instant.now(), amountPaid, changeAmount);
		if (method == PaymentMethod.BANK) {
			payment.setBank(request.bank());
		}
		AppOrderPayment savedPayment = paymentRepository.save(payment);
		return OrderResponse.from(order, savedPayment);
	}

	@Transactional
	public void cancelOrder(String username, Long orderId) {
		AppOrder order = orderRepository.findByIdAndUserUsername(orderId, username)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order tidak ditemukan."));

		AppOrderPayment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
		if (payment != null && payment.isConfirmed()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Order sudah dibayar dan tidak bisa dibatalkan.");
		}
		if (payment != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pembayaran sedang diproses.");
		}

		orderRepository.delete(order);
	}
}

package com.app.mie.ayam.ordering;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
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
import com.lowagie.text.Element;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

@Service
public class AppOrderService {

	private final AppOrderRepository orderRepository;
	private final AppOrderPaymentRepository paymentRepository;
	private final AppUserRepository userRepository;
	private final MenuItemRepository menuItemRepository;
	private final WalletService walletService;
	private final ObjectProvider<JavaMailSender> mailSenderProvider;
	private final Environment environment;
	private final RestClient restClient;

	public AppOrderService(
		AppOrderRepository orderRepository,
		AppOrderPaymentRepository paymentRepository,
		AppUserRepository userRepository,
		MenuItemRepository menuItemRepository,
		WalletService walletService,
		ObjectProvider<JavaMailSender> mailSenderProvider,
		Environment environment
	) {
		this.orderRepository = orderRepository;
		this.paymentRepository = paymentRepository;
		this.userRepository = userRepository;
		this.menuItemRepository = menuItemRepository;
		this.walletService = walletService;
		this.mailSenderProvider = mailSenderProvider;
		this.environment = environment;
		this.restClient = RestClient.builder().build();
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
			if (payment.getMethod() == PaymentMethod.BANK) {
				walletService.refundToBalance(username, order.getTotal(), order.getId());
			}
			paymentRepository.delete(payment);
		}

		orderRepository.delete(order);
	}

	@Transactional(readOnly = true)
	public AppOrderController.ShareReceiptResponse shareReceipt(String username, Long orderId, String email, String whatsapp) {
		AppOrder order = orderRepository.findByIdAndUserUsername(orderId, username)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order tidak ditemukan."));
		AppOrderPayment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
		OrderResponse receipt = OrderResponse.from(order, payment);

		String receiptText = buildReceiptText(receipt);
		String subject = "Struk Pembayaran #" + receipt.id();

		boolean emailSent = false;
		boolean whatsappSent = false;

		if (email != null && !email.isBlank()) {
			sendReceiptEmail(email.trim(), subject, receipt);
			emailSent = true;
		}
		if (whatsapp != null && !whatsapp.isBlank()) {
			sendReceiptWhatsApp(whatsapp.trim(), receiptText);
			whatsappSent = true;
		}

		return new AppOrderController.ShareReceiptResponse(emailSent, whatsappSent);
	}

	private String buildReceiptText(OrderResponse receipt) {
		NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);

		StringBuilder sb = new StringBuilder();
		sb.append("Struk Pembayaran - Aplikasi Mie Ayam\n");
		sb.append("Order #").append(receipt.id()).append("\n");
		sb.append(receipt.createdAt()).append("\n\n");

		sb.append("Rincian:\n");
		for (var it : receipt.items()) {
			sb.append("- ")
				.append(it.name())
				.append(" (")
				.append(it.quantity())
				.append(" x ")
				.append(nf.format(it.priceEach()))
				.append(") = ")
				.append(nf.format(it.subtotal()))
				.append("\n");
		}
		sb.append("\n");
		sb.append("Total: ").append(nf.format(receipt.total())).append("\n");
		sb.append("Status: ").append(receipt.status() == OrderStatus.PAID ? "Lunas" : "Belum dibayar").append("\n");
		if (receipt.paymentMethod() != null) {
			sb.append("Metode: ").append(receipt.paymentMethod()).append("\n");
		}
		if (receipt.paymentMethod() == PaymentMethod.BANK && receipt.bank() != null && !receipt.bank().isBlank()) {
			sb.append("Rekening: ").append(receipt.bank()).append("\n");
		}
		if (receipt.status() == OrderStatus.PAID) {
			sb.append("Dibayar: ").append(nf.format(receipt.amountPaid())).append("\n");
			sb.append("Kembalian: ").append(nf.format(receipt.changeAmount())).append("\n");
		}
		sb.append("\nTerima kasih.\n");
		return sb.toString();
	}

	private void sendReceiptEmail(String toEmail, String subject, OrderResponse receipt) {
		String host = environment.getProperty("spring.mail.host");
		String username = environment.getProperty("spring.mail.username");
		String password = environment.getProperty("spring.mail.password");
		if (host == null || host.isBlank() || username == null || username.isBlank() || password == null || password.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email belum dikonfigurasi (SPRING_MAIL_HOST/USERNAME/PASSWORD).");
		}

		JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
		if (mailSender == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email belum dikonfigurasi.");
		}

		String from = envFirstNonBlank("APP_MAIL_FROM", "SPRING_MAIL_USERNAME");
		if (from == null || from.isBlank()) {
			from = environment.getProperty("spring.mail.username");
		}
		if (from == null || from.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email sender belum dikonfigurasi (APP_MAIL_FROM / spring.mail.username).");
		}

		try {
			var message = mailSender.createMimeMessage();
			var helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText("Struk pembayaran terlampir dalam format PDF.", false);
			helper.addAttachment("struk-order-" + receipt.id() + ".pdf", new ByteArrayResource(buildReceiptPdf(receipt)), "application/pdf");
			mailSender.send(message);
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gagal mengirim email struk PDF.");
		}
	}

	private byte[] buildReceiptPdf(OrderResponse receipt) {
		try {
			NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
			nf.setMaximumFractionDigits(0);
			nf.setMinimumFractionDigits(0);

			int itemCount = receipt.items() == null ? 0 : receipt.items().size();
			float height = 430f + (itemCount * 22f);
			if (receipt.paymentMethod() == PaymentMethod.BANK && receipt.bank() != null && !receipt.bank().isBlank()) height += 16f;
			if (receipt.status() == OrderStatus.PAID) height += 34f;
			if (height < 560f) height = 560f;
			if (height > 920f) height = 920f;

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Document document = new Document(new Rectangle(320f, height), 20f, 20f, 22f, 22f);
			PdfWriter.getInstance(document, out);
			document.open();

			Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f);
			Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10f);
			Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10f);
			Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f);
			Font itemNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.2f);
			Font itemMetaFont = FontFactory.getFont(FontFactory.HELVETICA, 9.5f);

			Paragraph title = new Paragraph("Struk Pembayaran", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);
			title.setSpacingAfter(10f);
			document.add(title);

			ZoneId zone = ZoneId.of("Asia/Jakarta");
			DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
			String createdAt = receipt.createdAt() == null ? "-" : fmt.format(receipt.createdAt().atZone(zone));

			document.add(new Paragraph("Order #" + receipt.id(), metaFont));
			Paragraph meta2 = new Paragraph(createdAt, metaFont);
			meta2.setSpacingAfter(10f);
			document.add(meta2);

			LineSeparator sep = new LineSeparator(0.6f, 100f, null, Element.ALIGN_CENTER, 0);
			document.add(sep);
			Paragraph spacer = new Paragraph(" ");
			spacer.setSpacingAfter(8f);
			document.add(spacer);

			PdfPTable items = new PdfPTable(new float[] { 1.6f, 0.9f, 0.9f });
			items.setWidthPercentage(100f);
			for (var it : receipt.items()) {
				PdfPCell cName = new PdfPCell();
				cName.setBorder(Rectangle.NO_BORDER);
				cName.setPadding(0f);
				Paragraph pName = new Paragraph(it.name() == null ? "-" : it.name(), itemNameFont);
				pName.setSpacingAfter(2f);
				cName.addElement(pName);
				cName.addElement(new Paragraph(it.quantity() + " x " + nf.format(it.priceEach()), itemMetaFont));
				items.addCell(cName);

				PdfPCell cQty = new PdfPCell(new Paragraph(String.valueOf(it.quantity()), labelFont));
				cQty.setBorder(Rectangle.NO_BORDER);
				cQty.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cQty.setVerticalAlignment(Element.ALIGN_TOP);
				cQty.setPadding(0f);
				items.addCell(cQty);

				PdfPCell cSub = new PdfPCell(new Paragraph(nf.format(it.subtotal()), valueFont));
				cSub.setBorder(Rectangle.NO_BORDER);
				cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cSub.setVerticalAlignment(Element.ALIGN_TOP);
				cSub.setPadding(0f);
				items.addCell(cSub);

				PdfPCell gap = new PdfPCell(new Paragraph(" ", itemMetaFont));
				gap.setColspan(3);
				gap.setBorder(Rectangle.NO_BORDER);
				gap.setFixedHeight(8f);
				items.addCell(gap);
			}
			document.add(items);

			document.add(sep);
			Paragraph spacer2 = new Paragraph(" ");
			spacer2.setSpacingAfter(8f);
			document.add(spacer2);

			PdfPTable totals = new PdfPTable(new float[] { 1.2f, 1f });
			totals.setWidthPercentage(100f);
			addTotalRow(totals, "Total", nf.format(receipt.total()), labelFont, valueFont);
			addTotalRow(totals, "Status", receipt.status() == OrderStatus.PAID ? "Lunas" : "Belum dibayar", labelFont, valueFont);
			addTotalRow(totals, "Metode", receipt.paymentMethod() == null ? "-" : String.valueOf(receipt.paymentMethod()), labelFont, valueFont);
			if (receipt.paymentMethod() == PaymentMethod.BANK && receipt.bank() != null && !receipt.bank().isBlank()) {
				addTotalRow(totals, "Rekening", receipt.bank(), labelFont, valueFont);
			}
			if (receipt.status() == OrderStatus.PAID) {
				addTotalRow(totals, "Dibayar", nf.format(receipt.amountPaid()), labelFont, valueFont);
				addTotalRow(totals, "Kembalian", nf.format(receipt.changeAmount()), labelFont, valueFont);
			}
			document.add(totals);

			document.close();
			return out.toByteArray();
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Gagal membuat PDF struk.");
		}
	}

	private static void addTotalRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
		PdfPCell c1 = new PdfPCell(new Paragraph(label, labelFont));
		c1.setBorder(Rectangle.NO_BORDER);
		c1.setPadding(0f);
		table.addCell(c1);

		PdfPCell c2 = new PdfPCell(new Paragraph(value, valueFont));
		c2.setBorder(Rectangle.NO_BORDER);
		c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
		c2.setPadding(0f);
		table.addCell(c2);

		PdfPCell gap = new PdfPCell(new Paragraph(" "));
		gap.setColspan(2);
		gap.setBorder(Rectangle.NO_BORDER);
		gap.setFixedHeight(6f);
		table.addCell(gap);
	}

	private void sendReceiptWhatsApp(String rawNumber, String text) {
		String token = envFirstNonBlank("APP_WHATSAPP_TOKEN");
		String phoneNumberId = envFirstNonBlank("APP_WHATSAPP_PHONE_NUMBER_ID");
		if (token == null || token.isBlank() || phoneNumberId == null || phoneNumberId.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WhatsApp belum dikonfigurasi (APP_WHATSAPP_TOKEN, APP_WHATSAPP_PHONE_NUMBER_ID).");
		}

		String to = normalizePhoneNumber(rawNumber);
		if (to == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nomor WhatsApp tidak valid.");
		}

		String url = "https://graph.facebook.com/v20.0/" + phoneNumberId + "/messages";
		Map<String, Object> payload = Map.of(
			"messaging_product", "whatsapp",
			"to", to,
			"type", "text",
			"text", Map.of("body", text)
		);
		try {
			restClient.post()
				.uri(url)
				.header("Authorization", "Bearer " + token)
				.body(payload)
				.retrieve()
				.toBodilessEntity();
		} catch (RuntimeException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gagal mengirim WhatsApp. Pastikan akun WhatsApp Business API sudah aktif.");
		}
	}

	private static String normalizePhoneNumber(String raw) {
		if (raw == null) return null;
		String digits = raw.replaceAll("\\D", "");
		if (digits.isBlank()) return null;
		if (digits.startsWith("0")) digits = "62" + digits.substring(1);
		if (!digits.startsWith("62") && digits.length() >= 9) digits = "62" + digits;
		if (digits.length() < 10) return null;
		return digits;
	}

	private static String envFirstNonBlank(String... keys) {
		for (String k : keys) {
			String v = System.getenv(k);
			if (v != null && !v.isBlank()) return v;
		}
		return null;
	}
}

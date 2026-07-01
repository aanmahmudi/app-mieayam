package com.app.mie.ayam.ordering.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.app.mie.ayam.ordering.dto.OrderResponse;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class OrderPaidEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(OrderPaidEventPublisher.class);

	private final KafkaTemplate<String, OrderPaidEvent> kafkaTemplate;
	private final String topic;

	public OrderPaidEventPublisher(
		KafkaTemplate<String, OrderPaidEvent> kafkaTemplate,
		@Value("${app.kafka.topics.order-paid}") String topic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	public void publish(String username, OrderResponse receipt) {
		int amountPaid = receipt.amountPaid() <= 0 ? receipt.total() : receipt.amountPaid();
		int changeAmount = Math.max(0, receipt.changeAmount());
		OrderPaidEvent event = new OrderPaidEvent(
			receipt.id(),
			username,
			receipt.paymentMethod() == null ? null : receipt.paymentMethod().name(),
			receipt.bank(),
			receipt.total(),
			amountPaid,
			changeAmount,
			receipt.createdAt(),
			receipt.paidAt(),
			receipt.items() == null ? java.util.List.of() : receipt.items().stream()
				.map(item -> new OrderPaidItemEvent(
					item.name(),
					item.category() == null ? null : item.category().name(),
					item.quantity(),
					item.priceEach(),
					item.subtotal()
				))
				.toList()
		);

		kafkaTemplate.send(topic, String.valueOf(receipt.id()), event)
			.whenComplete((result, ex) -> {
				if (ex != null) {
					log.warn("Gagal publish event order paid untuk order {}", receipt.id(), ex);
					return;
				}
				log.info("Event order paid terkirim ke Kafka untuk order {}", receipt.id());
			});
	}
}

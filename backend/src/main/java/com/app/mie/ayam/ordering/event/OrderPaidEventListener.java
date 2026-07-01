package com.app.mie.ayam.ordering.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class OrderPaidEventListener {

	private static final Logger log = LoggerFactory.getLogger(OrderPaidEventListener.class);

	@KafkaListener(topics = "${app.kafka.topics.order-paid}", groupId = "${spring.kafka.consumer.group-id}")
	public void handle(OrderPaidEvent event) {
		log.info(
			"Kafka proses async order paid: orderId={}, username={}, method={}, total={}",
			event.orderId(),
			event.username(),
			event.paymentMethod(),
			event.total()
		);
	}
}

package com.app.mie.ayam.ordering.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class ShareReceiptRequestedPublisher {

	private static final Logger log = LoggerFactory.getLogger(ShareReceiptRequestedPublisher.class);

	private final KafkaTemplate<String, ShareReceiptRequestedEvent> kafkaTemplate;
	private final String topic;

	public ShareReceiptRequestedPublisher(
		KafkaTemplate<String, ShareReceiptRequestedEvent> kafkaTemplate,
		@Value("${app.kafka.topics.share-receipt}") String topic
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	public void publish(String username, Long orderId, String email, String whatsapp) {
		ShareReceiptRequestedEvent event = new ShareReceiptRequestedEvent(orderId, username, email, whatsapp);
		kafkaTemplate.send(topic, String.valueOf(orderId), event)
			.whenComplete((result, ex) -> {
				if (ex != null) {
					log.warn("Gagal publish event share receipt untuk order {}", orderId, ex);
					return;
				}
				log.info("Event share receipt terkirim ke Kafka untuk order {}", orderId);
			});
	}
}

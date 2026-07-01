package com.app.mie.ayam.ordering.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.app.mie.ayam.ordering.AppOrderService;

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class ShareReceiptRequestedListener {

	private static final Logger log = LoggerFactory.getLogger(ShareReceiptRequestedListener.class);

	private final AppOrderService orderService;

	public ShareReceiptRequestedListener(AppOrderService orderService) {
		this.orderService = orderService;
	}

	@KafkaListener(topics = "${app.kafka.topics.share-receipt}", groupId = "${spring.kafka.consumer.group-id}")
	public void handle(ShareReceiptRequestedEvent event) {
		try {
			orderService.shareReceiptSync(event.username(), event.orderId(), event.email(), event.whatsapp());
			log.info("Share receipt diproses: orderId={}, username={}", event.orderId(), event.username());
		} catch (Exception ex) {
			log.warn("Share receipt gagal diproses: orderId={}, username={}", event.orderId(), event.username(), ex);
		}
	}
}

package com.app.mie.ayam.ordering;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppOrderPaymentRepository extends JpaRepository<AppOrderPayment, Long> {
	Optional<AppOrderPayment> findByOrderId(Long orderId);
}


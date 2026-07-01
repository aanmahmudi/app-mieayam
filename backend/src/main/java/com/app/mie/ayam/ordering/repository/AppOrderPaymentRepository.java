package com.app.mie.ayam.ordering.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.mie.ayam.ordering.entity.AppOrderPayment;

public interface AppOrderPaymentRepository extends JpaRepository<AppOrderPayment, Long> {
	Optional<AppOrderPayment> findByOrderId(Long orderId);
}

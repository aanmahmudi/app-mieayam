package com.app.mie.ayam.ordering.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_order_payment")
public class AppOrderPayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private AppOrder order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PaymentMethod method;

	@Column(nullable = false)
	private Instant paidAt;

	@Column(nullable = false)
	private int amountPaid;

	@Column(nullable = false)
	private int changeAmount;

	private Boolean confirmed;

	@Column(length = 30)
	private String bank;

	protected AppOrderPayment() {
	}

	public AppOrderPayment(AppOrder order, PaymentMethod method, Instant paidAt, int amountPaid, int changeAmount) {
		this.order = order;
		this.method = method;
		this.paidAt = paidAt;
		this.amountPaid = amountPaid;
		this.changeAmount = changeAmount;
		this.confirmed = true;
	}

	public Long getId() {
		return id;
	}

	public AppOrder getOrder() {
		return order;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public Instant getPaidAt() {
		return paidAt;
	}

	public int getAmountPaid() {
		return amountPaid;
	}

	public int getChangeAmount() {
		return changeAmount;
	}

	public boolean isConfirmed() {
		return confirmed == null || confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public void setPaidAt(Instant paidAt) {
		this.paidAt = paidAt;
	}

	public void setAmountPaid(int amountPaid) {
		this.amountPaid = amountPaid;
	}

	public void setChangeAmount(int changeAmount) {
		this.changeAmount = changeAmount;
	}
}

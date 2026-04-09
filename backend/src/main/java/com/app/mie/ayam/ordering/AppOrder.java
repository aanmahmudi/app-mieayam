package com.app.mie.ayam.ordering;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.app.mie.ayam.user.AppUser;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_order")
public class AppOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private int total;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AppOrderItem> items = new ArrayList<>();

	protected AppOrder() {
	}

	public AppOrder(AppUser user, Instant createdAt, int total) {
		this.user = user;
		this.createdAt = createdAt;
		this.total = total;
	}

	public Long getId() {
		return id;
	}

	public AppUser getUser() {
		return user;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public int getTotal() {
		return total;
	}

	public List<AppOrderItem> getItems() {
		return items;
	}

	public void addItem(AppOrderItem item) {
		items.add(item);
		item.setOrder(this);
	}
}

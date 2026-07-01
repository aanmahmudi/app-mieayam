package com.app.mie.ayam.ordering.entity;

import com.app.mie.ayam.menu.entity.MenuCategory;
import com.app.mie.ayam.menu.entity.MenuItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_order_item")
public class AppOrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private AppOrder order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_item_id")
	private MenuItem menuItem;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MenuCategory category;

	@Column(length = 50)
	private String unit;

	@Column(nullable = false)
	private int priceEach;

	@Column(nullable = false)
	private int quantity;

	@Column(length = 255)
	private String imageUrl;

	protected AppOrderItem() {
	}

	public AppOrderItem(
		MenuItem menuItem,
		String name,
		MenuCategory category,
		String unit,
		int priceEach,
		int quantity,
		String imageUrl
	) {
		this.menuItem = menuItem;
		this.name = name;
		this.category = category;
		this.unit = unit;
		this.priceEach = priceEach;
		this.quantity = quantity;
		this.imageUrl = imageUrl;
	}

	public Long getId() {
		return id;
	}

	public AppOrder getOrder() {
		return order;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public String getName() {
		return name;
	}

	public MenuCategory getCategory() {
		return category;
	}

	public String getUnit() {
		return unit;
	}

	public int getPriceEach() {
		return priceEach;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	void setOrder(AppOrder order) {
		this.order = order;
	}
}

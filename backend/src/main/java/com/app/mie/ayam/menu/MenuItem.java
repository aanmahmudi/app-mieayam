package com.app.mie.ayam.menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "menu_item")
public class MenuItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MenuCategory category;

	@Column(length = 50)
	private String unit;

	@Column(nullable = false)
	private int price;

	@Column(length = 255)
	private String imageUrl;

	protected MenuItem() {
	}

	public MenuItem(String name, MenuCategory category, String unit, int price) {
		this(name, category, unit, price, null);
	}

	public MenuItem(String name, MenuCategory category, String unit, int price, String imageUrl) {
		this.name = name;
		this.category = category;
		this.unit = unit;
		this.price = price;
		this.imageUrl = imageUrl;
	}

	public Long getId() {
		return id;
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

	public int getPrice() {
		return price;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public void setCategory(MenuCategory category) {
		this.category = category;
	}
}

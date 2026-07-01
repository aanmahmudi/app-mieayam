package com.app.mie.ayam.menu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.mie.ayam.menu.entity.MenuCategory;
import com.app.mie.ayam.menu.entity.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
	List<MenuItem> findAllByCategoryOrderByNameAsc(MenuCategory category);
	Optional<MenuItem> findByName(String name);
}

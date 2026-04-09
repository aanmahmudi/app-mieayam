package com.app.mie.ayam.menu;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
	List<MenuItem> findAllByCategoryOrderByNameAsc(MenuCategory category);
	Optional<MenuItem> findByName(String name);
}

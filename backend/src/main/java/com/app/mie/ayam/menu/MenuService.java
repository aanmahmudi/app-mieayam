package com.app.mie.ayam.menu;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.mie.ayam.menu.dto.MenuItemResponse;

@Service
public class MenuService {

	private final MenuItemRepository menuItemRepository;

	public MenuService(MenuItemRepository menuItemRepository) {
		this.menuItemRepository = menuItemRepository;
	}

	@Transactional(readOnly = true)
	public List<MenuItemResponse> list(MenuCategory category) {
		if (category == null) {
			return menuItemRepository.findAll().stream().map(MenuItemResponse::from).toList();
		}
		return menuItemRepository.findAllByCategoryOrderByNameAsc(category).stream().map(MenuItemResponse::from).toList();
	}
}


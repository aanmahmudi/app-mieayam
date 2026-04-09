package com.app.mie.ayam.menu;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.mie.ayam.menu.dto.MenuItemResponse;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

	private final MenuItemRepository menuItemRepository;

	public MenuController(MenuItemRepository menuItemRepository) {
		this.menuItemRepository = menuItemRepository;
	}

	@GetMapping
	public List<MenuItemResponse> list(@RequestParam(required = false) MenuCategory category) {
		if (category == null) {
			return menuItemRepository.findAll().stream().map(MenuItemResponse::from).toList();
		}
		return menuItemRepository.findAllByCategoryOrderByNameAsc(category).stream().map(MenuItemResponse::from).toList();
	}
}

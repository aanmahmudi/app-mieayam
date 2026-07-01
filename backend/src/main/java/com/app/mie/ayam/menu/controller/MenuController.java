package com.app.mie.ayam.menu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.mie.ayam.menu.dto.MenuItemResponse;
import com.app.mie.ayam.menu.entity.MenuCategory;
import com.app.mie.ayam.menu.service.MenuService;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

	private final MenuService menuService;

	public MenuController(MenuService menuService) {
		this.menuService = menuService;
	}

	@GetMapping
	public List<MenuItemResponse> list(@RequestParam(required = false) MenuCategory category) {
		return menuService.list(category);
	}
}

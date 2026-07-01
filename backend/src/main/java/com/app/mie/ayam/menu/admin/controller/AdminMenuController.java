package com.app.mie.ayam.menu.admin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.mie.ayam.menu.admin.dto.AdminUpsertMenuItemRequest;
import com.app.mie.ayam.menu.admin.service.AdminMenuService;
import com.app.mie.ayam.menu.dto.MenuItemResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/menu")
public class AdminMenuController {

	private final AdminMenuService adminMenuService;

	public AdminMenuController(AdminMenuService adminMenuService) {
		this.adminMenuService = adminMenuService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MenuItemResponse create(@Valid @RequestBody AdminUpsertMenuItemRequest request) {
		return adminMenuService.create(request);
	}

	@PutMapping("/{id}")
	public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody AdminUpsertMenuItemRequest request) {
		return adminMenuService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		adminMenuService.delete(id);
	}
}

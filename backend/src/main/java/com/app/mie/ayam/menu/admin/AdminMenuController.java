package com.app.mie.ayam.menu.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.menu.MenuItem;
import com.app.mie.ayam.menu.MenuItemRepository;
import com.app.mie.ayam.menu.admin.dto.AdminUpsertMenuItemRequest;
import com.app.mie.ayam.menu.dto.MenuItemResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/menu")
public class AdminMenuController {

	private final MenuItemRepository menuItemRepository;

	public AdminMenuController(MenuItemRepository menuItemRepository) {
		this.menuItemRepository = menuItemRepository;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MenuItemResponse create(@Valid @RequestBody AdminUpsertMenuItemRequest request) {
		if (menuItemRepository.findByName(request.name()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Nama menu sudah ada.");
		}
		MenuItem item = new MenuItem(request.name(), request.category(), request.unit(), request.price(), request.imageUrl());
		MenuItem saved = menuItemRepository.save(item);
		return MenuItemResponse.from(saved);
	}

	@PutMapping("/{id}")
	public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody AdminUpsertMenuItemRequest request) {
		MenuItem item = menuItemRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu tidak ditemukan."));
		MenuItem byName = menuItemRepository.findByName(request.name()).orElse(null);
		if (byName != null && !byName.getId().equals(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Nama menu sudah ada.");
		}
		item.setName(request.name());
		item.setCategory(request.category());
		item.setUnit(request.unit());
		item.setPrice(request.price());
		item.setImageUrl(request.imageUrl());
		MenuItem saved = menuItemRepository.save(item);
		return MenuItemResponse.from(saved);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		if (!menuItemRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu tidak ditemukan.");
		}
		menuItemRepository.deleteById(id);
	}
}

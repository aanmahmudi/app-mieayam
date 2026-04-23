package com.app.mie.ayam.menu.admin;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.mie.ayam.menu.MenuItem;
import com.app.mie.ayam.menu.MenuItemRepository;
import com.app.mie.ayam.menu.admin.dto.AdminUpsertMenuItemRequest;
import com.app.mie.ayam.menu.dto.MenuItemResponse;

@Service
public class AdminMenuService {

	private final MenuItemRepository menuItemRepository;

	public AdminMenuService(MenuItemRepository menuItemRepository) {
		this.menuItemRepository = menuItemRepository;
	}

	@Transactional
	public MenuItemResponse create(AdminUpsertMenuItemRequest request) {
		if (menuItemRepository.findByName(request.name()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Nama menu sudah ada.");
		}
		MenuItem item = new MenuItem(request.name(), request.category(), request.unit(), request.price(), request.imageUrl());
		MenuItem saved = menuItemRepository.save(item);
		return MenuItemResponse.from(saved);
	}

	@Transactional
	public MenuItemResponse update(Long id, AdminUpsertMenuItemRequest request) {
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

	@Transactional
	public void delete(Long id) {
		if (!menuItemRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu tidak ditemukan.");
		}
		menuItemRepository.deleteById(id);
	}
}


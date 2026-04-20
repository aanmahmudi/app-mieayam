package com.app.mie.ayam.menu;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MenuSeeder implements ApplicationRunner {

	private final MenuItemRepository menuItemRepository;

	public MenuSeeder(MenuItemRepository menuItemRepository) {
		this.menuItemRepository = menuItemRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		ensureMenuItem("Mie Ayam Pangsit", MenuCategory.MAKANAN, "1 porsi", 13000, "/uploads/makanan/mie ayam pangsit.jpeg");
		ensureMenuItem("Mie Ayam Bakso", MenuCategory.MAKANAN, "1 porsi", 16000, "/uploads/makanan/mie ayam bakso.jpeg");
		ensureMenuItem("Pangsit Rebus", MenuCategory.MAKANAN, "1 porsi", 13000, "/uploads/makanan/pangsit rebus.jpg");

		ensureMenuItem("Es Teh Manis", MenuCategory.MINUMAN, "1 gelas", 3000, "/uploads/minuman/es teh manis.jpg");
		ensureMenuItem("Es Teh Botol", MenuCategory.MINUMAN, "1 botol", 5000, "/uploads/minuman/teh botol.jpg");
		ensureMenuItemWithAliases(new String[] { "Es Tee", "Es S Tee" }, MenuCategory.MINUMAN, "1 gelas", 5000, "/uploads/minuman/s tee.jpg");
		ensureMenuItem("Aqua", MenuCategory.MINUMAN, "1 botol", 4000, "/uploads/minuman/aqua.jpg");
		ensureMenuItem("Teds", MenuCategory.MINUMAN, "1 botol", 2000, "/uploads/minuman/teds.jpg");
		ensureMenuItem("Teh Manis", MenuCategory.MINUMAN, "1 gelas", 2000, "/uploads/minuman/teh manis.jpg");
		ensureMenuItem("Teh Tawar", MenuCategory.MINUMAN, "1 gelas", 2000, "/uploads/minuman/teh tawar.jpg");

		ensureMenuItem("Extra Pangsit", MenuCategory.EXTRA, "1 pcs", 1000, "/uploads/extra/pangsit.jpg");
		ensureMenuItem("Bakso (3 butir)", MenuCategory.EXTRA, "3 butir", 5000, "/uploads/extra/bakso.jpg");
	}

	private void ensureMenuItem(String name, MenuCategory category, String unit, int price, String imageUrl) {
		ensureMenuItemWithAliases(new String[] { name }, category, unit, price, imageUrl);
	}

	private void ensureMenuItemWithAliases(String[] names, MenuCategory category, String unit, int price, String imageUrl) {
		MenuItem item = null;
		for (String name : names) {
			item = menuItemRepository.findByName(name).orElse(null);
			if (item != null) {
				break;
			}
		}

		if (item == null) {
			menuItemRepository.save(new MenuItem(names[0], category, unit, price, imageUrl));
			return;
		}
	}
}

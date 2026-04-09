package com.app.mie.ayam.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final AppUploadsProperties uploadsProperties;

	public WebConfig(AppUploadsProperties uploadsProperties) {
		this.uploadsProperties = uploadsProperties;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path uploadsDir = Path.of(uploadsProperties.dir()).toAbsolutePath().normalize();
		String location = uploadsDir.toUri().toString();
		if (!location.endsWith("/")) {
			location = location + "/";
		}
		registry.addResourceHandler("/uploads/**").addResourceLocations(location);
	}
}

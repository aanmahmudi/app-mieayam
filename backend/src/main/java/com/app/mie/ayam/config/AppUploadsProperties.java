package com.app.mie.ayam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.uploads")
public record AppUploadsProperties(String dir) {
}

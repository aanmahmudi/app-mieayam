package com.app.mie.ayam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.app.mie.ayam.config.AppCorsProperties;
import com.app.mie.ayam.config.AppJwtProperties;
import com.app.mie.ayam.config.AppUploadsProperties;

@SpringBootApplication
@EnableConfigurationProperties({AppCorsProperties.class, AppJwtProperties.class, AppUploadsProperties.class})
public class AppMieAyamApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppMieAyamApplication.class, args);
	}

}

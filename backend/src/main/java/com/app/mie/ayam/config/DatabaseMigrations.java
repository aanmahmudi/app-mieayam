package com.app.mie.ayam.config;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrations implements ApplicationRunner {

	private final DataSource dataSource;

	public DatabaseMigrations(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String db = getDatabaseProductName();
		if (db == null) return;
		if (db.toLowerCase().contains("postgresql")) {
			migratePostgres();
		}
	}

	private void migratePostgres() {
		JdbcTemplate jdbc = new JdbcTemplate(dataSource);
		try {
			jdbc.execute("ALTER TABLE app_order_payment DROP CONSTRAINT IF EXISTS app_order_payment_method_check1");
		} catch (RuntimeException ignored) {
		}
		try {
			jdbc.execute(
				"ALTER TABLE app_order_payment ADD CONSTRAINT app_order_payment_method_check1 CHECK (method IN ('CASH','QRIS','BANK'))"
			);
		} catch (RuntimeException ignored) {
		}
		try {
			jdbc.execute("ALTER TABLE wallet_transaction DROP CONSTRAINT IF EXISTS wallet_transaction_type_check");
		} catch (RuntimeException ignored) {
		}
		try {
			jdbc.execute(
				"ALTER TABLE wallet_transaction ADD CONSTRAINT wallet_transaction_type_check CHECK (type IN ('TOP_UP','PAYMENT','REFUND'))"
			);
		} catch (RuntimeException ignored) {
		}
	}

	private String getDatabaseProductName() {
		try (var connection = dataSource.getConnection()) {
			DatabaseMetaData meta = connection.getMetaData();
			return meta.getDatabaseProductName();
		} catch (SQLException ex) {
			return null;
		}
	}
}

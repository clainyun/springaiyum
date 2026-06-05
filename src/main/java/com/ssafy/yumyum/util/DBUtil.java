package com.ssafy.yumyum.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DBUtil {
	private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ssafy_yumyumcoach?serverTimezone=Asia/Seoul&characterEncoding=UTF-8";
	private static final String DEFAULT_USER = "ssafy";
	private static final String DEFAULT_PASSWORD = "ssafy";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL Driver 로딩 실패: " + e.getMessage());
        }
	}

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(
			resolve("spring.datasource.url", "SPRING_DATASOURCE_URL", DEFAULT_URL),
			resolve("spring.datasource.username", "SPRING_DATASOURCE_USERNAME", DEFAULT_USER),
			resolve("spring.datasource.password", "SPRING_DATASOURCE_PASSWORD", DEFAULT_PASSWORD)
		);
	}

	private static String resolve(String propertyName, String envName, String defaultValue) {
		String propertyValue = System.getProperty(propertyName);
		if (propertyValue != null && !propertyValue.isBlank()) {
			return propertyValue;
		}

		String envValue = System.getenv(envName);
		if (envValue != null && !envValue.isBlank()) {
			return envValue;
		}

		return defaultValue;
	}
}

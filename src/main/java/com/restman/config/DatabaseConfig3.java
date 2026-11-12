package com.restman.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig3 {
    private static final Properties PROPERTIES = loadProperties();

    private DatabaseConfig3() {
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig3.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Không tìm thấy application.properties");
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getConfiguredUrl(), getConfiguredUsername(), getConfiguredPassword());
    }

    public static Connection getServerConnection() throws SQLException {
        String url = getConfiguredUrl();
        int queryIndex = url.indexOf('?');
        String options = queryIndex < 0 ? "" : url.substring(queryIndex);
        String endpoint = queryIndex < 0 ? url : url.substring(0, queryIndex);
        int databaseSlash = endpoint.lastIndexOf('/');
        if (databaseSlash <= "jdbc:mysql://".length()) {
            throw new SQLException("db.url phải là URL JDBC MySQL có tên cơ sở dữ liệu.");
        }
        return DriverManager.getConnection(endpoint.substring(0, databaseSlash + 1) + options,
                getConfiguredUsername(), getConfiguredPassword());
    }

    private static String getConfiguredUrl() {
        return System.getenv().getOrDefault("RESTMAN_DB_URL", PROPERTIES.getProperty("db.url"));
    }

    private static String getConfiguredUsername() {
        return System.getenv().getOrDefault("RESTMAN_DB_USER", PROPERTIES.getProperty("db.username"));
    }

    private static String getConfiguredPassword() {
        return System.getenv().getOrDefault("RESTMAN_DB_PASSWORD", PROPERTIES.getProperty("db.password"));
    }
}

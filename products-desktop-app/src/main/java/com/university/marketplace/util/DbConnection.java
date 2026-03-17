package com.university.marketplace.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConnection {
    private static final String DB_DIR = System.getProperty("user.home") + "/.marketplace-products";
    private static final String DB_PATH = DB_DIR + "/products.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;

    private DbConnection() {}

    public static Connection getConnection() throws SQLException {
        ensureDirectory();
        return DriverManager.getConnection(JDBC_URL);
    }

    private static void ensureDirectory() {
        try {
            Path path = Paths.get(DB_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create DB directory", ex);
        }
    }
}

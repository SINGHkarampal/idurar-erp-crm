package com.university.marketplace.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public final class DatabaseInitializer {

    private DatabaseInitializer() {}

    public static void initializeDatabase() {
        try (Connection conn = DbConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sku TEXT NOT NULL UNIQUE,
                    name TEXT NOT NULL,
                    description TEXT,
                    price REAL NOT NULL,
                    stock INTEGER NOT NULL,
                    image_path TEXT,
                    archived INTEGER NOT NULL DEFAULT 0,
                    availability_status TEXT NOT NULL
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS product_categories (
                    product_id INTEGER NOT NULL,
                    category_id INTEGER NOT NULL,
                    PRIMARY KEY (product_id, category_id),
                    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                    FOREIGN KEY (category_id) REFERENCES categories(id)
                )
                """);

            seedCategories(conn);
            seedProducts(conn);
        } catch (Exception ex) {
            throw new RuntimeException("Database initialization failed", ex);
        }
    }

    private static void seedCategories(Connection conn) throws Exception {
        String[] base = {"Electronics", "Books", "Fashion", "Home", "Sports", "Beauty"};
        try (PreparedStatement ps = conn.prepareStatement("INSERT OR IGNORE INTO categories(name) VALUES(?)")) {
            for (String c : base) {
                ps.setString(1, c);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedProducts(Connection conn) throws Exception {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO products(sku, name, description, price, stock, image_path, archived, availability_status) VALUES(?,?,?,?,?,?,?,?)")) {
            ps.setString(1, "SKU-1001");
            ps.setString(2, "Wireless Mouse");
            ps.setString(3, "Ergonomic silent wireless mouse");
            ps.setDouble(4, 24.99);
            ps.setInt(5, 35);
            ps.setString(6, "");
            ps.setInt(7, 0);
            ps.setString(8, "Available");
            ps.executeUpdate();

            ps.setString(1, "SKU-1002");
            ps.setString(2, "Notebook A5");
            ps.setString(3, "Hardcover ruled notebook");
            ps.setDouble(4, 4.50);
            ps.setInt(5, 0);
            ps.setString(6, "");
            ps.setInt(7, 0);
            ps.setString(8, "Out of Stock");
            ps.executeUpdate();
        }

        try (Statement st = conn.createStatement()) {
            st.executeUpdate("INSERT OR IGNORE INTO product_categories(product_id, category_id) VALUES (1, 1)");
            st.executeUpdate("INSERT OR IGNORE INTO product_categories(product_id, category_id) VALUES (2, 2)");
        }
    }
}

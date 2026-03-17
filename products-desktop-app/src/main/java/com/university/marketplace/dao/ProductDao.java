package com.university.marketplace.dao;

import com.university.marketplace.model.Category;
import com.university.marketplace.model.Product;
import com.university.marketplace.util.DbConnection;

import java.sql.*;
import java.util.*;

public class ProductDao {

    public List<Product> findAll(boolean includeArchived, String search, Integer categoryId) {
        Map<Integer, Product> map = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder("""
            SELECT p.id, p.sku, p.name, p.description, p.price, p.stock, p.image_path, p.archived, p.availability_status,
                   c.id AS c_id, c.name AS c_name
            FROM products p
            LEFT JOIN product_categories pc ON p.id = pc.product_id
            LEFT JOIN categories c ON pc.category_id = c.id
            WHERE (? = 1 OR p.archived = 0)
            """);

        List<Object> params = new ArrayList<>();
        params.add(includeArchived ? 1 : 0);

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(p.name) LIKE ? OR LOWER(p.sku) LIKE ?) ");
            params.add("%" + search.toLowerCase() + "%");
            params.add("%" + search.toLowerCase() + "%");
        }
        if (categoryId != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM product_categories x WHERE x.product_id = p.id AND x.category_id = ?) ");
            params.add(categoryId);
        }
        sql.append(" ORDER BY p.id DESC");

        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Product p = map.computeIfAbsent(id, k -> {
                        Product np = new Product();
                        np.setId(id);
                        np.setSku(rsSafe(rs, "sku"));
                        np.setName(rsSafe(rs, "name"));
                        np.setDescription(rsSafe(rs, "description"));
                        np.setPrice(rs.getBigDecimal("price"));
                        np.setStock(rs.getInt("stock"));
                        np.setImagePath(rsSafe(rs, "image_path"));
                        np.setArchived(rs.getInt("archived") == 1);
                        np.setAvailabilityStatus(rsSafe(rs, "availability_status"));
                        return np;
                    });
                    int cid = rs.getInt("c_id");
                    if (!rs.wasNull()) {
                        p.getCategories().add(new Category(cid, rsSafe(rs, "c_name")));
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load products", ex);
        }
        return new ArrayList<>(map.values());
    }

    public void insert(Product product) {
        String productSql = "INSERT INTO products(sku,name,description,price,stock,image_path,archived,availability_status) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(productSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, product.getSku());
                ps.setString(2, product.getName());
                ps.setString(3, product.getDescription());
                ps.setBigDecimal(4, product.getPrice());
                ps.setInt(5, product.getStock());
                ps.setString(6, product.getImagePath());
                ps.setInt(7, product.isArchived() ? 1 : 0);
                ps.setString(8, product.getAvailabilityStatus());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) product.setId(keys.getInt(1));
                }
            }
            replaceCategories(conn, product);
            conn.commit();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create product", ex);
        }
    }

    public void update(Product product) {
        String sql = "UPDATE products SET sku=?,name=?,description=?,price=?,stock=?,image_path=?,archived=?,availability_status=? WHERE id=?";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            ps.setString(1, product.getSku());
            ps.setString(2, product.getName());
            ps.setString(3, product.getDescription());
            ps.setBigDecimal(4, product.getPrice());
            ps.setInt(5, product.getStock());
            ps.setString(6, product.getImagePath());
            ps.setInt(7, product.isArchived() ? 1 : 0);
            ps.setString(8, product.getAvailabilityStatus());
            ps.setInt(9, product.getId());
            ps.executeUpdate();
            replaceCategories(conn, product);
            conn.commit();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to update product", ex);
        }
    }

    public void delete(int productId) {
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setInt(1, productId);
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete product", ex);
        }
    }

    private void replaceCategories(Connection conn, Product product) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM product_categories WHERE product_id=?")) {
            del.setInt(1, product.getId());
            del.executeUpdate();
        }
        try (PreparedStatement ins = conn.prepareStatement("INSERT INTO product_categories(product_id,category_id) VALUES(?,?)")) {
            for (Category c : product.getCategories()) {
                ins.setInt(1, product.getId());
                ins.setInt(2, c.getId());
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }

    private static String rsSafe(ResultSet rs, String column) {
        try { return rs.getString(column); } catch (Exception e) { return ""; }
    }
}

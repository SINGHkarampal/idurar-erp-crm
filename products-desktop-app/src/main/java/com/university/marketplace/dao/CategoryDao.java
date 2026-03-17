package com.university.marketplace.dao;

import com.university.marketplace.model.Category;
import com.university.marketplace.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    public List<Category> findAll() {
        List<Category> result = new ArrayList<>();
        String sql = "SELECT id, name FROM categories ORDER BY name";
        try (Connection conn = DbConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load categories", ex);
        }
        return result;
    }
}

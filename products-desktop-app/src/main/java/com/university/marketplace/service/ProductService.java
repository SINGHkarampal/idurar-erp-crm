package com.university.marketplace.service;

import com.university.marketplace.dao.ProductDao;
import com.university.marketplace.model.Product;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {
    private final ProductDao productDao = new ProductDao();

    public List<Product> list(boolean includeArchived, String search, Integer categoryId) {
        return productDao.findAll(includeArchived, search, categoryId);
    }

    public void create(Product product) {
        validate(product, false);
        applyAvailability(product);
        productDao.insert(product);
    }

    public void update(Product product) {
        validate(product, true);
        applyAvailability(product);
        productDao.update(product);
    }

    public void delete(int productId) {
        productDao.delete(productId);
    }

    public void archive(Product product) {
        product.setArchived(true);
        applyAvailability(product);
        productDao.update(product);
    }

    public void reactivate(Product product) {
        product.setArchived(false);
        applyAvailability(product);
        productDao.update(product);
    }

    private void validate(Product p, boolean update) {
        if (update && p.getId() <= 0) throw new IllegalArgumentException("Invalid product id for update.");
        if (p.getSku() == null || p.getSku().isBlank()) throw new IllegalArgumentException("SKU is required.");
        if (!p.getSku().matches("[A-Za-z0-9\\-_.]{3,30}")) throw new IllegalArgumentException("SKU must be 3-30 chars, alphanumeric or -_.");
        if (p.getName() == null || p.getName().isBlank()) throw new IllegalArgumentException("Product name is required.");
        if (p.getName().length() > 120) throw new IllegalArgumentException("Product name max length is 120.");
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price must be >= 0.");
        if (p.getPrice().scale() > 2) throw new IllegalArgumentException("Price supports maximum 2 decimals.");
        if (p.getStock() < 0) throw new IllegalArgumentException("Stock must be >= 0.");
        if (p.getCategories() == null || p.getCategories().isEmpty()) throw new IllegalArgumentException("At least one category is required.");
    }

    private void applyAvailability(Product p) {
        if (p.isArchived()) p.setAvailabilityStatus("Inactive");
        else if (p.getStock() <= 0) p.setAvailabilityStatus("Out of Stock");
        else p.setAvailabilityStatus("Available");
    }
}

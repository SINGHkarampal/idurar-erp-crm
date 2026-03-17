package com.university.marketplace.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Product {
    private int id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private String imagePath;
    private boolean archived;
    private String availabilityStatus;
    private List<Category> categories = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }

    public String getCategoryNames() {
        return categories.stream().map(Category::getName).reduce((a, b) -> a + ", " + b).orElse("-");
    }
}

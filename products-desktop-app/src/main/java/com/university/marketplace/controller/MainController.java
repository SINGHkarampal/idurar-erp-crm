package com.university.marketplace.controller;

import com.university.marketplace.dao.CategoryDao;
import com.university.marketplace.model.Category;
import com.university.marketplace.model.Product;
import com.university.marketplace.service.ProductService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML private TextField searchField;
    @FXML private ComboBox<Category> categoryFilterBox;
    @FXML private CheckBox showArchivedCheck;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> idCol;
    @FXML private TableColumn<Product, String> skuCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> categoryCol;
    @FXML private TableColumn<Product, String> priceCol;
    @FXML private TableColumn<Product, String> stockCol;
    @FXML private TableColumn<Product, String> statusCol;
    @FXML private TableColumn<Product, String> activeCol;

    @FXML private TextField skuField;
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private ListView<Category> categoriesList;
    @FXML private TextArea descriptionArea;
    @FXML private javafx.scene.image.ImageView imagePreview;
    @FXML private Label messageLabel;

    private final ProductService productService = new ProductService();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ObservableList<Product> rows = FXCollections.observableArrayList();
    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    private Product editing;
    private String selectedImagePath;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        skuCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSku()));
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        categoryCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoryNames()));
        priceCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrice().toPlainString()));
        stockCol.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStock())));
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAvailabilityStatus()));
        activeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isArchived() ? "Inactive" : "Active"));

        productsTable.setItems(rows);
        categories.setAll(categoryDao.findAll());
        categoriesList.setItems(categories);
        categoriesList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        categoryFilterBox.getItems().add(new Category(0, "All categories"));
        categoryFilterBox.getItems().addAll(categories);
        categoryFilterBox.getSelectionModel().select(0);

        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, selected) -> loadToForm(selected));
        refreshTable();
        onNew();
    }

    @FXML
    private void onSearch() { refreshTable(); }

    @FXML
    private void onResetFilters() {
        searchField.clear();
        showArchivedCheck.setSelected(false);
        categoryFilterBox.getSelectionModel().select(0);
        refreshTable();
    }

    @FXML
    private void onNew() {
        editing = null;
        selectedImagePath = null;
        skuField.clear(); nameField.clear(); priceField.clear(); stockField.clear(); descriptionArea.clear();
        categoriesList.getSelectionModel().clearSelection();
        imagePreview.setImage(null);
        messageLabel.setText("");
        productsTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSave() {
        try {
            Product target = editing == null ? new Product() : editing;
            target.setSku(skuField.getText().trim());
            target.setName(nameField.getText().trim());
            target.setDescription(descriptionArea.getText().trim());
            target.setPrice(new BigDecimal(priceField.getText().trim()));
            target.setStock(Integer.parseInt(stockField.getText().trim()));
            target.setImagePath(selectedImagePath == null ? "" : selectedImagePath);
            target.setCategories(new ArrayList<>(categoriesList.getSelectionModel().getSelectedItems()));
            target.setArchived(editing != null && editing.isArchived());

            if (editing == null) productService.create(target); else productService.update(target);
            messageLabel.setText("Saved successfully.");
            refreshTable();
            selectById(target.getId());
        } catch (NumberFormatException nfe) {
            messageLabel.setText("Price and Stock must be valid numeric values.");
        } catch (Exception ex) {
            messageLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        if (editing == null) { messageLabel.setText("Select a product first."); return; }
        productService.delete(editing.getId());
        messageLabel.setText("Deleted.");
        refreshTable();
        onNew();
    }

    @FXML
    private void onArchive() {
        if (editing == null) { messageLabel.setText("Select a product first."); return; }
        productService.archive(editing);
        messageLabel.setText("Product archived.");
        refreshTable();
        selectById(editing.getId());
    }

    @FXML
    private void onReactivate() {
        if (editing == null) { messageLabel.setText("Select a product first."); return; }
        productService.reactivate(editing);
        messageLabel.setText("Product reactivated.");
        refreshTable();
        selectById(editing.getId());
    }

    @FXML
    private void onUploadImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = chooser.showOpenDialog(productsTable.getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.toURI().toString();
            imagePreview.setImage(new Image(selectedImagePath));
        }
    }

    @FXML
    private void onRemoveImage() {
        selectedImagePath = "";
        imagePreview.setImage(null);
    }

    private void refreshTable() {
        Integer categoryId = null;
        Category selectedCategory = categoryFilterBox.getValue();
        if (selectedCategory != null && selectedCategory.getId() > 0) categoryId = selectedCategory.getId();
        rows.setAll(productService.list(showArchivedCheck.isSelected(), searchField.getText(), categoryId));
    }

    private void loadToForm(Product p) {
        if (p == null) return;
        editing = p;
        selectedImagePath = p.getImagePath();
        skuField.setText(p.getSku());
        nameField.setText(p.getName());
        priceField.setText(p.getPrice().toPlainString());
        stockField.setText(String.valueOf(p.getStock()));
        descriptionArea.setText(p.getDescription());

        categoriesList.getSelectionModel().clearSelection();
        for (Category c : p.getCategories()) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == c.getId()) categoriesList.getSelectionModel().select(i);
            }
        }

        if (selectedImagePath != null && !selectedImagePath.isBlank()) {
            imagePreview.setImage(new Image(selectedImagePath, true));
        } else {
            imagePreview.setImage(null);
        }
        messageLabel.setText("Viewing product #" + p.getId());
    }

    private void selectById(int id) {
        for (Product row : rows) {
            if (row.getId() == id) {
                productsTable.getSelectionModel().select(row);
                productsTable.scrollTo(row);
                break;
            }
        }
    }
}

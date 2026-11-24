package Products;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductDatabase {

    private String databaseName;
    private final Map<String, Product> database = new LinkedHashMap<>();

    public ProductDatabase(String databaseName) {
        if (databaseName == null || databaseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Database name is required.");
        }

        this.databaseName = databaseName.trim();
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        String productCode = product.getProductCode();

        if (database.containsKey(productCode)) {
            throw new IllegalArgumentException("Product code already exists: " + productCode);
        }

        database.put(productCode, product);
    }

    public void removeProduct(Product product) {
        if (product == null) {
            return;
        }

        database.remove(product.getProductCode());
    }

    public void editProduct(Product product, String productName, String productDescription,
            String productSource, double productCostInKG, double productRawPrice,
            double productEstimatedMarketPrice) {

        if (product == null) {
            throw new IllegalArgumentException("Select a product first.");
        }

        if (!database.containsKey(product.getProductCode())) {
            throw new IllegalArgumentException("Product was not found in the database.");
        }

        product.update(productName, productDescription, productSource, productCostInKG, productRawPrice, productEstimatedMarketPrice);
    }

    // Returns the product with this code. Returns null when it is not found.
    public Product searchProduct(String productCode) {
        return database.get(productCode);
    }

    // Returns a new list so the caller cannot directly change the database map.
    public List<Product> getAllProducts() {
        return new ArrayList<>(database.values());
    }

    public int size() {
        return database.size();
    }

    // Used for backend testing. This is not intended for the GUI.
    public void viewAllProducts() {
        for (Map.Entry<String, Product> entry : database.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue().viewState());
        }
    }

    public void removeAllProducts() {
        database.clear();
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        if (databaseName == null || databaseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Database name is required.");
        }

        this.databaseName = databaseName.trim();
    }
}
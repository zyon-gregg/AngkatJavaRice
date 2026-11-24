package Products;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductDatabase {

    private String databaseName;
    private final Map<String, Product> database = new LinkedHashMap<>();

    public ProductDatabase(String databaseName) {
        this.databaseName = databaseName;
    }

    // Adds a product to the catalog. Returns false if the product is invalid
    // or its product code is already being used.
    public boolean addProduct(Product product) {
        if (product == null) {
            return false;
        }

        String productCode = product.getProductCode();

        if (productCode == null || productCode.trim().isEmpty()) {
            return false;
        }

        if (database.containsKey(productCode)) {
            return false;
        }

        database.put(productCode, product);
        return true;
    }

    // Removes a product from the catalog. Inventory removal is handled by
    // the Product Management screen so both modules stay synchronized.
    public boolean removeProduct(Product product) {
        if (product == null) {
            return false;
        }

        String productCode = product.getProductCode();
        Product savedProduct = database.get(productCode);

        if (savedProduct == null) {
            return false;
        }

        database.remove(productCode);
        return true;
    }

    // Updates the existing Product object instead of replacing it.
    // InventoryOfProduct holds this same object, so its displayed details
    // are updated without changing the stock quantity.
    public boolean editProduct(Product product, String productName,
            String productDescription, String productSource,
            double productCostInKG, double productRawPrice,
            double productEstimatedMarketPrice) {

        if (product == null) {
            return false;
        }

        String productCode = product.getProductCode();
        Product savedProduct = database.get(productCode);

        if (savedProduct == null) {
            return false;
        }

        savedProduct.update(productName, productDescription, productSource,
                productCostInKG, productRawPrice, productEstimatedMarketPrice);

        return true;
    }

    // Returns null when the product code is not found.
    public Product searchProduct(String productCode) {
        if (productCode == null) {
            return null;
        }

        return database.get(productCode);
    }

    // Returns a new list so callers cannot directly change the database map.
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
        this.databaseName = databaseName;
    }
}

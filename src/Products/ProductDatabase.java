package Products;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores products keyed by product code.
 * Uses LinkedHashMap so products keep their insertion order in the GUI table.
 * This class has no JavaFX code, so it can be reused and tested on its own.
 */
public class ProductDatabase {
	private String databaseName;
	private final Map<String, Product> database = new LinkedHashMap<>();

	public ProductDatabase(String databaseName) {
		this.setDatabaseName(databaseName);
	}

	public void addProduct(Product product) {
		if (product == null) {
			throw new IllegalArgumentException("Product cannot be null.");
		}
		if (database.containsKey(product.getProductCode())) {
			throw new IllegalArgumentException("Product code already exists: " + product.getProductCode());
		}
		database.put(product.getProductCode(), product);
	}

	public void removeProduct(Product product) {
		if (product != null) {
			database.remove(product.getProductCode());
		}
	}

	public void editProduct(Product product, String productName, String productDescription, String productSource,
			double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {
		if (product == null || !database.containsKey(product.getProductCode())) {
			throw new IllegalArgumentException("Product not found in the database.");
		}
		product.update(productName, productDescription, productSource, productCostInKG,
				productRawPrice, productEstimatedMarketPrice);
	}

	/** Returns the product with this code, or null if none exists. */
	public Product searchProduct(String productCode) {
		return database.get(productCode);
	}

	/** Returns a copy of all products, so callers cannot break the database by editing the list. */
	public List<Product> getAllProducts() {
		return new ArrayList<>(database.values());
	}

	public int size() {
		return database.size();
	}

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
		if (databaseName == null || databaseName.isBlank()) {
			throw new IllegalArgumentException("Database name is required.");
		}
		this.databaseName = databaseName.trim();
	}
}
package Products;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	public void editProduct(Product product, String productName, String productDescription, String productSource, double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {
		if (product == null || !database.containsKey(product.getProductCode())) {
			throw new IllegalArgumentException("Product not found in the database.");
		}
		
		product.update(productName, productDescription, productSource, productCostInKG, productRawPrice, productEstimatedMarketPrice);
	}
	

	//returns a product (or null if it doesn't exist)
	public Product searchProduct(String productCode) {
		return database.get(productCode);
	}

	//returns an array list of all products
	public List<Product> getAllProducts() {
		return new ArrayList<>(database.values());
	}

	public int size() {
		return database.size();
	}
	
	
	//for back end testing. Not to be viewed in the gui
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
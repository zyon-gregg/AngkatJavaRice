package Products;

import java.util.ArrayList;
import java.util.HashMap;

import users.Source;
import users.SourceDatabase;

public class ProductDatabase {
	private String databaseName;
	private HashMap<String,Product> database = new HashMap<>();
	
	public ProductDatabase(String databaseName){
		this.setDatabaseName(databaseName);
	}
	
	public void addProduct(Product product) {
		database.put(product.getProductCode(), product);
	}
	
	public void removeProduct(Product product) {
		database.remove(product.getProductCode());
	}
	
	public void editProduct(Product product, String productName, String productDescription, double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {
		
		if(!product.getProductName().equals(productName)) {
			product.setProductName(productName);
		}
		
		if(!product.getProductDescription().equals(productDescription)) {
			product.setProductDescription(productDescription);
		}
		
		if(!(product.getProductCostInKG() == (productCostInKG))) {
			product.setProductCostInKG(productCostInKG);
		}
		
		if(!(product.getProductRawPrice() == productRawPrice)) {
			product.setProductRawPrice(productRawPrice);
		}
		
		if(!(product.getProductEstimatedMarketPrice() == productEstimatedMarketPrice)) {
			product.setProductEstimatedMarketPrice(productEstimatedMarketPrice);
		}
		
		//Update Table Implementation or Something
	}
	
	public void addAllSourcesToDatabase(SourceDatabase sourceDB) {
		for(Source sources: sourceDB.returnSources()) {
			for(Product product: sources.returnProducts()) {
				this.addProduct(product);
			}
		}
	}
	
	public ArrayList<Product> getAllProducts() {
		return new ArrayList<Product>(database.values());
	}
	
	public String searchProduct(Product product) {
		for(String key: database.keySet()) {
			if(key.equals(product.getProductCode())) {
				return key;
			}
		}
		return null;
	}
	
	public void viewAllProducts() {
		for(String key : database.keySet()) {
			System.out.println(key + " : " + database.get(key).viewState());
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

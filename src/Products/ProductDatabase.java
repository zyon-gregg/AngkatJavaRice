package Products;

import java.util.HashMap;

public class ProductDatabase {
	private String databaseName;
	private HashMap<String,Product> database = new HashMap<>();
	
	ProductDatabase(String databaseName){
		this.databaseName = databaseName;
	}
	
	public void addProduct(Product product) {
		database.put(product.getProductCode(), product);
	}
	
	public void removeProduct(Product product) {
		if(database.containsValue(product)) {
			for(String key: this.database.keySet()) {
				if(database.get(key).equals(product)) {
					database.remove(key);
				}
			}
		}else{
			return;
		}
	}
	
	public void editProduct(Product product, String productName, String productDescription, double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {
		if(!product.getProductName().equals(productName)) {
			product.setProductName(productName);
		}
	}
	
	
}

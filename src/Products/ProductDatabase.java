package Products;

import java.util.HashMap;

public class ProductDatabase {
	private HashMap<String,Product> database = new HashMap<>();
	
	ProductDatabase(){
		
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
		}
	}
}

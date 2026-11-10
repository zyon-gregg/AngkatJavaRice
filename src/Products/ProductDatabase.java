package Products;

import java.util.HashMap;

public class ProductDatabase {
	private HashMap<String,Product> database = new HashMap<>();
	
	ProductDatabase(){
		
	}
	
	private void addProduct(Product product) {
		database.put(product.getProductCode(), product);
	}
}

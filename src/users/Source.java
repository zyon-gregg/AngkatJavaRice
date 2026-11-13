package users;


import java.util.HashMap;
import java.util.Set;

import Products.Product;

public class Source extends User{
	
	private HashMap<Product,Double> listOfSourceProductsAndQuantity = new HashMap<>();
	
	public Source(String username, String password, String sourceName){
		super(username,password,sourceName);
		super.setUserAccessLevel("SOURCE");
	}
	
	public void addProductToSource(Product product, Double quantity) {
		this.listOfSourceProductsAndQuantity.put(product, quantity);
	}
	
	public Set<Product> returnProducts(){
		return this.listOfSourceProductsAndQuantity.keySet();
	}
	
	public void removeProductofSource(Product product) {
		this.listOfSourceProductsAndQuantity.remove(product);
	}
	
	
}

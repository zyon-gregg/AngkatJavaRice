package users;


import java.util.HashMap;
import java.util.Set;

import Products.Product;

public class Source extends User{
	//Source hashmap with Products and Quantity 
	//Not used in this implementation, but may be expanded since in this implementation source is just a string for simplicity sake and time constraints
	//This class is for future use
	
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
	
	public HashMap<Product, Double> getSourceProductsAndQuantity() {
		return new HashMap<Product, Double>(this.listOfSourceProductsAndQuantity);
	}
	
	
}

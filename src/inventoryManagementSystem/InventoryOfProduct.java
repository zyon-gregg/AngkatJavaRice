package inventoryManagementSystem;

import java.time.*;
import Products.Product;



public class InventoryOfProduct {
	//This class is used to represent the stock of a specific product
	
	//attributes
	private Product product; //the product itself being stored
	private double productStockInKG;
	private LocalDateTime lastUpdated;
	
	public InventoryOfProduct(Product currentProduct) {//the original constructor used for creating a stock product during the program
		this.product = currentProduct;
		this.productStockInKG = 0;
		this.lastUpdated = LocalDateTime.now();
	}
	
	//method overloading, this is used during the loading of a saved InventoryofProduct
	public InventoryOfProduct(Product currentProduct, double stock, LocalDateTime update) {
		this.product = currentProduct;
		this.productStockInKG = stock;
		this.lastUpdated = update;
	}
	
	//getters
	public Product getProduct() {
		return product;
	}
	
	public double getProductStockInKG() {
		return productStockInKG;
	}
	
	//a setter
	void setProductStockInKG(double stock) {
		this.productStockInKG = stock;
		this.lastUpdated = LocalDateTime.now();
		
	}
	
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}
	
}

package inventoryManagementSystem;

import java.time.*;
import Products.Product;



public class InventoryOfProduct {
	
	//attributes
	private Product product;
	private double productStockInKG;
	private LocalDateTime lastUpdated;
	
	public InventoryOfProduct(Product currentProduct) {
		this.product = currentProduct;
		this.productStockInKG = 0;
		this.lastUpdated = LocalDateTime.now();
	}
	
	public InventoryOfProduct(Product currentProduct, double stock, LocalDateTime update) {//for loading
		this.product = currentProduct;
		this.productStockInKG = stock;
		this.lastUpdated = update;
	}
	
	public Product getProduct() {
		return product;
	}
	
	public double getProductStockInKG() {
		return productStockInKG;
	}
	
	void setProductStockInKG(double stock) {
		this.productStockInKG = stock;
		this.lastUpdated = LocalDateTime.now();
		
	}
	
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}
	
}

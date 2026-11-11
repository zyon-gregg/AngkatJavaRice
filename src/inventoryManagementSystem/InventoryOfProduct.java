package inventoryManagementSystem;

import java.time.*;
import Products.Product;



public class InventoryOfProduct {
	
	//attributes
	private Product product;
	private double productStockInKG;
	//private double maxStock;
	//private double reorderLevel;
	private LocalDateTime lastUpdated;
	
	public InventoryOfProduct(Product currentProduct) {
		this.product = currentProduct;
		this.productStockInKG = 0;
		//this.maxStock = 1000.00;
		//this.reorderLevel = 300.00;
		this.lastUpdated = LocalDateTime.now();
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
	
	/*
	
	public double getMaxStock() {
		return maxStock;
	}
	
	public double getReorderLevel() {
		return reorderLevel;
	}
	
	*/
	
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}
	
}

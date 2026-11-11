package inventoryManagementSystem;

import java.time.*;
import productManagementSystem.Product;



public class InventoryOfProduct {
	
	//attributes
	private String productCode;
	private Product product;
	private double productStockInKG;
	private double reorderLevelInKG;
	private LocalDateTime lastUpdated;
	
	public InventoryOfProduct(Product currentProduct) {
		this.productCode = currentProduct.getProductCode();
		this.ReorderLevel = 0;
		this.monthChanged = month;
		this.dayChanged = day;
		this.yearChanged = year;
	}
	
	public String getProductCode() {
		return ProductCode;
	}
	
	public int getReorderLevel() {
		return ReorderLevel;
	}
	
	public String getMonthChanged() {
		return monthChanged;
	}
	
	public int getDayChanged() {
		return dayChanged;
	}
	
	public int getYearChanged() {
		return yearChanged;
	}

}

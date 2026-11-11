package inventoryManagementSystem;

import java.time.*;
import Products.Product;


public class StockRecordedChange {
	
	//the main purpose of this is to simulate something like an audit system. Every change must be recorded.
	
	public static final String RESTOCK = "RESTOCK";
	public static final String SALE_DEDUCTION = "DEDUCTION_FROM_SALE";
	public static final String STOCK_ADJUSTMENT = "STOCK_ADJUSTMENT";
	public static final String SALE_RESTORED = "RESTORED_FROM_SALE";
	
	private String recordID;
	private Product product;
	private String changeType;
	private double previousStock;
	private double newStock;
	private  LocalDateTime time;
	private String performedBy; //keeps track on who performed it (could be manager or employee)
	private String explanation;
	
	public StockRecordedChange(String id, Product recordedProduct, String type, double prev, double updated, LocalDateTime recordedTime, String username, String reason) {
		this.recordID = id;
		this.product = recordedProduct;
		this.changeType = type;
		this.previousStock = prev;
		this.newStock = updated;
		this.time = recordedTime;
		this.performedBy = username;
		this.explanation = reason;
	}
	
	//getters
	
	public String getRecordId() {
		return recordID;
	}
	
	public Product getProduct() {
		return product;
	}
	
	public String getProductCode() {
		return product.getProductCode();
	}
	
	public String getChangeType() {
		return changeType;
	}
	
	public double getPreviousStock() {
		return previousStock;
	}
	
	public double getNewStock() {
		return newStock;
	}
	
	public double getAmountChanged() {
	    return newStock - previousStock;
	}
	
	public LocalDateTime getTime() {
		return time;
	}
	
	public String getPerformedBy() {
		return performedBy;
	}
	
	public String getExplanation() {
		return explanation;
	}
	
	
	

}

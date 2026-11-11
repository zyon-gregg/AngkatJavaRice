package productManagementSystem;

import java.util.HashMap;

public class Product {
	
	//Attributes
	
	private static int nextId = 1;
	private String productCode;
	private String productName;
	private double productCostInKG;
	private double productStockInKG;
	private double productRawPrice;
	private double productEstimatedMarketPrice;
	
	//Constructor
	
	Product(HashMap<String,Product> database, String productName, double productCostInKG, double productStockInKG, double productRawPrice, double productEstimatedMarketPrice){
		this.setProductCode(database);
		this.productName = productName;
		this.productCostInKG = productCostInKG;
		this.productStockInKG = productStockInKG;
		this.productRawPrice = productRawPrice;
		this.productEstimatedMarketPrice = productEstimatedMarketPrice;
	}
	
	
	//Methods
	
	protected void setProductCode(HashMap<String,Product> database) {
		String formatted = String.format("P%05D",nextId);
		nextId++;
		this.productCode = formatted;
	}
	
	public double reduceStock(double itemBoughtInKG) {
		this.productStockInKG = this.productStockInKG - itemBoughtInKG;
		return this.productStockInKG;
	}
	
	
	
	
	
	
	
	//Auto generated Getters and Setters
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public double getProductCostInKG() {
		return productCostInKG;
	}

	public void setProductCostInKG(double productCostInKG) {
		this.productCostInKG = productCostInKG;
	}

	public double getProductStockInKG() {
		return productStockInKG;
	}

	public void setProductStockInKG(double productStockInKG) {
		this.productStockInKG = productStockInKG;
	}

	public double getProductRawPrice() {
		return productRawPrice;
	}

	public void setProductRawPrice(double productRawPrice) {
		this.productRawPrice = productRawPrice;
	}

	public double getProductEstimatedMarketPrice() {
		return productEstimatedMarketPrice;
	}

	public void setProductEstimatedMarketPrice(double productEstimatedMarketPrice) {
		this.productEstimatedMarketPrice = productEstimatedMarketPrice;
	}
	
}

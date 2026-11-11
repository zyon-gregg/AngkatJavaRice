package Products;

import java.util.HashMap;

public class Product {
	
	//Attributes
	
	private static int nextId = 1;
	private String productCode;
	private String productName;
	private String productDescription;
	private double productCostInKG;
	private double productRawPrice;
	private double productEstimatedMarketPrice;
	
	//Constructor
	
	Product(HashMap<String,Product> database, String productName, String productDescription, double productCostInKG, double productRawPrice, double productEstimatedMarketPrice){
		this.setProductCode(database);
		this.productName = productName;
		this.setProductDescription(productDescription);
		this.productCostInKG = productCostInKG;
		this.productRawPrice = productRawPrice;
		this.productEstimatedMarketPrice = productEstimatedMarketPrice;
	}
	
	
	//Methods
	
	protected void setProductCode(HashMap<String,Product> database) {
		
		//Product Code Format: 00001, 00010, -> 99999 (Can Be Expanded Later if Needed)
		
		String formatted = String.format("P%05D",nextId);
		nextId++;
		this.productCode = formatted;
	}
	
	
	public String viewState() {
		String compiled = this.productCode + "|" + this.productName + "|" + this.productDescription + "|" + this.productCostInKG  + "|" + this.productRawPrice + "|" + this.productEstimatedMarketPrice;
		return compiled;
	
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


	public String getProductDescription() {
		return productDescription;
	}


	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	
}

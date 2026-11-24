package Products;

public class Product {

	// Attributes
	private static int nextId = 1;
	private String productCode;
	private String productName;
	private String productDescription;
	private String productSource; // fishing ground, fish pen, or supplier
	private double productCostInKG;
	private double productRawPrice;
	private double productEstimatedMarketPrice;
	

	public Product(ProductDatabase database, String productName, String productDescription, String productSource, double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {
		
		this.productCode = String.format("P%05d", nextId);
		this.productName = productName;
		this.productDescription = productDescription;
		this.productSource = productSource;
		this.productCostInKG = productCostInKG;
		this.productRawPrice = productRawPrice;
		this.productEstimatedMarketPrice = productEstimatedMarketPrice;
	}

	//this constructor is for loading saved products from a file
	public Product(String productCode, String productName, String productDescription, String productSource, double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {

		this.productCode = productCode;
		this.productName = productName;
		this.productDescription = productDescription;
		this.productSource = productSource;
		this.productCostInKG = productCostInKG;
		this.productRawPrice = productRawPrice;
		this.productEstimatedMarketPrice = productEstimatedMarketPrice;
		syncNextId(productCode);
	}

	//Keeps nextId ahead of any code in use, so IDs stay unique after loading from a file.
	private static void syncNextId(String code) {
		
		try {
			int number = Integer.parseInt(code.substring(1));
			
			if (number >= nextId) {
				nextId = number + 1;
			}
			
		} catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException ex) {
			throw new IllegalArgumentException("Invalid product code: " + code);
		}
	}

	//This is for updating an information about a specific product
	public void update(String productName, String productDescription, String productSource, double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {

		this.productName = productName;
		this.productDescription = productDescription;
		this.productSource = productSource;
		this.productCostInKG = productCostInKG;
		this.productRawPrice = productRawPrice;
		this.productEstimatedMarketPrice = productEstimatedMarketPrice;
	}

	//formatted for saving it to a text file
	public String viewState() {
		return this.productCode + "|" + this.productName + "|" + this.productDescription + "|" + this.productSource + "|" + this.productCostInKG + "|" + this.productRawPrice + "|" + this.productEstimatedMarketPrice;
	}

	// Getters and Setters
	public String getProductCode() { 
		return productCode; 
	}

	public String getProductName() {
		return productName; 
	}
	
	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductDescription() { 
		return productDescription; 
	}
	
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}

	public String getProductSource() { 
		return productSource; 
	}
	
	public void setProductSource(String productSource) {
		this.productSource = productSource;
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
}

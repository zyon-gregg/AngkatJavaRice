package Products;

/**
 * A fish product handled at the port.
 * All setters validate their input and throw IllegalArgumentException,
 * so the GUI only needs to catch the exception and show its message.
 */
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
	

	// Constructors

	/** Creates a NEW product and auto-generates its code (P00001, P00002, ...). */
	public Product(ProductDatabase database, String productName, String productDescription, String productSource,
			double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {
		this(String.format("P%05d", nextId), productName, productDescription, productSource,
				productCostInKG, productRawPrice, productEstimatedMarketPrice);
	}

	/** Used when LOADING saved products from a file (the code already exists). */
	public Product(String productCode, String productName, String productDescription, String productSource,
			double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {
		// Validate everything first so a bad product never consumes an ID
		String name = requireText(productName, "Product name");
		String source = requireText(productSource, "Source");
		double cost = requirePositive(productCostInKG, "Cost per kg");
		double raw = requirePositive(productRawPrice, "Raw price");
		double market = requirePositive(productEstimatedMarketPrice, "Estimated market price");

		this.productCode = productCode;
		this.productName = name;
		this.productDescription = productDescription == null ? "" : productDescription.trim();
		this.productSource = source;
		this.productCostInKG = cost;
		this.productRawPrice = raw;
		this.productEstimatedMarketPrice = market;
		syncNextId(productCode);
	}

	// Methods


	/** Keeps nextId ahead of any code in use, so IDs stay unique after loading from a file. */
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

	/** Edits all editable fields at once. Nothing changes if any value is invalid. */
	public void update(String productName, String productDescription, String productSource,
			double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {
		String name = requireText(productName, "Product name");
		String source = requireText(productSource, "Source");
		double cost = requirePositive(productCostInKG, "Cost per kg");
		double raw = requirePositive(productRawPrice, "Raw price");
		double market = requirePositive(productEstimatedMarketPrice, "Estimated market price");

		this.productName = name;
		this.productDescription = productDescription == null ? "" : productDescription.trim();
		this.productSource = source;
		this.productCostInKG = cost;
		this.productRawPrice = raw;
		this.productEstimatedMarketPrice = market;
	}

	/** Pipe-separated line, handy for saving to a text file. */
	public String viewState() {
		return this.productCode + "|" + this.productName + "|" + this.productDescription + "|"
				+ this.productSource + "|" + this.productCostInKG + "|" + this.productRawPrice + "|"
				+ this.productEstimatedMarketPrice;
	}

	private static String requireText(String value, String label) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(label + " is required.");
		}
		return value.trim();
	}

	private static double requirePositive(double value, String label) {
		if (Double.isNaN(value) || value <= 0) {
			throw new IllegalArgumentException(label + " must be greater than zero.");
		}
		return value;
	}

	// Getters and Setters (setters validate)

	public String getProductCode() { return productCode; }

	public String getProductName() { return productName; }
	public void setProductName(String productName) {
		this.productName = requireText(productName, "Product name");
	}

	public String getProductDescription() { return productDescription; }
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription == null ? "" : productDescription.trim();
	}

	public String getProductSource() { return productSource; }
	public void setProductSource(String productSource) {
		this.productSource = requireText(productSource, "Source");
	}

	public double getProductCostInKG() { return productCostInKG; }
	public void setProductCostInKG(double productCostInKG) {
		this.productCostInKG = requirePositive(productCostInKG, "Cost per kg");
	}

	public double getProductRawPrice() { return productRawPrice; }
	public void setProductRawPrice(double productRawPrice) {
		this.productRawPrice = requirePositive(productRawPrice, "Raw price");
	}

	public double getProductEstimatedMarketPrice() { return productEstimatedMarketPrice; }
	public void setProductEstimatedMarketPrice(double productEstimatedMarketPrice) {
		this.productEstimatedMarketPrice = requirePositive(productEstimatedMarketPrice, "Estimated market price");
	}
}

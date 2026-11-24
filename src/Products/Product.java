package Products;

public class Product {

    private static int nextId = 1;

    private String productCode;
    private String productName;
    private String productDescription;
    private String productSource;
    private double productCostInKG;
    private double productRawPrice;
    private double productEstimatedMarketPrice;

    // The database parameter is kept so existing GUI code does not need to change.
    public Product(ProductDatabase database, String productName, String productDescription,
            String productSource, double productCostInKG, double productRawPrice,
            double productEstimatedMarketPrice) {

        this.productCode = String.format("P%05d", nextId);
        nextId++;

        this.productName = productName;
        this.productDescription = productDescription;
        this.productSource = productSource;
        this.productCostInKG = productCostInKG;
        this.productRawPrice = productRawPrice;
        this.productEstimatedMarketPrice = productEstimatedMarketPrice;
    }

    // This constructor is used when products are loaded from a saved file.
    public Product(String productCode, String productName, String productDescription,
            String productSource, double productCostInKG, double productRawPrice,
            double productEstimatedMarketPrice) {

        this.productCode = productCode;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productSource = productSource;
        this.productCostInKG = productCostInKG;
        this.productRawPrice = productRawPrice;
        this.productEstimatedMarketPrice = productEstimatedMarketPrice;

        updateNextId(productCode);
    }

    // Makes sure the next generated code does not repeat a code loaded from a file.
    private static void updateNextId(String productCode) {
        if (productCode == null || productCode.length() < 2) {
            return;
        }

        try {
            int savedId = Integer.parseInt(productCode.substring(1));

            if (savedId >= nextId) {
                nextId = savedId + 1;
            }
        } catch (NumberFormatException e) {
            // If the saved code is not in the expected format, leave nextId unchanged.
        }
    }

    public void update(String productName, String productDescription, String productSource,
            double productCostInKG, double productRawPrice, double productEstimatedMarketPrice) {

        this.productName = productName;
        this.productDescription = productDescription;
        this.productSource = productSource;
        this.productCostInKG = productCostInKG;
        this.productRawPrice = productRawPrice;
        this.productEstimatedMarketPrice = productEstimatedMarketPrice;
    }

    // Returns the product information in a format that can be saved to a text file.
    public String viewState() {
        return productCode + "|" + productName + "|" + productDescription + "|" + productSource
                + "|" + productCostInKG + "|" + productRawPrice + "|" + productEstimatedMarketPrice;
    }

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
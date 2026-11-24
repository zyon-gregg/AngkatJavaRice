package transaction;

public final class ReceiptItem {

    private final String productCode;
    private final String productName;
    private final double quantityInKG;
    private final double unitPrice;
    private final double subtotal;

    public ReceiptItem(String productCode, String productName, double quantityInKG, double unitPrice) {
        if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Receipt item product code is required.");
        }
        
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Receipt item product name is required.");
        }
        
        if (!Double.isFinite(quantityInKG) || quantityInKG <= 0) {
            throw new IllegalArgumentException("Receipt item quantity must be finite and greater than zero.");
        }
        
        if (!Double.isFinite(unitPrice) || unitPrice <= 0) {
            throw new IllegalArgumentException("Receipt item unit price must be finite and greater than zero.");
        }

        double calculatedSubtotal = quantityInKG * unitPrice;
        
        if (!Double.isFinite(calculatedSubtotal)) {
            throw new IllegalArgumentException("Receipt item subtotal is invalid.");
        }

        this.productCode = productCode;
        this.productName = productName;
        this.quantityInKG = quantityInKG;
        this.unitPrice = unitPrice;
        this.subtotal = calculatedSubtotal;
    }
    
    //getters

    public String getProductCode() { 
    	return productCode; 	
    }
    
    public String getProductName() { 
    	return productName; 
    }
    
    public double getQuantityInKG() { 
    	return quantityInKG; 
    }
    
    public double getUnitPrice() { 
    	return unitPrice; 
    }
    
    public double getSubtotal() { 
    	return subtotal; 
    }
}
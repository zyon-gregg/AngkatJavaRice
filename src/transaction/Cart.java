package transaction;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import Products.Product;
import users.User;

public class Cart {

    private String saleID;
    private final HashMap<Product, Double> cart = new HashMap<>();
    private final HashMap<Product, Double> unitPrices = new HashMap<>();
    private final HashMap<Product, String> productNames = new HashMap<>();
    private Double totalCartValue = 0.0;
    private LocalDateTime lastUpdated;
    private User processing;
    private String employeeLevelofProcessing;
    private Receipt receipt;
    private boolean isPaid = false;

    Cart() {
    }

    // Gets the selling price used when a product is first added to the cart.
    public static double unitPrice(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }

        double price = product.getProductRawPrice();

        if (price <= 0 || Double.isNaN(price) || Double.isInfinite(price)) {
            throw new IllegalArgumentException("Product has an invalid selling price.");
        }

        return price;
    }
    

    // Adds a product to the cart. Adding it again replaces its quantity.
    // The first selling price is kept so later product price edits do not change this cart.
    public void addToCart(Product product, Double quantity) {
        ensureNotPaid();

        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }

        if (quantity == null || quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        if (!cart.containsKey(product)) {
            unitPrices.put(product, unitPrice(product));
            productNames.put(product, product.getProductName());
        }

        cart.put(product, quantity);
        updateCartValue();
    }

    public void removeFromCart(Product product) {
        ensureNotPaid();

        cart.remove(product);
        unitPrices.remove(product);
        productNames.remove(product);
        updateCartValue();
    }

    // Recalculates the total using the saved price for each cart item.
    public void updateCartValue() {
        double total = 0.0;

        for (Map.Entry<Product, Double> item : cart.entrySet()) {
        	
            Product product = item.getKey();
            Double quantity = item.getValue();
            Double price = unitPrices.get(product);

            if (product == null || quantity == null || price == null) {
                throw new IllegalStateException("The cart contains an incomplete item.");
            }

            if (quantity <= 0 || price <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity) || Double.isNaN(price) || Double.isInfinite(price)) {
                throw new IllegalStateException("The cart contains an invalid quantity or price.");
            }

            total = total + (price * quantity);

            if (Double.isNaN(total) || Double.isInfinite(total)) {
                throw new IllegalStateException("The cart total is invalid.");
            }
        }

        totalCartValue = total;
        lastUpdated = LocalDateTime.now();
    }

    public double getUnitPrice(Product product) {
        Double savedPrice = unitPrices.get(product);

        if (savedPrice == null) {
            return unitPrice(product);
        }

        return savedPrice;
    }

    public String getProductName(Product product) {
        String savedName = productNames.get(product);

        if (savedName == null) {
            return product.getProductName();
        }

        return savedName;
    }

    public double getSubtotal(Product product) {
        Double quantity = cart.get(product);

        if (quantity == null) {
            return 0.0;
        }

        return getUnitPrice(product) * quantity;
    }

    public String getSaleID() {
        return saleID;
    }

    public void setSaleID(String saleID) {
        this.saleID = saleID;
    }

    // Returns a copy of the cart items.
    public HashMap<Product, Double> getCart() {
        return new HashMap<>(cart);
    }

    public void setCart(HashMap<Product, Double> items) {
        ensureNotPaid();

        cart.clear();
        unitPrices.clear();
        productNames.clear();

        if (items != null) {
            for (Map.Entry<Product, Double> item : items.entrySet()) {
                addToCart(item.getKey(), item.getValue());
            }
            
        } else {
            updateCartValue();
        }
    }

    public Double getTotalCartValue() {
        return totalCartValue;
    }

    public void setTotalCartValue(Double totalCartValue) {
        if (totalCartValue == null || totalCartValue < 0 || Double.isNaN(totalCartValue) || Double.isInfinite(totalCartValue)) {
            throw new IllegalArgumentException("Cart total must be zero or greater.");
        }

        this.totalCartValue = totalCartValue;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public User getProcessing() {
        return processing;
    }

    public void setProcessing(User processing) {
        this.processing = processing;
    }

    public String getEmployeeLevelofProcessing() {
        return employeeLevelofProcessing;
    }

    public void setEmployeeLevelofProcessing(String employeeLevelofProcessing) {
        this.employeeLevelofProcessing = employeeLevelofProcessing;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public boolean isEmpty() {
        return cart.isEmpty();
    }

    public boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    private void ensureNotPaid() {
        if (isPaid) {
            throw new IllegalStateException("A completed sale cannot be modified.");
        }
    }
}
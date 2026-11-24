package transaction;

import java.time.LocalDateTime;
import java.util.Collections;
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
    private Boolean isPaid = false;

    Cart() {
    }

    /** Price used when a product is first added to a cart. */
    public static double unitPrice(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }
        double price = product.getProductRawPrice();
        if (!Double.isFinite(price) || price <= 0) {
            throw new IllegalArgumentException("Product has an invalid selling price.");
        }
        return price;
    }
    
    //adds a product, replacing its quantity, and locking its price at the first add

    public void addToCart(Product product, Double quantity) {
        ensureNotPaid();

        if (product == null) {
            throw new IllegalArgumentException("Product is required.");
        }
        
        if (quantity == null || !Double.isFinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a finite number greater than zero.");
        }

        if (!cart.containsKey(product)) { //avoid duplication of product to the cart
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

    public void updateCartValue() { //updates the total price of the cart each time a product is being added
        double total = 0.0;
        
        for (Map.Entry<Product, Double> item : cart.entrySet()) {
            Product product = item.getKey();
            Double quantity = item.getValue();
            Double price = unitPrices.get(product);

            if (product == null || quantity == null || !Double.isFinite(quantity) || quantity <= 0
                    || price == null || !Double.isFinite(price) || price <= 0) {
                throw new IllegalStateException("The cart contains an invalid item.");
            }

            total += price * quantity;
            
            if (!Double.isFinite(total)) {
                throw new IllegalStateException("The cart total is too large or invalid.");
            }
        }
        
        
        totalCartValue = total;
        lastUpdated = LocalDateTime.now();
    }

    public double getUnitPrice(Product product) {
        Double price = unitPrices.get(product);
        return price == null ? unitPrice(product) : price;
    }

    public String getProductName(Product product) {
        String name = productNames.get(product);
        return name == null ? product.getProductName() : name;
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

    //returns the copy of the cart
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
        if (totalCartValue == null || !Double.isFinite(totalCartValue) || totalCartValue < 0) {
            throw new IllegalArgumentException("Cart total must be a finite non-negative amount.");
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

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    private void ensureNotPaid() {
        if (Boolean.TRUE.equals(isPaid)) {
            throw new IllegalStateException("A completed sale cannot be modified.");
        }
    }
}
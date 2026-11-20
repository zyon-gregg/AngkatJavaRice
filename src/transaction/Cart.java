package transaction;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import Products.Product;
import users.User;

public class Cart {
	private String saleID;
	private HashMap<Product,Double> cart = new HashMap<>();
	private Double totalCartValue = 0.0;      // CHANGED: was null, which crashed updateCartValue()
	private LocalDateTime lastUpdated;
	private User processing;
	private String employeeLevelofProcessing;
	private Receipt receipt;
	private Boolean isPaid = false;           // CHANGED: was null

	Cart(){

	}

	/**
	 * CHANGED: the price per kg used for every sale.
	 * To sell at another price (e.g. estimated market price), change it here only.
	 */
	public static double unitPrice(Product product) {
		return product.getProductRawPrice();
	}

	/** Adds the product, or replaces its quantity if it is already in the cart. */
	public void addToCart(Product product, Double quant) {
		if (product == null) {
			throw new IllegalArgumentException("Product is required.");
		}
		if (quant == null || quant.isNaN() || quant <= 0) {
			throw new IllegalArgumentException("Quantity must be greater than zero.");
		}
		this.cart.put(product, quant);
		updateCartValue();
	}

	public void removeFromCart(Product product) {
		this.cart.remove(product);
		updateCartValue();
	}

	/** CHANGED: total is now price x kg for each item (it used to add up the kg only), recomputed from zero. */
	public void updateCartValue() {
		double total = 0;
		for (Map.Entry<Product, Double> item : this.cart.entrySet()) {
			total += unitPrice(item.getKey()) * item.getValue();
		}
		this.totalCartValue = total;
		this.lastUpdated = LocalDateTime.now();
	}

	public String getSaleID() {
		return saleID;
	}

	public void setSaleID(String saleID) {
		this.saleID = saleID;
	}

	public HashMap<Product,Double> getCart() {
		return cart;
	}

	public void setCart(HashMap<Product,Double> cart) {
		this.cart = cart;
		updateCartValue();
	}

	public Double getTotalCartValue() {
		return totalCartValue;
	}

	public void setTotalCartValue(Double totalCartValue) {
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
		return this.cart.isEmpty();
	}

	public Boolean getIsPaid() {
		return isPaid;
	}

	public void setIsPaid(Boolean isPaid) {
		this.isPaid = isPaid;
	}

}
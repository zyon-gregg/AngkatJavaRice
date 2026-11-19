package transaction;

import java.time.LocalDateTime;
import java.util.HashMap;

import Products.Product;
import users.Clerk;
import users.User;

public class Cart {
	private String saleID;
	private HashMap<Product,Double> cart = new HashMap<>();
	private Double totalCartValue;
	private LocalDateTime lastUpdated;
	private User processing;
	private String employeeLevelofProcessing;
	private Receipt receipt;
	private Boolean isPaid;
	Cart(){
		
	}
	
	public void addToCart(Product product, Double quant) {
		this.cart.put(product, quant);
	}
	
	public void removeFromCart(Product product) {
		this.cart.remove(product);
	}
	
	public void updateCartValue() {
		for(Double val: this.cart.values()) {
			this.totalCartValue += val;
		}
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

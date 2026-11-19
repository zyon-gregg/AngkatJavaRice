package users;

import java.util.HashMap;

import Products.Product;
import transaction.Cart;
import transaction.Receipt;

public class Buyer extends User{
	
	private double budget;
	private Cart buyerCart;
	private Receipt buyerReceipt;
	Buyer(String username, String password, String name) {
		super(username, password, name);
	}
	
	public double returnTotalValueProducts() {
		double summation = 0;
		
		return summation;
	}

	public double getBudget() {
		return budget;
	}

	public void setBudget(double budget) {
		this.budget = budget;
	}


	public Cart getBuyerCart() {
		return buyerCart;
	}

	public void setBuyerCart(Cart buyerCart) {
		this.buyerCart = buyerCart;
	}

	public Receipt getBuyerReceipt() {
		return buyerReceipt;
	}

	public void setBuyerReceipt(Receipt buyerReceipt) {
		this.buyerReceipt = buyerReceipt;
	}

}

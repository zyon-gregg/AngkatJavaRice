package users;

import transaction.Cart;
import transaction.Receipt;

public class Buyer extends User {
	//Buyer has a Cart and a Budget
	private double budget;
	private Cart buyerCart;

	// Order is (username, password, name, budget), like the other users plus a budget
	public Buyer(String username, String password, String name, double budget) {
		super(username, password, name);
		setUserAccessLevel("BUYER");
		setBudget(budget);
	}

	//Return
	public double getBudget() {
		return budget;
	}
	//Errors if Invalid
	public void setBudget(double budget) {
		if (Double.isNaN(budget) || budget < 0) {
			throw new IllegalArgumentException("Budget cannot be negative.");
		}
		this.budget = budget;
	}

	public Cart getBuyerCart() {
		return buyerCart;
	}

	public void setBuyerCart(Cart buyerCart) {
		this.buyerCart = buyerCart;
	}

	/** The receipt of the sale in progress (or the last sale), or null if there is no cart. */
	public Receipt getBuyerReceipt() {
		if (buyerCart == null) {
			return null;
		}

		return buyerCart.getReceipt();
	}
}
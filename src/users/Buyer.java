package users;

import transaction.Cart;
import transaction.Receipt;

/**
 * A customer who buys fish. Holds a budget and the cart of the sale in progress.
 * Minimal version: only what the sales screen needs.
 */
public class Buyer extends User {

	private double budget;
	private Cart buyerCart;

	// Order is (username, password, name, budget), like the other users plus a budget
	public Buyer(String username, String password, String name, double budget) {
		super(username, password, name);
		setUserAccessLevel("BUYER");
		setBudget(budget);
	}


	public double getBudget() {
		return budget;
	}

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
		return buyerCart == null ? null : buyerCart.getReceipt();
	}
}
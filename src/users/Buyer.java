package users;

import java.util.HashMap;

import Products.Product;

public class Buyer extends User{
	
	private double budget;
	private HashMap<Product,Double> itemsBought = new HashMap<>();
	
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

	public HashMap<Product,Double> getItemsBought() {
		return itemsBought;
	}

	public void setItemsBought(HashMap<Product,Double> itemsBought) {
		this.itemsBought = itemsBought;
	}

}

package users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class BuyerDatabase {
	
	//Buyer Database attributes
	private String databaseName;
	private HashMap<String,Buyer> database = new HashMap<>();
	
	
	//getters and setters
	public BuyerDatabase(String databaseName){
		this.setDatabaseName(databaseName);
	}
	//Adder
	public void addBuyerToDatabase(Buyer buyer) {
		this.database.put(buyer.getUserName(), buyer);
	}
	//Remover
	public void removeBuyerFromDatabase(Buyer buyer) {
		this.database.remove(buyer.getUserName());
	}
	//Returns the Buyers
	public Collection<Buyer> returnBuyers(){
		return this.database.values();
	}
	//returns a list of buyers
	public ArrayList<Buyer> returnBuyerList(){
		return new ArrayList<>(this.database.values());
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
}
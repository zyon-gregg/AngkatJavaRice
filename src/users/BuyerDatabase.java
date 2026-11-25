package users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class BuyerDatabase {
	
	private String databaseName;
	private HashMap<String,Buyer> database = new HashMap<>();
	
	

	public BuyerDatabase(String databaseName){
		this.setDatabaseName(databaseName);
	}
	
	public void addBuyerToDatabase(Buyer buyer) {
		this.database.put(buyer.getUserName(), buyer);
	}

	public void removeBuyerFromDatabase(Buyer buyer) {
		this.database.remove(buyer.getUserName());
	}
	
	public Collection<Buyer> returnBuyers(){
		return this.database.values();
	}
	
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
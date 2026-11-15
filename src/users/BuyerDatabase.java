package users;

import java.util.Collection;
import java.util.HashMap;

public class BuyerDatabase {
	
	private String databaseName;
	private HashMap<String,Buyer> database = new HashMap<>();
	
	

	public BuyerDatabase(String databaseName){
		this.setDatabaseName(databaseName);
	}
	
	public void addSourceToDatabase(Buyer buyer) {
		this.database.put(buyer.getName(), buyer);
	}

	public void removeSourceFromDatabase(Buyer buyer) {
		this.database.remove(buyer.getName());
	}
	
	public Collection<Buyer> returnSources(){
		return this.database.values();
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
}

package users;

import java.util.Collection;
import java.util.HashMap;


public class SourceDatabase {
	private String databaseName;
	private HashMap<String,Source> database = new HashMap<>();
	
	
	public SourceDatabase(String databaseName){
		this.setDatabaseName(databaseName);
	}
	
	public void addSourceToDatabase(Source source) {
		this.database.put(source.getUserName(), source);
	}

	public void removeSourceFromDatabase(Source source) {
		this.database.remove(source.getUserName());
	}
	
	public Collection<Source> returnSources(){
		return this.database.values();
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

}

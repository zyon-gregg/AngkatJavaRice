package users;

import java.util.HashMap;


public class SourceDatabase {
	private String databaseName;
	private HashMap<String,Source> database = new HashMap<>();

	SourceDatabase(String databaseName){
		this.setDatabaseName(databaseName);
	}
	
	public void addSourceToDatabase(Source source) {
		this.database.put(source.getName(), source);
	}

	public void removeSourceFromDatabase(Source source) {
		this.database.remove(source.getName());
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

}

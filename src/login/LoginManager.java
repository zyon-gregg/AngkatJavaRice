package login;

import java.util.HashMap;

import users.User;

public class LoginManager {
	
	private HashMap<String,User> accountsList = new HashMap<>();
	
	LoginManager(){
		
	}
	
	public void addAccount(User account) {
		this.accountsList.put(account.getUserID(), account);
	}
	
	public void removeAccount(User account) {
		this.accountsList.remove(account.getUserID());
	}
	
	public boolean login(String username, String password) {
		
		for(User user : this.accountsList.values()) {
			if(user.getUserName().equals(username)) {
				if(user.getPassword().equals(password)) {
					
					
					//Add Login Cases based on their Type and Stuff
					return true;
				}
			}
		}
		return false;
	}
}

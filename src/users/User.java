package users;

public abstract class User {
	private String userID;
	private String userName;
	private String name;
	private String password;
	private String userAccessLevel;
	
	User(String username, String password, String name){
		this.setUserName(username);
		this.setPassword(password);
		this.setName(name);
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getUserAccessLevel() {
		return userAccessLevel;
	}

	public void setUserAccessLevel(String userAccessLevel) {
		this.userAccessLevel = userAccessLevel;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	
	
}

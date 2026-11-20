package users;

<<<<<<< HEAD

public abstract class User {
=======
public class User {
>>>>>>> branch 'main' of https://github.com/zyon-gregg/AngkatJavaRice.git
	private String userID;
	private String userName;
	private String name;
	private String password;
	private String userAccessLevel;
	private Boolean activated;
	
	public User(String username, String password, String name){
		this.setUserName(username);
		this.setPassword(password);
		this.setName(name);
	}

	/** The role name used by the GUI, e.g. "manager" or "clerk". */
	public abstract String getRole();
	
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

	public Boolean getActivated() {
		return activated;
	}

	public void setActivated(Boolean activated) {
		this.activated = activated;
	}
	
	
	
	
}

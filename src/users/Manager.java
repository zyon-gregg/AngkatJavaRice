package users;

public class Manager extends User {
	public Manager(String username, String password, String name) {
		super(username, password, name);
		setUserAccessLevel("MANAGER");
	}
}
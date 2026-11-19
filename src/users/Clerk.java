package users;

public class Clerk extends User {
	public Clerk(String username, String password, String name) {
		super(username, password, name);
		setUserAccessLevel("CLERK");
	}
}
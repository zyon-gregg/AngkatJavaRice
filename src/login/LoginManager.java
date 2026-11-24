package login;

import java.util.ArrayList;
import java.util.HashMap;

import users.User;

public class LoginManager {

    private HashMap<String, User> accountsList = new HashMap<String, User>();
    private ArrayList<User> pending = new ArrayList<User>();
    private User loggedInUser;
    private int nextUserID = 1;

    public LoginManager() {
    }

    // Adds an activated or inactive account to the registered account list.
    public void addAccount(User account) {
        if (account == null) {
            return;
        }

        String userID = account.getUserID();

        if (userID == null || userID.trim().isEmpty()) {
            userID = generateUserID();
            account.setUserID(userID);
        }

        if (usernameExists(account.getUserName(), account)) {
            return;
        }

        accountsList.put(userID, account);
    }

    public void removeAccount(User account) {
        if (account == null) {
            return;
        }

        accountsList.remove(account.getUserID());

        if (loggedInUser == account) {
            loggedInUser = null;
        }
    }

    // Checks the username and password of an active account.
    public boolean login(String username, String password) {
        loggedInUser = null;

        if (username == null || password == null) {
            return false;
        }

        for (User user : accountsList.values()) {
            if (user == null) {
                continue;
            }

            String savedUsername = user.getUserName();
            String savedPassword = user.getPassword();

            if (savedUsername != null && savedUsername.equalsIgnoreCase(username)) {
                if (savedPassword != null && savedPassword.equals(password)) {
                    if (user.getActivated()) {
                        String role = user.getUserAccessLevel();

                        if ("MANAGER".equalsIgnoreCase(role) || "CLERK".equalsIgnoreCase(role)) {
                            loggedInUser = user;
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void logout() {
        loggedInUser = null;
    }

    // Public registration always creates a pending Clerk account.
    // A Manager chooses the final role when approving the request.
    public boolean registerUser(String username, String password, String name) {
        if (username == null || password == null || name == null) {
            return false;
        }

        if (username.trim().isEmpty() || password.trim().isEmpty() || name.trim().isEmpty()) {
            return false;
        }

        if (usernameExists(username, null)) {
            return false;
        }

        User newUser = new User(username.trim(), password, name.trim());
        newUser.setActivated(false);
        newUser.setUserAccessLevel("CLERK");
        pending.add(newUser);

        return true;
    }

    // Kept for compatibility with the original LoginManager method.
    // This method is intended for trusted account-management code.
    public void createUser(String username, String password, String name, String accessLevel) {
        if (username == null || password == null || name == null) {
            return;
        }

        if (username.trim().isEmpty() || password.trim().isEmpty() || name.trim().isEmpty()) {
            return;
        }

        if (usernameExists(username, null)) {
            return;
        }

        User newUser = new User(username.trim(), password, name.trim());
        newUser.setActivated(false);

        if ("MANAGER".equalsIgnoreCase(accessLevel)) {
            newUser.setUserAccessLevel("MANAGER");
        } else {
            newUser.setUserAccessLevel("CLERK");
        }

        pending.add(newUser);
    }

    // Approves a registration and adds it to the active account list.
    public boolean approvePendingUser(User user, String accessLevel) {
        if (user == null) {
            return false;
        }

        if (!pending.contains(user)) {
            return false;
        }

        if ("MANAGER".equalsIgnoreCase(accessLevel)) {
            user.setUserAccessLevel("MANAGER");
        } else {
            user.setUserAccessLevel("CLERK");
        }

        user.setActivated(true);

        if (user.getUserID() == null || user.getUserID().trim().isEmpty()) {
            user.setUserID(generateUserID());
        }

        accountsList.put(user.getUserID(), user);
        pending.remove(user);

        return true;
    }

    public boolean rejectPendingUser(User user) {
        if (user == null) {
            return false;
        }

        return pending.remove(user);
    }

    public boolean deactivateAccount(User user) {
        if (user == null) {
            return false;
        }

        if (!accountsList.containsKey(user.getUserID())) {
            return false;
        }

        user.setActivated(false);

        if (loggedInUser == user) {
            loggedInUser = null;
        }

        return true;
    }

    public boolean activateAccount(User user) {
        if (user == null) {
            return false;
        }

        if (!accountsList.containsKey(user.getUserID())) {
            return false;
        }

        user.setActivated(true);
        return true;
    }

    public ArrayList<User> getPending() {
        return new ArrayList<User>(pending);
    }

    public void setPending(ArrayList<User> pending) {
        this.pending = new ArrayList<User>();

        if (pending == null) {
            return;
        }

        for (User user : pending) {
            if (user != null) {
                this.pending.add(user);
            }
        }
    }

    public ArrayList<User> getAccounts() {
        ArrayList<User> accounts = new ArrayList<User>();

        for (User user : accountsList.values()) {
            if (user != null) {
                accounts.add(user);
            }
        }

        return accounts;
    }

    private boolean usernameExists(String username, User ignoredUser) {
        if (username == null) {
            return false;
        }

        for (User user : accountsList.values()) {
            if (user != null && user != ignoredUser) {
                if (user.getUserName() != null && user.getUserName().equalsIgnoreCase(username.trim())) {
                    return true;
                }
            }
        }

        for (User user : pending) {
            if (user != null && user != ignoredUser) {
                if (user.getUserName() != null
                        && user.getUserName().equalsIgnoreCase(username.trim())) {
                    return true;
                }
            }
        }

        return false;
    }

    private String generateUserID() {
        String newID = "";
        boolean idIsUsed = true;

        while (idIsUsed) {
            newID = String.format("USR%04d", nextUserID);
            nextUserID++;

            idIsUsed = accountsList.containsKey(newID);
        }

        return newID;
    }
}
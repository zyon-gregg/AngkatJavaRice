package application;

import Products.Product;
import Products.ProductDatabase;
import clerkDashboard.ClerkDashboard;
import inventoryManagementSystem.InventorySystem;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import login.LoginManager;
import managerDashboard.ManagerDashboard;
import transaction.SalesTransactionSystem;
import users.Buyer;
import users.BuyerDatabase;
import users.Source;
import users.SourceDatabase;
import users.User;
import SaveLoad.DataManager;

public class Main extends Application {

    // Shared backend instances. These objects are passed to each dashboard.
    private final ProductDatabase products = new ProductDatabase("Angkat Fish Port");
    private final InventorySystem inventory = new InventorySystem();
    private final BuyerDatabase buyers = new BuyerDatabase("Buyers");
    private final SourceDatabase sources = new SourceDatabase("Source");
    private final LoginManager loginManager = new LoginManager();

    private final SalesTransactionSystem sales = new SalesTransactionSystem(products, inventory, buyers, sources);

    private DataManager dataManager;
    private Stage primaryStage;
    private User authenticatedUser;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Angkat - Fish Port Management System");
        
        dataManager = new DataManager(products, inventory, buyers, sources, loginManager, sales);
        
        boolean succesfulLoad;
        
        succesfulLoad = dataManager.loadProducts();
        
        if(!succesfulLoad) {
        	System.out.print("Unsuccesful load of products");
        }
        

        loadSampleData();
        loadDefaultAccounts();
        showLogin();

        primaryStage.show();
    }

    // -----------------------------------------------------------------
    // LOGIN AND AUTHENTICATION
    // -----------------------------------------------------------------

    private void showLogin() {
        authenticatedUser = null;
        sales.setCurrentUser(null);
        loginManager.logout();

        LoginView loginView = new LoginView(
                this::authenticate,
                (username, role) -> {
                    if (authenticatedUser == null) {
                        showLogin();
                        return;
                    }

                    sales.setCurrentUser(authenticatedUser);

                    if ("MANAGER".equalsIgnoreCase(role)) {
                        showManagerDashboard();
                    } else if ("CLERK".equalsIgnoreCase(role)) {
                        showClerkDashboard();
                    } else {
                        showLogin();
                    }
                },
                this::showRegistrationDialog
        );

        primaryStage.setScene(new Scene(loginView, 1150, 600));
        primaryStage.setTitle("Angkat - Fish Port Management System");
    }
    
    private void handleLogout() { //the acutal saving

        boolean saved = dataManager.saveProducts();

        if (saved) {
            System.out.println("Products saved successfully.");
            showLogin();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(primaryStage);
            alert.setTitle("Saving Error");
            alert.setHeaderText("Unable to save products.");
            alert.setContentText("Please try logging out again.");
            alert.showAndWait();
        }
        
        
    }

    // Uses the existing LoginManager to authenticate the user.
    private String authenticate(String username, String password) {
        boolean loginSuccessful = loginManager.login(username, password);

        if (!loginSuccessful) {
            authenticatedUser = null;
            return null;
        }

        authenticatedUser = loginManager.getLoggedInUser();

        if (authenticatedUser == null) {
            return null;
        }

        return authenticatedUser.getUserAccessLevel();
    }

    // Creates the first Manager and a sample Clerk account for testing.
    private void loadDefaultAccounts() {
        User manager = new User("manager", "manager123", "Test Manager");
        manager.setUserID("MGR001");
        manager.setUserAccessLevel("MANAGER");
        manager.setActivated(true);
        loginManager.addAccount(manager);

        User clerk = new User("clerk", "clerk123", "Test Clerk");
        clerk.setUserID("CLK001");
        clerk.setUserAccessLevel("CLERK");
        clerk.setActivated(true);
        loginManager.addAccount(clerk);
    }

    // Opens a simple form for a new user to request a Clerk account.
    private void showRegistrationDialog() {
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.initOwner(primaryStage);
        dialog.setTitle("Request an Account");
        dialog.setHeaderText("Submit your details for Manager approval.");

        TextField nameField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();

        nameField.setPromptText("Full name");
        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        confirmPasswordField.setPromptText("Confirm password");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Full name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Username:"), 0, 1);
        form.add(usernameField, 1, 1);
        form.add(new Label("Password:"), 0, 2);
        form.add(passwordField, 1, 2);
        form.add(new Label("Confirm password:"), 0, 3);
        form.add(confirmPasswordField, 1, 3);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL);

        java.util.Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (name.isEmpty() || username.isEmpty() || password.trim().isEmpty()) {
                showMessage("Registration", "Please complete all fields.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showMessage("Registration", "The passwords do not match.");
                return;
            }

            boolean requestCreated = loginManager.registerUser(
                    username, password, name);

            if (requestCreated) {
                showMessage("Registration Submitted",
                        "Your account request was submitted. A Manager must approve it before you can log in.");
            } else {
                showMessage("Registration", 
                        "That username is already used or the registration details are incomplete.");
            }
        }
    }

    private void showMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // -----------------------------------------------------------------
    // DASHBOARDS
    // -----------------------------------------------------------------

    private void showManagerDashboard() {
        ManagerDashboard managerDashboard = new ManagerDashboard(
                primaryStage,
                authenticatedUser,
                products,
                inventory,
                sales,
                buyers.returnBuyerList(),
                loginManager,
                this::showLogin
        );

        managerDashboard.show();
    }

    private void showClerkDashboard() {
        ClerkDashboard clerkDashboard = new ClerkDashboard(
                primaryStage,
                authenticatedUser.getName(),
                authenticatedUser.getUserID(),
                authenticatedUser.getUserName(),
                products,
                inventory,
                sales,
                buyers.returnBuyerList(),
                this::showLogin
        );

        clerkDashboard.show();
    }

    // -----------------------------------------------------------------
    // DATA INITIALIZATION
    // -----------------------------------------------------------------

    /** Creates an inventory record for each product that does not have one. */
    private void syncInventoryRecords() {
        for (Product product : products.getAllProducts()) {
            if (inventory.getProductInventoryByCode(product.getProductCode()) == null) {
                inventory.createInventoryRecord(product);
            }
        }
    }

    private void loadSampleData() {
        Product bangus = new Product(
                products, "Bangus", "Fresh milkfish, whole",
                "Dagupan fish pens", 120.00, 150.00, 180.00);

        Product tilapia = new Product(
                products, "Tilapia", "Fresh tilapia, whole",
                "Taal Lake fish cages", 80.00, 100.00, 120.00);

        Product tuna = new Product(
                products, "Tuna", "Yellowfin tuna, sliced",
                "General Santos", 230.00, 280.00, 320.00);

        Product galunggong = new Product(
                products, "Galunggong", "Round scad",
                "Navotas Fish Port", 110.00, 140.00, 160.00);

        Product pusit = new Product(
                products, "Pusit", "Fresh squid",
                "Palawan", 200.00, 240.00, 280.00);

        for (Product product : new Product[] {
                bangus, tilapia, tuna, galunggong, pusit
        }) {
            products.addProduct(product);
        }

        Source appleFarm = new Source("AppleF", "TheFarm", "AppleFarms");

        for (Product product : new Product[] {
                bangus, tilapia, tuna, galunggong, pusit
        }) {
            appleFarm.addProductToSource(product, 100000.00);
        }

        sources.addSourceToDatabase(appleFarm);

        buyers.addBuyerToDatabase(
                new Buyer("Apple", "Apple123", "Apples", 20000));
        buyers.addBuyerToDatabase(
                new Buyer("Apple2", "Apple1234", "Apples", 120000));
        buyers.addBuyerToDatabase(
                new Buyer("Apple3", "Apple12345", "Apples", 22130000));

        syncInventoryRecords();

        inventory.restockProduct(
                bangus.getProductCode(), 500, "System", "Opening stock");
        inventory.restockProduct(
                tilapia.getProductCode(), 250, "System", "Opening stock");
        inventory.restockProduct(
                galunggong.getProductCode(), 800, "System", "Opening stock");
        inventory.restockProduct(
                pusit.getProductCode(), 120, "System", "Opening stock");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
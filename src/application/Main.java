package application;

import Products.Product;
import Products.ProductDatabase;
import clerkDashboard.ClerkDashboard;
import inventoryManagementSystem.InventorySystem;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import managerDashboard.ManagerDashboard;
import transaction.SalesTransactionSystem;
import users.Buyer;
import users.BuyerDatabase;
import users.Source;
import users.SourceDatabase;
import users.User;

public class Main extends Application {

    // Shared backend instances. Create each system once and pass these same
    // objects to the dashboards and module windows.
    private final ProductDatabase products = new ProductDatabase("Angkat Fish Port");

    private final InventorySystem inventory = new InventorySystem();
    private final BuyerDatabase buyers = new BuyerDatabase("Buyers");
    private final SourceDatabase sources = new SourceDatabase("Source");

    private final SalesTransactionSystem sales =
            new SalesTransactionSystem(products, inventory, buyers, sources);

    private Stage primaryStage;
    private User authenticatedUser;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Angkat - Fish Port Management System");

        loadSampleData();
        showLogin();

        primaryStage.show();
    }

    // --------------------------------------------------
    // LOGIN AND AUTHENTICATION
    // --------------------------------------------------

    private void showLogin() {
        // Clear the previous session before displaying the login screen.
        authenticatedUser = null;
        sales.setCurrentUser(null);

        LoginView loginView = new LoginView(
                this::authenticate,
                (username, role) -> {
                    authenticatedUser = createTestUser(username, role);
                    sales.setCurrentUser(authenticatedUser);

                    if ("MANAGER".equals(role)) {
                        showManagerDashboard();
                    } else if ("CLERK".equals(role)) {
                        showClerkDashboard();
                    }
                }
        );

        // Dashboards currently create and assign their own Scene to this
        // Stage, so login must do the same instead of using SceneManager's
        // separate, no-longer-displayed Scene.
        primaryStage.setScene(new Scene(loginView, 1150, 600));
        primaryStage.setTitle("Angkat - Fish Port Management System");
    }

    /**
     * Temporary test authentication.
     * Returns MANAGER, CLERK, or null when credentials are invalid.
     */
    private String authenticate(String username, String password) {
        if ("manager".equals(username) && "manager123".equals(password)) {
            return "MANAGER";
        }

        if ("clerk".equals(username) && "clerk123".equals(password)) {
            return "CLERK";
        }

        return null;
    }

    /**
     * Creates a temporary User object for the two hard-coded test accounts.
     * Replace this method when LoginManager returns the real authenticated User.
     */
    private User createTestUser(String username, String role) {
        String displayName;
        String userID;

        if ("MANAGER".equals(role)) {
            displayName = "Test Manager";
            userID = "MGR001";
        } else {
            displayName = "Test Clerk";
            userID = "CLK001";
        }

        User user = new User(username, "", displayName);
        user.setUserID(userID);
        user.setUserAccessLevel(role);
        user.setActivated(true);

        return user;
    }

    // --------------------------------------------------
    // DASHBOARDS
    // --------------------------------------------------

    private void showManagerDashboard() {
        ManagerDashboard managerDashboard = new ManagerDashboard(
                primaryStage,
                authenticatedUser,
                products,
                inventory,
                sales,
                buyers.returnBuyerList(),
                this::showLogin
        );

        managerDashboard.show();
    }

    private void showClerkDashboard() {
        ClerkDashboard clerkDashboard = new ClerkDashboard(
                primaryStage,
                authenticatedUser.getName(),
                authenticatedUser.getUserID(),
                products,
                inventory,
                sales,
                buyers.returnBuyerList(),
                this::showLogin
        );

        clerkDashboard.show();
    }

    // --------------------------------------------------
    // DATA INITIALIZATION
    // --------------------------------------------------

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

        // Register products.
        for (Product product : new Product[] {
                bangus, tilapia, tuna, galunggong, pusit
        }) {
            products.addProduct(product);
        }

        // Register source and its available quantities.
        Source appleFarm = new Source("AppleF", "TheFarm", "AppleFarms");

        for (Product product : new Product[] {
                bangus, tilapia, tuna, galunggong, pusit
        }) {
            appleFarm.addProductToSource(product, 100000.00);
        }

        sources.addSourceToDatabase(appleFarm);

        // Register sample buyers.
        buyers.addBuyerToDatabase(
                new Buyer("Apple", "Apple123", "Apples", 20000));
        buyers.addBuyerToDatabase(
                new Buyer("Apple2", "Apple1234", "Apples", 120000));
        buyers.addBuyerToDatabase(
                new Buyer("Apple3", "Apple12345", "Apples", 22130000));

        // Create inventory records, then populate opening stock.
        syncInventoryRecords();

        inventory.restockProduct(
                bangus.getProductCode(), 500, "System", "Opening stock");
        inventory.restockProduct(
                tilapia.getProductCode(), 250, "System", "Opening stock");
        inventory.restockProduct(
                galunggong.getProductCode(), 800, "System", "Opening stock");
        inventory.restockProduct(
                pusit.getProductCode(), 120, "System", "Opening stock");

        // Tuna intentionally remains at 0 kg for the out-of-stock test case.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package application;

import Products.Product;
import Products.ProductDatabase;
import Products.ProductManagementView;
import inventoryManagementSystem.InventoryGUI;
import inventoryManagementSystem.InventorySystem;
import javafx.application.Application;
import javafx.stage.Stage;


import transaction.SalesTransactionSystem;
import transaction.TransactionView;
import users.Buyer;
import java.util.ArrayList;
import java.util.List;


//Latestaa
public class Main extends Application {

	private final ProductDatabase products = new ProductDatabase("Angkat Fish Port");
	private final InventorySystem inventory = new InventorySystem();
	private SceneManager sceneManager;

	// Set after a successful login
	private String currentUser = "";
	private boolean isManager = false;

	@Override
	public void start(Stage stage) {
		loadSampleData();

		sceneManager = new SceneManager(stage, 1150, 600);
		showLogin();

		stage.setTitle("Angkat - Fish Port Management System");
		stage.show();
	}

	// ---------- Screens ----------

	private void showLogin() {
		sceneManager.setRoot(new LoginView(this::authenticate, (user, role) -> {
			currentUser = user;
			isManager = role.equals("MANAGER");
			showMenu();
		}));
	}

	private void showMenu() {
		sceneManager.setRoot(new MainMenuView(this::showProducts, this::showInventory, this::showTransactions));
	}

	private void showProducts() {
		sceneManager.showWithBackButton(new ProductManagementView(products, isManager), this::showMenu);
	}

	private void showInventory() {
		 InventoryGUI inventoryGUI =new InventoryGUI(inventory);
         inventoryGUI.setStageComponents();
		
	}
	private void showTransactions() {
		syncInventoryRecords();
		sceneManager.showWithBackButton(new TransactionView(sales, products, getBuyers()), this::showMenu);
	}
	
	/** TODO: return the real buyers from BuyerDatabase. Empty for now, so the buyer dropdown is empty. */
	private List<Buyer> getBuyers() {
		return new ArrayList<>();
	}

	// ---------- Login ----------

	/**
	 * TEMPORARY login check, so the login screen works without the users package.
	 * Replace the body with the real user database once that part is ready.
	 * Returns "MANAGER" or "CLERK", or null if the login is wrong.
	 */
	private String authenticate(String username, String password) {
		if (username.equals("manager") && password.equals("manager123")) return "MANAGER";
		if (username.equals("clerk") && password.equals("clerk123")) return "CLERK";
		return null;
	}

	// ---------- Data ----------

	/** Gives every product that has no stock record yet one (starting at 0 kg). */
	private void syncInventoryRecords() {
		for (Product p : products.getAllProducts()) {
			if (inventory.getProductInventoryByCode(p.getProductCode()) == null) {
				inventory.createInventoryRecord(p);
			}
		}
	}

	private void loadSampleData() {
		// Arguments: name, description, source, cost/kg, raw price, estimated market price
		Product bangus = new Product(products,"Bangus", "Fresh milkfish, whole", "Dagupan fish pens", 120.00, 150.00, 180.00);
		Product tilapia = new Product(products,"Tilapia", "Fresh tilapia, whole", "Taal Lake fish cages", 80.00, 100.00, 120.00);
		Product tuna = new Product(products,"Tuna", "Yellowfin tuna, sliced", "General Santos", 230.00, 280.00, 320.00);
		Product galunggong = new Product(products,"Galunggong", "Round scad", "Navotas Fish Port", 110.00, 140.00, 160.00);
		Product pusit = new Product(products,"Pusit", "Fresh squid", "Palawan", 200.00, 240.00, 280.00);

		for (Product p : new Product[] { bangus, tilapia, tuna, galunggong, pusit }) {
			products.addProduct(p);
		}
		syncInventoryRecords();

		// Sample stock: Tuna stays at 0 (out of stock), Tilapia and Pusit are low
		inventory.restockProduct(bangus.getProductCode(), 500, "manager", "Opening stock");
		inventory.restockProduct(tilapia.getProductCode(), 250, "manager", "Opening stock");
		inventory.restockProduct(galunggong.getProductCode(), 800, "manager", "Opening stock");
		inventory.restockProduct(pusit.getProductCode(), 120, "manager", "Opening stock");
	}
	
	private final SalesTransactionSystem sales = new SalesTransactionSystem(products, inventory);


	public static void main(String[] args) {
		launch(args);
	}
}
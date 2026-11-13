package application;

import Products.Product;
import Products.ProductDatabase;
import Products.ProductManagementView;
import inventoryManagementSystem.InventoryGUI;
import inventoryManagementSystem.InventorySystem;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	private final ProductDatabase products = new ProductDatabase("Angkat Fish Port");
	private final InventorySystem inventory = new InventorySystem();
	private SceneManager sceneManager;

	@Override
	public void start(Stage stage) {
		loadSampleData();

		sceneManager = new SceneManager(stage, 1150, 600);
		showMenu();

		stage.setTitle("Angkat - Fish Port Management System");
		stage.show();
	}

	private void showMenu() {
		sceneManager.setRoot(new MainMenuView(this::showProducts, this::showInventory));
	}

	private void showProducts() {
		sceneManager.showWithBackButton(new ProductManagementView(products, true), this::showMenu);
	}

	private void showInventory() {
		syncInventoryRecords();
		sceneManager.showWithBackButton(new InventoryGUI(inventory, "manager", true), this::showMenu);
	}

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
		Product bangus = new Product("Bangus", "Fresh milkfish, whole", "Dagupan fish pens", 120.00, 150.00, 180.00);
		Product tilapia = new Product("Tilapia", "Fresh tilapia, whole", "Taal Lake fish cages", 80.00, 100.00, 120.00);
		Product tuna = new Product("Tuna", "Yellowfin tuna, sliced", "General Santos", 230.00, 280.00, 320.00);
		Product galunggong = new Product("Galunggong", "Round scad", "Navotas Fish Port", 110.00, 140.00, 160.00);
		Product pusit = new Product("Pusit", "Fresh squid", "Palawan", 200.00, 240.00, 280.00);

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

	public static void main(String[] args) {
		launch(args);
	}
}
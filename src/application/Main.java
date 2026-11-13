package application;
import Products.Product;
import Products.ProductDatabase;
import Products.ProductManagementView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import users.Source;
import users.SourceDatabase;

import inventoryManagementSystem.InventoryGUI;
import inventoryManagementSystem.InventorySystem;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Demo entry point for testing the Product Management screen alone.
 */
public class Main extends Application {

	@Override
	public void start(Stage stage) {
		ProductDatabase database = new ProductDatabase("Angkat Fish Port");
		Source sampleSource = new Source("Source","SourcePassword","Fisherman");
		SourceDatabase sampleSourceDatabase = new SourceDatabase("SourceDatabase");
		SourceDatabase sampleSourceDatabase2 = new SourceDatabase("SourceDatabase");//used for testing empty cases
		sampleSourceDatabase.addSourceToDatabase(sampleSource);
		
		Product bangus = new Product(database, "Bangus", "Fresh milkfish, whole", 120.00, 150.00, 180.00,sampleSource);
		Product tilapia = new Product(database, "Tilapia", "Fresh tilapia, whole", 80.00, 100.00, 120.00,sampleSource);
		Product tuna = new Product(database, "Tuna", "Yellowfin tuna, sliced", 230.00, 280.00, 320.00,sampleSource);
		Product galunggong = new Product(database, "Galunggong", "Round scad", 110.00, 140.00, 160.00,sampleSource);
		Product pusit = new Product(database, "Pusit", "Fresh squid", 200.00, 240.00, 280.00,sampleSource);
		
		sampleSource.addProductToSource(bangus, 200.00);//Set Quantity Temporarily, Will Add method to connect Inventory stock to this
		sampleSource.addProductToSource(tilapia, 200.00);
		sampleSource.addProductToSource(tuna, 200.00);
		sampleSource.addProductToSource(galunggong, 200.00);
		sampleSource.addProductToSource(pusit, 200.00);
		
		InventorySystem inventorySystem = new InventorySystem();
		
		initializeProductInventory(inventorySystem, bangus, 450.00);
		initializeProductInventory(inventorySystem, tilapia, 200.00);
		initializeProductInventory(inventorySystem, tuna, 600.00);
		initializeProductInventory(inventorySystem, galunggong, 700.00);
		initializeProductInventory(inventorySystem, pusit, 150.00);
		
		database.addAllSourcesToDatabase(sampleSourceDatabase);

        Button productButton = new Button("Product Management");

        Button inventoryButton = new Button("Inventory Management");

        // Product Management Window
        productButton.setOnAction(e -> {
        	ProductManagementView view = new ProductManagementView(database, true);

            Stage productStage = new Stage();

            productStage.setTitle("Angkat - Product Management");

            productStage.setScene(new Scene(view, 1000, 560));

            productStage.show();
        });

        // Inventory Management Window
        inventoryButton.setOnAction(e -> {

            InventoryGUI inventoryGUI =new InventoryGUI(inventorySystem);

            inventoryGUI.setStageComponents();
        });
        
        VBox root = new VBox(15);

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        root.getChildren().addAll(productButton, inventoryButton);

        stage.setTitle("Angkat Test");
        stage.setScene(new Scene(root, 400, 300));
        stage.show();

	}
	
	private void initializeProductInventory(InventorySystem inventorySystem,Product product,double initialStock) {

        // Create inventory record for the product
        inventorySystem.createInventoryRecord(product);

        // Add its initial stock
        inventorySystem.restockProduct(product.getProductCode(),initialStock,"System","Initial demo stock");
    }

	public static void main(String[] args) {
		launch(args);
	}
}
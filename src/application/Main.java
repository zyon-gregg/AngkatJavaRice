package application;
import Products.Product;
import Products.ProductDatabase;
import Products.ProductManagementView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import users.Source;
import users.SourceDatabase;

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
		
		sampleSource.addProductToSource(new Product(database, "Bangus", "Fresh milkfish, whole", 120.00, 150.00, 180.00,sampleSource), 200.00);//Set Quantity Temporarily, Will Add method to connect Inventory stock to this
		sampleSource.addProductToSource(new Product(database, "Tilapia", "Fresh tilapia, whole", 80.00, 100.00, 120.00,sampleSource), 200.00);
		sampleSource.addProductToSource(new Product(database, "Tuna", "Yellowfin tuna, sliced", 230.00, 280.00, 320.00,sampleSource), 200.00);
		sampleSource.addProductToSource(new Product(database, "Galunggong", "Round scad", 110.00, 140.00, 160.00,sampleSource), 200.00);
		sampleSource.addProductToSource(new Product(database, "Pusit", "Fresh squid", 200.00, 240.00, 280.00,sampleSource), 200.00);
		
		database.addAllSourcesToDatabase(sampleSourceDatabase);
		
		// true = Manager view (can edit), false = Clerk view (read-only)
		ProductManagementView view = new ProductManagementView(database, true);

		stage.setTitle("Angkat - Product Management");
		stage.setScene(new Scene(view, 1000, 560));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
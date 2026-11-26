package Products;

//JavaFX uses Observable lists. When the underlying data changes, 
//the UI updates automatically. No need to manually redraw the table.
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList; // Used for the live search bar
import javafx.collections.transformation.SortedList; // Used for clicking column headers
//THE LAYOUT AND GEOMETRY
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos; // Used to align labels to the top of tall text areas
import javafx.scene.Node;
import javafx.scene.layout.*;

//UI CONTROLS
import javafx.scene.control.*; // Buttons, Labels, TextFields, Tables, Alerts

// JAVA UTILITIES & BACKEND MODULES
import java.util.Optional;
import java.util.function.Function;

import inventoryManagementSystem.InventoryOfProduct;
import inventoryManagementSystem.InventorySystem;

/**
 * Reusable Product Management screen.
 * - Manager: canEdit = true  -> table + add/update/delete form
 * - Clerk:   canEdit = false -> table + search only (read-only)
 *
 * The view never touches the HashMap directly. It calls ProductDatabase methods,
 * then reloads the table from getAllProducts().
 */
public class ProductManagementView extends BorderPane {

	private final ProductDatabase database;
	private final InventorySystem inventorySystem;
	
	
	// THE LISTS OF DATA
    // 1. products: The raw master list straight from the database.
    // 2. filtered: A wrapper that hides rows that don't match the search bar.
    // 3. table: Displays the sorted, filtered list.
	private final ObservableList<Product> products = FXCollections.observableArrayList();
	private final FilteredList<Product> filtered = new FilteredList<>(products, p -> true);
	private final TableView<Product> table = new TableView<>();
	
	// input fields for the form
	private final TextField nameField = new TextField();
	private final TextField sourceField = new TextField();
	private final TextArea descField = new TextArea();
	private final TextField costField = new TextField();
	private final TextField rawPriceField = new TextField();
	private final TextField marketPriceField = new TextField();

	public ProductManagementView(ProductDatabase database, InventorySystem inventorySystem, boolean canEdit) {
		this.database = database;
		this.inventorySystem = inventorySystem;
		setPadding(new Insets(16));
		//assembling the main layout
		setTop(buildHeader());
		setCenter(buildTable());
		//if the user has permission from the manager, we can show the edit form on the right
		if (canEdit) {
			setRight(buildForm());
		}
		  // Ensure the database and inventory system have matching records before showing UI
		syncInventoryRecords();
		reload();
	}

	// LAYOUT BUILDERS

	private Node buildHeader() {
		Label title = new Label("Product Management"); //title of the table 

		// Live Search Bar
		TextField search = new TextField();
		search.setPromptText("Search by code, name or source...");
		search.setPrefWidth(280);
		
		 // As the user types, we update the "filtered" list's predicate.
        // This instantly hides rows in the table without deleting data from the database.
		//this searches what the user types in the search bar
		search.textProperty().addListener((obs, old, text) -> {
			String searchText = "";
			if (text != null) {
				searchText = text.trim().toLowerCase();
			}
			final String q = searchText;
			filtered.setPredicate(p -> q.isEmpty()
					|| p.getProductCode().toLowerCase().contains(q)
					|| p.getProductName().toLowerCase().contains(q)
					|| p.getProductSource().toLowerCase().contains(q));
		});

		//invisible node for spacing
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Pushes the search bar to the right

		//this was used for proper spacing also
		HBox header = new HBox(10, title, spacer, search);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(0, 0, 12, 0));
		return header;
	}

	private Node buildTable() {
		 // Define the columns using our helper method
		//These are the visible names in the column row of the PMS
		table.getColumns().add(column("Code", Product::getProductCode, 80));
		table.getColumns().add(column("Name", Product::getProductName, 130));
		table.getColumns().add(column("Description", Product::getProductDescription, 190));
		table.getColumns().add(column("Source", Product::getProductSource, 150));
		table.getColumns().add(column("Cost / kg", p -> money(p.getProductCostInKG()), 90));
		table.getColumns().add(column("Raw price", p -> money(p.getProductRawPrice()), 90));
		table.getColumns().add(column("Est. market price", p -> money(p.getProductEstimatedMarketPrice()), 120));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		table.setPlaceholder(new Label("No products yet."));

		 // THE SORTING PIPELINE:
		// Sorting by clicking column headers, on top of the search filter
        // We wrap the filtered list in a SortedList. By binding the SortedList's comparator
        // to the TableView's comparator, clicking the column headers automatically sorts the data.
		SortedList<Product> sorted = new SortedList<>(filtered);
		sorted.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sorted);

		 
        // When a Manager clicks a row, we populate the form fields on the right with that product's data.
		table.getSelectionModel().selectedItemProperty().addListener((obs, old, p) -> {
			if (p != null) {
				nameField.setText(p.getProductName());
				sourceField.setText(p.getProductSource());
				descField.setText(p.getProductDescription());
				costField.setText(String.valueOf(p.getProductCostInKG()));
				rawPriceField.setText(String.valueOf(p.getProductRawPrice()));
				marketPriceField.setText(String.valueOf(p.getProductEstimatedMarketPrice()));
			}
		});
		return table;
	}

	//this buildform is to create several
	private Node buildForm() {
		descField.setPrefRowCount(3); //set max characters
		descField.setWrapText(true); //for text wrapping
		sourceField.setPromptText("e.g. Navotas Fish Port"); //display this when the given string in the field is empty and not focused
		costField.setPromptText("e.g. 120.00");//displays this price when that space is empty 
		rawPriceField.setPromptText("e.g. 150.00");//displays this price when that space is empty 
		marketPriceField.setPromptText("e.g. 180.00"); //displays this price when that space is empty 
		
		//creating an GridPane object 
		GridPane grid = new GridPane();
		grid.setHgap(10); //horizontal gaps
		grid.setVgap(10); //vertical gaps

		// labels: the label column is never shrunk below its text,
		// and the field column takes all the remaining width.
		ColumnConstraints labelColumn = new ColumnConstraints();
		labelColumn.setMinWidth(Region.USE_PREF_SIZE);
		ColumnConstraints fieldColumn = new ColumnConstraints();
		fieldColumn.setHgrow(Priority.ALWAYS);
		fieldColumn.setFillWidth(true);
		grid.getColumnConstraints().addAll(labelColumn, fieldColumn);
		
		
		addRow(grid, 0, "Name", nameField);
		addRow(grid, 1, "Source", sourceField);
		addRow(grid, 2, "Cost / kg", costField);
		addRow(grid, 3, "Raw price", rawPriceField);
		addRow(grid, 4, "Est. market", marketPriceField);
		addRow(grid, 5, "Description", descField);

		Button addBtn = new Button("Add"); //add button
		Button updateBtn = new Button("Update");//update button
		Button deleteBtn = new Button("Delete"); // delete button
		Button clearBtn = new Button("Clear"); //for clearing inputs
		addBtn.setOnAction(e -> handleAdd()); //catches the click
		updateBtn.setOnAction(e -> handleUpdate());  //catches when the update button is clicked
		deleteBtn.setOnAction(e -> handleDelete()); //catches when the delete button is clicked
		clearBtn.setOnAction(e -> clearForm()); //catches the action when clear button is clicked

		Label formTitle = new Label("Product details");
		formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		//creating a vbox for the form found. this will be used later by the 
		VBox form = new VBox(14, formTitle, grid, new HBox(8, addBtn, updateBtn, deleteBtn, clearBtn));
		form.setPadding(new Insets(0, 0, 0, 16));
		form.setPrefWidth(360);
		form.setMinWidth(Region.USE_PREF_SIZE);
		return form;
	}

	/** Adds one "label + field" row. The label sits at the top so it lines up with tall fields. */
	private void addRow(GridPane grid, int row, String labelText, Node field) {
		Label label = new Label(labelText);
		GridPane.setValignment(label, VPos.TOP);
		grid.add(label, 0, row);
		grid.add(field, 1, row);
	}

	// ---------- Event handlers ----------
	
	
	//this reads if the costfield is empty or not, then save entered cost to variable name "cost"
	private void handleAdd() {
		Double cost = readNumber(costField, "Cost per kg");
		if (cost == null) {
			return;
		}
		//same goes here reading the rawPriceField
		Double rawPrice = readNumber(rawPriceField, "Raw price");
		if (rawPrice == null) {
			return;
		}
		//same here
		Double marketPrice = readNumber(marketPriceField, "Estimated market price");
		if (marketPrice == null) {
			return;
		}
		//now the new product has a name,source, cost/kg, etc.
		Product newProduct = new Product(database, nameField.getText(),
				descField.getText(), sourceField.getText(), cost, rawPrice, marketPrice);

		//checks if the product is successfully added in the catalog
		boolean productAdded = database.addProduct(newProduct);
		if (!productAdded) {
			showError("The product could not be added to the catalog.");
			return;
		}

		// Every new catalog product must also receive an empty inventory record.
		boolean inventoryCreated = inventorySystem.createInventoryRecord(newProduct);
		if (!inventoryCreated) {
			database.removeProduct(newProduct);
			showError("The product could not be added because its inventory record could not be created.");// if there is no inventory created we can't add the the product
			return;
		}

		reload(); //restarts
		clearForm(); //then clear if the product is successfully added
	}

	//this method is for updating the existing product in the catalog
	private void handleUpdate() {
		//we have a selected object after selecting/clicking an item from the catalog
		Product selected = table.getSelectionModel().getSelectedItem();
		//if the user clicked update without selecting a produc this will be an error
		if (selected == null) {
			showError("Select a product from the table first.");
			return;
		}

		//if successful, it will asked the user to update the cost, prices, etc.
		Double cost = readNumber(costField, "Cost per kg");
		if (cost == null) {
			return;
		}

		Double rawPrice = readNumber(rawPriceField, "Raw price");
		if (rawPrice == null) {
			return;
		}

		Double marketPrice = readNumber(marketPriceField, "Estimated market price");
		if (marketPrice == null) {
			return;
		}
		  // Update the database. Because the database edits the *same object* 
        // that the Inventory System is holding, the inventory system automatically
        // reflects the new name/price without needing a separate update call.
		boolean productUpdated = database.editProduct(selected, nameField.getText(),
				descField.getText(), sourceField.getText(), cost, rawPrice, marketPrice);

		if (!productUpdated) {
			showError("The selected product could not be updated.");
			return;
		}

		// InventoryOfProduct holds the same Product object, so its details
		// update automatically. The stock quantity is not changed here.
		reload();
		clearForm();
	}
	//same idea with the update, this is for deleting the product
	private void handleDelete() {
		Product selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showError("Select a product from the table first.");
			return;
		}
        // Safety Check: Ask for confirmation before deleting
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				"Delete \"" + selected.getProductName() + "\"?", ButtonType.YES, ButtonType.NO);
		confirm.setHeaderText(null);
		Optional<ButtonType> answer = confirm.showAndWait();

		if (!answer.isPresent()) {
			return;
		}

		if (answer.get() != ButtonType.YES) {
			return;
		}

		String productCode = selected.getProductCode();

        // CROSS-MODULE SAFETY: We cannot delete a product from the catalog 
        // if it still has physical stock in the inventory system.
		if (!inventorySystem.canRemoveInventoryRecord(productCode)) {
			showError("This product still has stock. Reduce its inventory to 0 kg before deleting it.");
			return;
		}
		 // Remove from Catalog
		boolean productRemoved = database.removeProduct(selected);
		if (!productRemoved) {
			showError("The selected product could not be removed from the catalog.");
			return;
		}

		inventorySystem.removeInventoryRecord(productCode);

		reload();
		clearForm();
	}
	//UTILITIES & HELPERS

	 /** 
     * Ensures data integrity between the two modules. If a product exists in the 
     * catalog but has no inventory record (e.g., from an old save file), we create one.
     */
	private void syncInventoryRecords() {
		for (Product product : database.getAllProducts()) {
			InventoryOfProduct inventoryRecord =
					inventorySystem.getProductInventoryByCode(product.getProductCode());

			if (inventoryRecord == null) {
				inventorySystem.createInventoryRecord(product);
			}
		}
	}

	/** Pulls the latest products from the database into the table. */
	public void reload() {
		products.setAll(database.getAllProducts());
	}
    /** Safely parses text into a Double, showing a user-friendly error if it fails. */
	private Double readNumber(TextField field, String label) {
		try {
			double value = Double.parseDouble(field.getText().trim());

			if (Double.isNaN(value) || Double.isInfinite(value)) {
				showError(label + " must be a valid number.");
				return null;
			}

			return value;
		} catch (NumberFormatException ex) {
			showError(label + " must be a valid number.");
			return null;
		}
	}
	//this is just for formating the money in PHP
	private String money(double value) {
		return String.format("PHP %.2f", value);
	}

	//for clearing the parts in the form
	private void clearForm() {
		nameField.clear();
		sourceField.clear();
		descField.clear();
		costField.clear();
		rawPriceField.clear();
		marketPriceField.clear();
		table.getSelectionModel().clearSelection(); // Deselect row so it doesn't re-populate
	}

	//error messages.
	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR, message);
		alert.setHeaderText("Invalid input");
		alert.showAndWait();
	}

	/** Small helper so every column is built the same way. */

    /** 
     * Factory Method for TableColumns. 
     * Uses a Function (getter) to extract the specific String data from the Product object.
     */
	private TableColumn<Product, String> column(String title, Function<Product, String> getter, double width) {
		TableColumn<Product, String> col = new TableColumn<>(title);
		col.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
		col.setPrefWidth(width);
		return col;
	}
}
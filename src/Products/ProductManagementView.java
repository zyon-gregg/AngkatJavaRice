package Products;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Function;

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
	private final ObservableList<Product> products = FXCollections.observableArrayList();
	private final FilteredList<Product> filtered = new FilteredList<>(products, p -> true);
	private final TableView<Product> table = new TableView<>();

	private final TextField nameField = new TextField();
	private final TextField sourceField = new TextField();
	private final TextArea descField = new TextArea();
	private final TextField costField = new TextField();
	private final TextField rawPriceField = new TextField();
	private final TextField marketPriceField = new TextField();

	public ProductManagementView(ProductDatabase database, boolean canEdit) {
		this.database = database;
		setPadding(new Insets(16));

		setTop(buildHeader());
		setCenter(buildTable());
		if (canEdit) {
			setRight(buildForm());
		}
		reload();
	}

	// ---------- Layout builders ----------

	private Node buildHeader() {
		Label title = new Label("Product Management");
		title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

		TextField search = new TextField();
		search.setPromptText("Search by code, name or source...");
		search.setPrefWidth(280);
		search.textProperty().addListener((obs, old, text) -> {
			String q = text == null ? "" : text.trim().toLowerCase();
			filtered.setPredicate(p -> q.isEmpty()
					|| p.getProductCode().toLowerCase().contains(q)
					|| p.getProductName().toLowerCase().contains(q)
					|| p.getProductSource().toLowerCase().contains(q));
		});

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox header = new HBox(10, title, spacer, search);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(0, 0, 12, 0));
		return header;
	}

	private Node buildTable() {
		table.getColumns().add(column("Code", Product::getProductCode, 80));
		table.getColumns().add(column("Name", Product::getProductName, 130));
		table.getColumns().add(column("Description", Product::getProductDescription, 190));
		table.getColumns().add(column("Source", Product::getProductSource, 150));
		table.getColumns().add(column("Cost / kg", p -> money(p.getProductCostInKG()), 90));
		table.getColumns().add(column("Raw price", p -> money(p.getProductRawPrice()), 90));
		table.getColumns().add(column("Est. market price", p -> money(p.getProductEstimatedMarketPrice()), 120));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		table.setPlaceholder(new Label("No products yet."));

		// Sorting by clicking column headers, on top of the search filter
		SortedList<Product> sorted = new SortedList<>(filtered);
		sorted.comparatorProperty().bind(table.comparatorProperty());
		table.setItems(sorted);

		// Selecting a row fills the form so it can be edited
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

	private Node buildForm() {
		descField.setPrefRowCount(3);
		descField.setWrapText(true);
		sourceField.setPromptText("e.g. Navotas Fish Port");
		costField.setPromptText("e.g. 120.00");
		rawPriceField.setPromptText("e.g. 150.00");
		marketPriceField.setPromptText("e.g. 180.00");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);

		// Fix for the "..." labels: the label column is never shrunk below its text,
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

		Button addBtn = new Button("Add");
		Button updateBtn = new Button("Update");
		Button deleteBtn = new Button("Delete");
		Button clearBtn = new Button("Clear");
		addBtn.setOnAction(e -> handleAdd());
		updateBtn.setOnAction(e -> handleUpdate());
		deleteBtn.setOnAction(e -> handleDelete());
		clearBtn.setOnAction(e -> clearForm());

		Label formTitle = new Label("Product details");
		formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

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

	private void handleAdd() {
		try {
			database.addProduct(new Product(nameField.getText(), descField.getText(),
					sourceField.getText(),
					readNumber(costField, "Cost per kg"),
					readNumber(rawPriceField, "Raw price"),
					readNumber(marketPriceField, "Estimated market price")));
			reload();
			clearForm();
		} catch (IllegalArgumentException ex) {
			showError(ex.getMessage());
		}
	}

	private void handleUpdate() {
		Product selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showError("Select a product from the table first.");
			return;
		}
		try {
			database.editProduct(selected, nameField.getText(), descField.getText(),
					sourceField.getText(),
					readNumber(costField, "Cost per kg"),
					readNumber(rawPriceField, "Raw price"),
					readNumber(marketPriceField, "Estimated market price"));
			reload();
			clearForm();
		} catch (IllegalArgumentException ex) {
			showError(ex.getMessage());
		}
	}

	private void handleDelete() {
		Product selected = table.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showError("Select a product from the table first.");
			return;
		}
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
				"Delete \"" + selected.getProductName() + "\"?", ButtonType.YES, ButtonType.NO);
		confirm.setHeaderText(null);
		confirm.showAndWait().ifPresent(answer -> {
			if (answer == ButtonType.YES) {
				database.removeProduct(selected);
				reload();
				clearForm();
			}
		});
	}

	// ---------- Helpers ----------

	/** Pulls the latest products from the database into the table. */
	public void reload() {
		products.setAll(database.getAllProducts());
	}

	private double readNumber(TextField field, String label) {
		try {
			return Double.parseDouble(field.getText().trim());
		} catch (NumberFormatException | NullPointerException ex) {
			throw new IllegalArgumentException(label + " must be a valid number.");
		}
	}

	private String money(double value) {
		return String.format("PHP %.2f", value);
	}

	private void clearForm() {
		nameField.clear();
		sourceField.clear();
		descField.clear();
		costField.clear();
		rawPriceField.clear();
		marketPriceField.clear();
		table.getSelectionModel().clearSelection();
	}

	private void showError(String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR, message);
		alert.setHeaderText("Invalid input");
		alert.showAndWait();
	}

	/** Small helper so every column is built the same way. */
	private TableColumn<Product, String> column(String title, Function<Product, String> getter, double width) {
		TableColumn<Product, String> col = new TableColumn<>(title);
		col.setCellValueFactory(cell -> new SimpleStringProperty(getter.apply(cell.getValue())));
		col.setPrefWidth(width);
		return col;
	}
}
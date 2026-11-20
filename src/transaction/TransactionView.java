package transaction;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import Products.Product;
import Products.ProductDatabase;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import users.Buyer;

/**
 * Sales screen (point of sale).
 * Flow: choose a buyer -> Start Sale -> add products to the cart -> choose payment -> Complete Sale.
 * All the rules (stock check, budget check, receipt) live in SalesTransactionSystem;
 * this class only shows things and calls it.
 */
public class TransactionView extends BorderPane {

	private final SalesTransactionSystem sales;
	private final ProductDatabase products;
	private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private ComboBox<Buyer> buyerBox;
	private Button startButton;
	private ComboBox<Product> productBox;
	private Label stockLabel;
	private TextField kgField;
	private Button addButton;

	private TableView<Map.Entry<Product, Double>> cartTable;
	private Button removeButton;
	private Label budgetLabel;
	private Label totalLabel;
	private ComboBox<String> paymentBox;
	private Button completeButton;
	private Button cancelButton;

	// The sale in progress (null when no sale has been started)
	private Buyer currentBuyer;
	private Cart currentCart;

	public TransactionView(SalesTransactionSystem sales, ProductDatabase products, Collection<Buyer> buyers) {
		this.sales = sales;
		this.products = products;
		buildScreen(buyers);
		setSaleActive(false);
	}

	// ---------- Layout ----------

	private void buildScreen(Collection<Buyer> buyers) {
		Label title = new Label("Sales Transaction");
		title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
		HBox titleBox = new HBox(title);
		titleBox.setPadding(new Insets(15, 15, 0, 15));

		setTop(titleBox);
		setLeft(buildLeftPanel(buyers));
		setCenter(buildCartPanel());
	}

	private VBox buildLeftPanel(Collection<Buyer> buyers) {
		buyerBox = new ComboBox<>(FXCollections.observableArrayList(buyers));
		buyerBox.setPromptText("Select a buyer");
		buyerBox.setMaxWidth(Double.MAX_VALUE);
		buyerBox.setConverter(new StringConverter<Buyer>() {
			@Override
			public String toString(Buyer b) {
				return b == null ? "" : b.getName();
			}

			@Override
			public Buyer fromString(String s) {
				return null;
			}
		});

		startButton = new Button("Start Sale");
		startButton.setMaxWidth(Double.MAX_VALUE);
		startButton.setOnAction(e -> startSale());

		productBox = new ComboBox<>(FXCollections.observableArrayList(products.getAllProducts()));
		productBox.setPromptText("Select a product");
		productBox.setMaxWidth(Double.MAX_VALUE);
		productBox.setConverter(new StringConverter<Product>() {
			@Override
			public String toString(Product p) {
				return p == null ? "" : p.getProductCode() + " - " + p.getProductName();
			}

			@Override
			public Product fromString(String s) {
				return null;
			}
		});
		productBox.setOnAction(e -> updateStockLabel());

		stockLabel = new Label();
		kgField = new TextField();
		kgField.setPromptText("Quantity (kg)");

		addButton = new Button("Add to Cart");
		addButton.setMaxWidth(Double.MAX_VALUE);
		addButton.setOnAction(e -> addToCart());

		VBox panel = new VBox(10,
				new Label("1. Buyer"), buyerBox, startButton,
				new Separator(),
				new Label("2. Add items"), productBox, stockLabel, kgField, addButton);
		panel.setPadding(new Insets(15));
		panel.setPrefWidth(290);
		return panel;
	}

	private VBox buildCartPanel() {
		cartTable = buildCartTable();
		VBox.setVgrow(cartTable, Priority.ALWAYS);

		removeButton = new Button("Remove Selected");
		removeButton.setOnAction(e -> removeSelected());

		budgetLabel = new Label();
		totalLabel = new Label();

		paymentBox = new ComboBox<>(FXCollections.observableArrayList("Cash", "GCash", "Bank Transfer"));
		paymentBox.setPromptText("Payment method");

		completeButton = new Button("Complete Sale");
		completeButton.setDefaultButton(true);
		completeButton.setOnAction(e -> completeSale());

		cancelButton = new Button("Cancel Sale");
		cancelButton.setOnAction(e -> resetScreen());

		HBox payRow = new HBox(10, paymentBox, completeButton, cancelButton);

		VBox panel = new VBox(10, new Label("3. Cart"), cartTable, removeButton,
				new Separator(), budgetLabel, totalLabel, payRow);
		panel.setPadding(new Insets(15));
		return panel;
	}

	private TableView<Map.Entry<Product, Double>> buildCartTable() {
		TableView<Map.Entry<Product, Double>> table = new TableView<>();

		TableColumn<Map.Entry<Product, Double>, String> productCol = new TableColumn<>("Product");
		productCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getKey().getProductName()));

		TableColumn<Map.Entry<Product, Double>, String> kgCol = new TableColumn<>("Qty (kg)");
		kgCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("%.2f", d.getValue().getValue())));

		TableColumn<Map.Entry<Product, Double>, String> priceCol = new TableColumn<>("Price/kg");
		priceCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(
				String.format("%,.2f", Cart.unitPrice(d.getValue().getKey()))));

		TableColumn<Map.Entry<Product, Double>, String> subtotalCol = new TableColumn<>("Subtotal");
		subtotalCol.setCellValueFactory(d -> new ReadOnlyStringWrapper(
				String.format("%,.2f", Cart.unitPrice(d.getValue().getKey()) * d.getValue().getValue())));

		table.getColumns().addAll(productCol, kgCol, priceCol, subtotalCol);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		return table;
	}

	// ---------- Actions ----------

	private void startSale() {
		Buyer buyer = buyerBox.getValue();
		if (buyer == null) {
			showError("Select a buyer first.");
			return;
		}
		try {
			currentBuyer = buyer;
			currentCart = sales.createCart(buyer);
		} catch (IllegalArgumentException ex) {
			showError(ex.getMessage());
			return;
		}
		setSaleActive(true);
		refreshCart();
	}

	private void addToCart() {
		Product product = productBox.getValue();
		if (product == null) {
			showError("Select a product first.");
			return;
		}
		try {
			double kg = Double.parseDouble(kgField.getText().trim());
			if (Double.isNaN(kg) || kg <= 0) {
				showError("Quantity must be greater than zero.");
				return;
			}

			// Adding the same product again increases its quantity
			Double existing = currentCart.getCart().get(product);
			double newQty = kg + (existing == null ? 0 : existing);

			if (!sales.checkProductStockWithQuantity(product, newQty)) {
				showError(String.format("Not enough stock. Available: %.2f kg.", sales.productStock(product)));
				return;
			}

			currentCart.addToCart(product, newQty);
			kgField.clear();
			refreshCart();
		} catch (NumberFormatException ex) {
			showError("Enter a valid quantity.");
		} catch (IllegalArgumentException ex) {
			showError(ex.getMessage());
		}
	}

	private void removeSelected() {
		Map.Entry<Product, Double> row = cartTable.getSelectionModel().getSelectedItem();
		if (row == null) {
			showError("Select an item in the cart first.");
			return;
		}
		currentCart.removeFromCart(row.getKey());
		refreshCart();
	}

	private void completeSale() {
		String method = paymentBox.getValue();
		if (method == null) {
			showError("Select a payment method.");
			return;
		}
		try {
			sales.completeSale(currentBuyer, method);
		} catch (IllegalArgumentException ex) {
			showError(ex.getMessage());
			return;
		}
		showReceipt(currentCart.getReceipt(), currentBuyer.getName());
		resetScreen();
	}

	/** Clears the screen. An unpaid cart is simply dropped; the next sale starts a new one. */
	private void resetScreen() {
		currentBuyer = null;
		currentCart = null;
		cartTable.getItems().clear();
		kgField.clear();
		productBox.setValue(null);
		paymentBox.setValue(null);
		buyerBox.setValue(null);
		setSaleActive(false);
	}

	// ---------- Display helpers ----------

	/** Enables the sale controls only while a sale is in progress. */
	private void setSaleActive(boolean active) {
		buyerBox.setDisable(active);
		startButton.setDisable(active);
		productBox.setDisable(!active);
		kgField.setDisable(!active);
		addButton.setDisable(!active);
		removeButton.setDisable(!active);
		paymentBox.setDisable(!active);
		completeButton.setDisable(!active);
		cancelButton.setDisable(!active);

		if (!active) {
			stockLabel.setText("Available: -");
			budgetLabel.setText("Buyer budget: -");
			totalLabel.setText("Total: PHP 0.00");
			totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
		}
	}

	private void refreshCart() {
		cartTable.setItems(FXCollections.observableArrayList(sortedRows(currentCart)));

		double total = sales.cartTotal(currentCart);
		boolean overBudget = currentBuyer.getBudget() < total;
		budgetLabel.setText(String.format("Buyer budget: PHP %,.2f", currentBuyer.getBudget()));
		totalLabel.setText(String.format("Total: PHP %,.2f", total));
		totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"
				+ (overBudget ? " -fx-text-fill: #c0392b;" : ""));

		updateStockLabel();
	}

	private void updateStockLabel() {
		Product p = productBox.getValue();
		if (p == null) {
			stockLabel.setText("Available: -");
			return;
		}
		stockLabel.setText(String.format("Available: %.2f kg   (PHP %,.2f/kg)",
				sales.productStock(p), Cart.unitPrice(p)));
	}

	/** The cart's items as a list, in product-code order. */
	private List<Map.Entry<Product, Double>> sortedRows(Cart cart) {
		List<Map.Entry<Product, Double>> rows = new ArrayList<>(cart.getCart().entrySet());
		rows.sort(Comparator.comparing(r -> r.getKey().getProductCode()));
		return rows;
	}

	private void showReceipt(Receipt receipt, String buyerName) {
		StringBuilder sb = new StringBuilder();
		sb.append("Receipt: ").append(receipt.getTransactionID()).append("\n");
		sb.append("Date:    ").append(receipt.getTransacTime() == null ? "-" : receipt.getTransacTime().format(timeFormat)).append("\n");
		sb.append("Buyer:   ").append(buyerName).append("\n");
		sb.append("Cashier: ").append(receipt.getEmployee() == null ? "-" : receipt.getEmployee().getName()).append("\n\n");

		for (Map.Entry<Product, Double> item : sortedRows(receipt.getCart())) {
			double price = Cart.unitPrice(item.getKey());
			sb.append(String.format("%-14s %7.2f kg x %,9.2f = %,10.2f%n",
					item.getKey().getProductName(), item.getValue(), price, price * item.getValue()));
		}

		sb.append("\n");
		sb.append(String.format("Total:  PHP %,.2f%n", receipt.getCart().getTotalCartValue()));
		sb.append(String.format("Paid:   PHP %,.2f (%s)%n", receipt.getAmountPaid(), receipt.getPaymentMethod()));
		sb.append(String.format("Change: PHP %,.2f%n", receipt.getChange()));

		TextArea area = new TextArea(sb.toString());
		area.setEditable(false);
		area.setStyle("-fx-font-family: monospace;");
		area.setPrefRowCount(14);
		area.setPrefColumnCount(46);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Receipt");
		alert.setHeaderText("Sale complete");
		alert.getDialogPane().setContent(area);
		alert.showAndWait();
	}

	private void showError(String message) {
		new Alert(Alert.AlertType.ERROR, message).showAndWait();
	}
}

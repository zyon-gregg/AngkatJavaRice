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
 * Point-of-sale screen.
 * The view collects inputs and displays results. Sales validation, payment
 * recording, and inventory deductions are handled by SalesTransactionSystem.
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
    private Label cashReceivedLabel;
    private TextField cashReceivedField;
    private Button completeButton;
    private Button cancelButton;

    private Buyer currentBuyer;
    private Cart currentCart;

    public TransactionView(SalesTransactionSystem sales,
                           ProductDatabase products,
                           Collection<Buyer> buyers) {
        this.sales = sales;
        this.products = products;
        buildScreen(buyers);
        setSaleActive(false);
    }

    // --------------------------------------------------
    // Layout
    // --------------------------------------------------

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
            public String toString(Buyer buyer) {
                return buyer == null ? "" : buyer.getName();
            }

            @Override
            public Buyer fromString(String value) {
                return null;
            }
        });

        startButton = new Button("Start Sale");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setOnAction(event -> startSale());

        productBox = new ComboBox<>(FXCollections.observableArrayList(products.getAllProducts()));
        productBox.setPromptText("Select a product");
        productBox.setMaxWidth(Double.MAX_VALUE);
        productBox.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                return product == null ? "" : product.getProductCode() + " - " + product.getProductName();
            }

            @Override
            public Product fromString(String value) {
                return null;
            }
        });
        productBox.setOnAction(event -> updateStockLabel());

        stockLabel = new Label("Available: -");
        kgField = new TextField();
        kgField.setPromptText("Quantity (kg)");

        addButton = new Button("Add to Cart");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setOnAction(event -> addToCart());

        VBox panel = new VBox(10,
                new Label("1. Buyer"), buyerBox, startButton,
                new Separator(),
                new Label("2. Add items"), productBox, stockLabel, kgField, addButton);
        panel.setPadding(new Insets(15));
        return panel;
    }

    private VBox buildCartPanel() {
        cartTable = buildCartTable();
        VBox.setVgrow(cartTable, Priority.ALWAYS);

        removeButton = new Button("Remove Selected");
        removeButton.setOnAction(event -> removeSelected());

        budgetLabel = new Label();
        totalLabel = new Label();

        paymentBox = new ComboBox<>(FXCollections.observableArrayList(
                "Cash", "GCash", "Bank Transfer"));
        paymentBox.setPromptText("Payment method");
        paymentBox.setOnAction(event -> updatePaymentFields());

        cashReceivedLabel = new Label("Cash received (PHP):");
        cashReceivedField = new TextField();
        cashReceivedField.setPromptText("Enter amount received");
        cashReceivedField.setPrefColumnCount(14);

        completeButton = new Button("Complete Sale");
        completeButton.setDefaultButton(true);
        completeButton.setOnAction(event -> completeSale());

        cancelButton = new Button("Cancel Sale");
        cancelButton.setOnAction(event -> cancelSale());

        HBox cashRow = new HBox(8, cashReceivedLabel, cashReceivedField);
        HBox payRow = new HBox(10, paymentBox, completeButton, cancelButton);

        VBox panel = new VBox(10,
                new Label("3. Cart"), cartTable, removeButton,
                new Separator(), budgetLabel, totalLabel, cashRow, payRow);
        panel.setPadding(new Insets(15));
        return panel;
    }

    private TableView<Map.Entry<Product, Double>> buildCartTable() {
        TableView<Map.Entry<Product, Double>> table = new TableView<>();

        TableColumn<Map.Entry<Product, Double>, String> productColumn =
                new TableColumn<>("Product");
        productColumn.setCellValueFactory(data -> {
            Product product = data.getValue().getKey();
            String name = currentCart == null
                    ? product.getProductName()
                    : currentCart.getProductName(product);
            return new ReadOnlyStringWrapper(name);
        });

        TableColumn<Map.Entry<Product, Double>, String> quantityColumn =
                new TableColumn<>("Qty (kg)");
        quantityColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.format("%.2f", data.getValue().getValue())));

        TableColumn<Map.Entry<Product, Double>, String> priceColumn =
                new TableColumn<>("Price/kg");
        priceColumn.setCellValueFactory(data -> {
            Product product = data.getValue().getKey();
            double price = currentCart == null
                    ? Cart.unitPrice(product)
                    : currentCart.getUnitPrice(product);
            return new ReadOnlyStringWrapper(String.format("%,.2f", price));
        });

        TableColumn<Map.Entry<Product, Double>, String> subtotalColumn =
                new TableColumn<>("Subtotal");
        subtotalColumn.setCellValueFactory(data -> {
            Product product = data.getValue().getKey();
            double subtotal = currentCart == null
                    ? Cart.unitPrice(product) * data.getValue().getValue()
                    : currentCart.getSubtotal(product);
            return new ReadOnlyStringWrapper(String.format("%,.2f", subtotal));
        });

        table.getColumns().addAll(productColumn, quantityColumn, priceColumn, subtotalColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    // --------------------------------------------------
    // Sale actions
    // --------------------------------------------------

    private void startSale() {
        Buyer buyer = buyerBox.getValue();
        if (buyer == null) {
            showError("Select a buyer first.");
            return;
        }

        try {
            currentCart = sales.createCart(buyer);
            currentBuyer = buyer;
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            return;
        }

        setSaleActive(true);
        refreshCart();
    }

    private void addToCart() {
        if (currentCart == null) {
            showError("Start a sale first.");
            return;
        }

        Product product = productBox.getValue();
        if (product == null) {
            showError("Select a product first.");
            return;
        }

        try {
            double quantity = Double.parseDouble(kgField.getText().trim());
            if (!Double.isFinite(quantity) || quantity <= 0) {
                showError("Quantity must be a finite number greater than zero.");
                return;
            }

            Double existingQuantity = currentCart.getCart().get(product);
            double newQuantity = quantity + (existingQuantity == null ? 0.0 : existingQuantity);

            if (!Double.isFinite(newQuantity)) {
                showError("Combined quantity is invalid.");
                return;
            }

            if (!sales.checkProductStockWithQuantity(product, newQuantity)) {
                showError(String.format("Not enough stock. Available: %.2f kg.", sales.productStock(product)));
                return;
            }

            currentCart.addToCart(product, newQuantity);
            kgField.clear();
            refreshCart();
        } catch (NumberFormatException ex) {
            showError("Enter a valid quantity.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeSelected() {
        if (currentCart == null) {
            return;
        }

        Map.Entry<Product, Double> row = cartTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            showError("Select an item in the cart first.");
            return;
        }

        currentCart.removeFromCart(row.getKey());
        refreshCart();
    }

    private void completeSale() {
        if (currentBuyer == null || currentCart == null) {
            showError("Start a sale first.");
            return;
        }

        String paymentMethod = paymentBox.getValue();
        if (paymentMethod == null) {
            showError("Select a payment method.");
            return;
        }

        double cashReceived = 0.0;
        if ("Cash".equals(paymentMethod)) {
            try {
                cashReceived = Double.parseDouble(cashReceivedField.getText().trim());
            } catch (NumberFormatException ex) {
                showError("Enter the cash amount received from the buyer.");
                return;
            }
            if (!Double.isFinite(cashReceived) || cashReceived < 0) {
                showError("Cash received must be a valid non-negative amount.");
                return;
            }
        }

        try {
            Receipt completedReceipt = sales.completeSale(
                    currentBuyer, paymentMethod, cashReceived);
            showReceipt(completedReceipt, currentBuyer.getName());
            resetScreen();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void cancelSale() {
        if (currentBuyer != null) {
            try {
                sales.cancelSale(currentBuyer);
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
                return;
            }
        }
        resetScreen();
    }

    /** Clears the view after checkout or an explicit cancellation. */
    private void resetScreen() {
        currentBuyer = null;
        currentCart = null;
        cartTable.getItems().clear();
        kgField.clear();
        cashReceivedField.clear();
        productBox.setValue(null);
        paymentBox.setValue(null);
        buyerBox.setValue(null);
        setSaleActive(false);
    }

    // --------------------------------------------------
    // Display helpers
    // --------------------------------------------------

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
        cashReceivedField.setDisable(!active);

        if (!active) {
            stockLabel.setText("Available: -");
            budgetLabel.setText("Buyer spending limit: -");
            totalLabel.setText("Total: PHP 0.00");
            totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        }
        updatePaymentFields();
    }

    private void updatePaymentFields() {
        boolean cashSelected = paymentBox != null && "Cash".equals(paymentBox.getValue());
        if (cashReceivedLabel != null && cashReceivedField != null) {
            cashReceivedLabel.setVisible(cashSelected);
            cashReceivedLabel.setManaged(cashSelected);
            cashReceivedField.setVisible(cashSelected);
            cashReceivedField.setManaged(cashSelected);
            if (!cashSelected) {
                cashReceivedField.clear();
            }
        }
    }

    private void refreshCart() {
        if (currentCart == null || currentBuyer == null) {
            return;
        }

        cartTable.setItems(FXCollections.observableArrayList(sortedRows(currentCart)));
        cartTable.refresh();

        double total = sales.cartTotal(currentCart);
        boolean overBudget = currentBuyer.getBudget() < total;

        budgetLabel.setText(String.format(
                "Buyer spending limit: PHP %,.2f", currentBuyer.getBudget()));
        totalLabel.setText(String.format("Total: PHP %,.2f", total));
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;"
                + (overBudget ? " -fx-text-fill: #c0392b;" : ""));

        updateStockLabel();
    }

    private void updateStockLabel() {
        Product product = productBox.getValue();
        if (product == null) {
            stockLabel.setText("Available: -");
            return;
        }

        double available = sales.productStock(product);
        stockLabel.setText(String.format("Available: %.2f kg   (PHP %,.2f/kg)",
                available, Cart.unitPrice(product)));
    }

    private List<Map.Entry<Product, Double>> sortedRows(Cart cart) {
        List<Map.Entry<Product, Double>> rows = new ArrayList<>(cart.getCart().entrySet());
        rows.sort(Comparator.comparing(row -> row.getKey().getProductCode()));
        return rows;
    }

    private void showReceipt(Receipt receipt, String buyerName) {
        StringBuilder text = new StringBuilder();
        text.append("Receipt: ").append(receipt.getTransactionID()).append("\n");
        text.append("Date:    ")
                .append(receipt.getTransacTime() == null
                        ? "-" : receipt.getTransacTime().format(timeFormat))
                .append("\n");
        text.append("Buyer:   ").append(buyerName).append("\n");
        text.append("Cashier: ")
                .append(receipt.getEmployee() == null
                        ? "-" : receipt.getEmployee().getName())
                .append("\n\n");

        for (ReceiptItem item : receipt.getItems()) {
            text.append(String.format("%-14s %7.2f kg x %,9.2f = %,10.2f%n",
                    item.getProductName(), item.getQuantityInKG(),
                    item.getUnitPrice(), item.getSubtotal()));
        }

        text.append("\n");
        text.append(String.format("Total:  PHP %,.2f%n", receipt.getTotalAmount()));
        text.append(String.format("Paid:   PHP %,.2f (%s)%n",
                receipt.getAmountPaid(), receipt.getPaymentMethod()));

        if ("Cash".equals(receipt.getPaymentMethod())) {
            text.append(String.format("Cash received: PHP %,.2f%n", receipt.getCashReceived()));
            text.append(String.format("Change:        PHP %,.2f%n", receipt.getChange()));
        }

        TextArea area = new TextArea(text.toString());
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

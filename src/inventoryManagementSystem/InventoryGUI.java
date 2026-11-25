package inventoryManagementSystem;

import javafx.collections.FXCollections;
import javafx.beans.property.ReadOnlyStringWrapper;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;

import java.time.format.DateTimeFormatter;

public class InventoryGUI {

    // Window components
    private Stage stage;
    private Scene scene;
    private BorderPane root;

    // Backend and access context
    private final InventorySystem inventorySystem;
    private final String currentUsername;
    private final boolean canManageStock;

    // Tables
    private TableView<InventoryOfProduct> inventoryTable;
    private TableView<StockRecordedChange> historyTable;

    // Manager-only controls (remain null in read-only mode)
    private TextField quantityField;
    private TextField explanationField;
    private Label selectedProductLabel;

    private Button restockButton;
    private Button adjustButton;
    private Button deductButton;
    private Button refreshButton;

    private static final double MAX_STOCK_IN_KG = 1000.00;
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public InventoryGUI(InventorySystem inventorySystem, String currentUsername, boolean canManageStock) {

        if (inventorySystem == null) {
            throw new IllegalArgumentException("InventorySystem cannot be null.");
        }

        this.inventorySystem = inventorySystem;
        
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            this.currentUsername = "Unknown User";
        } else {
            this.currentUsername = currentUsername;
        }
        
        this.canManageStock = canManageStock;

        this.stage = new Stage();
        this.root = new BorderPane();
        this.root.getStyleClass().add("inventory-root");
        this.scene = new Scene(root, 1100, 750);

        // Load the stylesheet from the same package as InventoryGUI.
        java.net.URL stylesheet = getClass().getResource("InventoryGUIDesign.css");

        if (stylesheet == null) {
            throw new IllegalStateException(
                    "InventoryGUIDesign.css was not found in inventoryManagementSystem.");
        }

        scene.getStylesheets().add(stylesheet.toExternalForm());
    }

    public void setStageComponents() {

        VBox mainContainer = new VBox(10);
        mainContainer.getStyleClass().add("inventory-main-container");
        mainContainer.setPadding(new Insets(15));

        String inventoryTitleText = "Inventory Overview";

        if (canManageStock) {
            inventoryTitleText = "Inventory Management";
        }

        Label inventoryTitle = new Label(inventoryTitleText);
        inventoryTitle.getStyleClass().add("inventory-title");

        refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("inventory-refresh-button");

        // StackPane keeps the title centered while Refresh stays on the right.
        StackPane header = new StackPane();
        header.getStyleClass().add("inventory-header");
        StackPane.setAlignment(refreshButton, javafx.geometry.Pos.CENTER_RIGHT);
        header.getChildren().addAll(inventoryTitle, refreshButton);

        // First tab: inventory records and stock operations.
        inventoryTable = createInventoryTable();
        inventoryTable.setPrefHeight(350);

        VBox inventoryTabContent = new VBox(10);
        inventoryTabContent.getStyleClass().add("inventory-tab-content");
        inventoryTabContent.setPadding(new Insets(10));
        inventoryTabContent.getChildren().add(inventoryTable);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);

        if (canManageStock) {
            Separator operationsDivider = new Separator();
            operationsDivider.getStyleClass().add("inventory-operations-divider");
            inventoryTabContent.getChildren().add(operationsDivider);
            inventoryTabContent.getChildren().add(createManagerOperationsSection());
            registerInventorySelectionListener();
            
        } else {
            Label readOnlyNotice = new Label("Read-only access: Clerks can view stock levels and history, but only Managers can modify inventory.");
            readOnlyNotice.getStyleClass().add("inventory-read-only-notice");
            readOnlyNotice.setWrapText(true);
            inventoryTabContent.getChildren().add(readOnlyNotice);
        }

        Tab inventoryTab = new Tab("Inventory Records");
        inventoryTab.setClosable(false);
        inventoryTab.setContent(inventoryTabContent);

        // Second tab: history only.
        historyTable = createHistoryTable();
        historyTable.setPrefHeight(500);

        VBox historyTabContent = new VBox(10);
        historyTabContent.getStyleClass().add("inventory-tab-content");
        historyTabContent.setPadding(new Insets(10));
        historyTabContent.getChildren().add(historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        Tab historyTab = new Tab("Change History");
        historyTab.setClosable(false);
        historyTab.setContent(historyTabContent);

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("inventory-tab-pane");

        // Makes the two tabs equally wide across the available window width.
        DoubleBinding tabWidth = Bindings.createDoubleBinding(
                () -> Math.max(0, (tabPane.getWidth() - 24) / 2),
                tabPane.widthProperty()
        );

        tabPane.tabMinWidthProperty().bind(tabWidth);
        tabPane.tabMaxWidthProperty().bind(tabWidth);
        
        tabPane.getTabs().addAll(inventoryTab, historyTab);

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        mainContainer.getChildren().addAll(header, tabPane);

        refreshButton.setOnAction(event -> refreshTables());

        root.setCenter(mainContainer);

        if (canManageStock) {
            stage.setTitle("Angkat - Inventory Management");
        } else {
            stage.setTitle("Angkat - Inventory Overview (Read Only)");
        }

        stage.setScene(scene);

        refreshTables();
        stage.show();
    }

    //This one is a method to build the portion of the stock operations for manager only
    private VBox createManagerOperationsSection() {

        Label operationTitle = new Label("Inventory Operations");
        operationTitle.getStyleClass().add("inventory-operations-title");

        selectedProductLabel = new Label("Select a product from the table above.");
        selectedProductLabel.getStyleClass().add("inventory-selected-product");

        quantityField = new TextField();
        quantityField.setPromptText("Enter quantity in kilograms");

        explanationField = new TextField();
        explanationField.setPromptText("Reason for stock change");

        GridPane operationForm = new GridPane();
        operationForm.getStyleClass().add("inventory-operation-form");
        operationForm.setHgap(10);
        operationForm.setVgap(8);
        operationForm.add(new Label("Quantity (kg):"), 0, 0);
        operationForm.add(quantityField, 1, 0);
        operationForm.add(new Label("Explanation:"), 0, 1);
        operationForm.add(explanationField, 1, 1);

        restockButton = new Button("Restock");
        adjustButton = new Button("Adjust Stock");
        deductButton = new Button("Deduct Stock");

        restockButton.setOnAction(event -> handleRestock());
        adjustButton.setOnAction(event -> handleAdjustment());
        deductButton.setOnAction(event -> handleDeduction());

        restockButton.getStyleClass().add("inventory-restock-button");
        adjustButton.getStyleClass().add("inventory-adjust-button");
        deductButton.getStyleClass().add("inventory-deduct-button");

        HBox operationButtons = new HBox(10, restockButton, adjustButton, deductButton);
        operationButtons.getStyleClass().add("inventory-operation-buttons");

        VBox operationsSection = new VBox(
                10, operationTitle, selectedProductLabel, operationForm, operationButtons);
        operationsSection.getStyleClass().add("inventory-operations-section");

        return operationsSection;
    }

    private void registerInventorySelectionListener() {
        inventoryTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) {
                        selectedProductLabel.setText(
                                "Select a product from the table above.");
                    } else {
                        String productName = newValue.getProduct().getProductName();
                        String productCode = newValue.getProduct().getProductCode();
                        double stock = newValue.getProductStockInKG();

                        selectedProductLabel.setText("Selected: " + productName + " (" + productCode + ")" + " | Current Stock: " + String.format("%.2f kg", stock));
                    }
                });
    }

    private TableView<InventoryOfProduct> createInventoryTable() {

        TableView<InventoryOfProduct> table = new TableView<>();
        table.getStyleClass().add("inventory-table");

        TableColumn<InventoryOfProduct, String> codeColumn = new TableColumn<>("Product Code");
        
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getProduct().getProductCode()));

        TableColumn<InventoryOfProduct, String> productColumn =
                new TableColumn<>("Product Name");
        productColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getProduct().getProductName()));

        TableColumn<InventoryOfProduct, String> stockColumn =
                new TableColumn<>("Current Stock (kg)");
        stockColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.format("%.2f", data.getValue().getProductStockInKG())));

        TableColumn<InventoryOfProduct, String> statusColumn =
                new TableColumn<>("Stock Status");
        statusColumn.setCellValueFactory(data -> {
            InventoryOfProduct record = data.getValue();
            String productCode = record.getProduct().getProductCode();
            double stock = record.getProductStockInKG();

            String status;
            if (stock == 0) {
                status = "OUT OF STOCK";
            } else if (inventorySystem.isLowOnStock(productCode)) {
                status = "LOW STOCK";
            } else {
                status = "IN STOCK";
            }

            return new ReadOnlyStringWrapper(status);
        });

        TableColumn<InventoryOfProduct, String> updatedColumn =
                new TableColumn<>("Last Updated");
        updatedColumn.setCellValueFactory(data -> {
            if (data.getValue().getLastUpdated() == null) {
                return new ReadOnlyStringWrapper("N/A");
            }

            String updatedText = data.getValue().getLastUpdated().format(dateFormat);
            return new ReadOnlyStringWrapper(updatedText);
        });

        table.getColumns().addAll(
                codeColumn, productColumn, stockColumn, statusColumn, updatedColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No inventory records available."));

        return table;
    }

    private TableView<StockRecordedChange> createHistoryTable() {

        TableView<StockRecordedChange> table = new TableView<>();
        table.getStyleClass().add("inventory-table");

        TableColumn<StockRecordedChange, String> idColumn =
                new TableColumn<>("Record ID");
        idColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getRecordId()));

        TableColumn<StockRecordedChange, String> codeColumn =
                new TableColumn<>("Product Code");
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getProductCode()));

        TableColumn<StockRecordedChange, String> typeColumn =
                new TableColumn<>("Change Type");
        typeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getChangeType()));

        TableColumn<StockRecordedChange, String> previousColumn =
                new TableColumn<>("Previous Stock");
        previousColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.format("%.2f", data.getValue().getPreviousStock())));

        TableColumn<StockRecordedChange, String> newColumn =
                new TableColumn<>("New Stock");
        newColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.format("%.2f", data.getValue().getNewStock())));

        TableColumn<StockRecordedChange, String> amountColumn =
                new TableColumn<>("Amount Changed");
        amountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                String.format("%+.2f", data.getValue().getAmountChanged())));

        TableColumn<StockRecordedChange, String> dateColumn =
                new TableColumn<>("Date/Time");
        dateColumn.setCellValueFactory(data -> {
            if (data.getValue().getTime() == null) {
                return new ReadOnlyStringWrapper("N/A");
            }

            String recordedTime = data.getValue().getTime().format(dateFormat);
            return new ReadOnlyStringWrapper(recordedTime);
        });

        TableColumn<StockRecordedChange, String> userColumn =
                new TableColumn<>("Performed By");
        userColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getPerformedBy()));

        TableColumn<StockRecordedChange, String> reasonColumn =
                new TableColumn<>("Explanation");
        reasonColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getExplanation()));

        table.getColumns().addAll(
                idColumn, codeColumn, typeColumn, previousColumn, newColumn,
                amountColumn, dateColumn, userColumn, reasonColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    // --------------------------------------------------
    // Manager-only stock operations
    // --------------------------------------------------

    private boolean requireManagerPermission() {
        if (!canManageStock) {
            showError("You do not have permission to modify inventory. "
                    + "Please contact a Manager.");
            return false;
        }
        return true;
    }

    private void handleRestock() {

        if (!requireManagerPermission()) return;

        InventoryOfProduct selected =
                inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a product first.");
            return;
        }

        Double quantity = getQuantityInput();
        if (quantity == null) return;

        if (quantity <= 0) {
            showError("Restock quantity must be greater than zero.");
            return;
        }

        double currentStock = selected.getProductStockInKG();
        if (currentStock + quantity > MAX_STOCK_IN_KG) {
            showError("Restocking would exceed the maximum stock of 1000 kg.");
            return;
        }

        String reason = getExplanation();
        if (reason == null) return;

        inventorySystem.restockProduct(
                selected.getProduct().getProductCode(),
                quantity,
                currentUsername,
                reason);

        completeOperation("Stock successfully restocked.");
    }

    private void handleAdjustment() {

        if (!requireManagerPermission()) return;

        InventoryOfProduct selected =
                inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a product first.");
            return;
        }

        Double quantity = getQuantityInput();
        if (quantity == null) return;

        if (quantity < 0) {
            showError("Stock quantity cannot be negative.");
            return;
        }

        if (quantity > MAX_STOCK_IN_KG) {
            showError("Stock cannot exceed 1000 kg.");
            return;
        }

        String reason = getExplanation();
        if (reason == null) return;

        inventorySystem.editStock(
                selected.getProduct().getProductCode(),
                quantity,
                currentUsername,
                reason);

        completeOperation("Stock successfully adjusted.");
    }

    private void handleDeduction() {

        if (!requireManagerPermission()) return;

        InventoryOfProduct selected =
                inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a product first.");
            return;
        }

        Double quantity = getQuantityInput();
        if (quantity == null) return;

        if (quantity <= 0) {
            showError("Deduction quantity must be greater than zero.");
            return;
        }

        double currentStock = selected.getProductStockInKG();
        if (quantity > currentStock) {
            showError("Insufficient stock for this deduction.");
            return;
        }

        String reason = getExplanation();
        if (reason == null) return;

        inventorySystem.reduceStock(
                selected.getProduct().getProductCode(),
                quantity,
                currentUsername,
                reason);

        completeOperation("Stock successfully deducted.");
    }

    // --------------------------------------------------
    // Input, refresh, and messages
    // --------------------------------------------------

    private Double getQuantityInput() {

        String input = quantityField.getText().trim();
        if (input.isEmpty()) {
            showError("Please enter a quantity.");
            return null;
        }

        try {
            double quantity = Double.parseDouble(input);
            if (!Double.isFinite(quantity)) {
                showError("Please enter a valid finite number.");
                return null;
            }
            return quantity;
        } catch (NumberFormatException exception) {
            showError("Invalid quantity. Please enter a number.");
            return null;
        }
    }

    private String getExplanation() {
        String reason = explanationField.getText().trim();
        if (reason.isEmpty()) {
            showError("Please provide an explanation for this operation.");
            return null;
        }
        return reason;
    }

    private void completeOperation(String message) {
        refreshTables();
        quantityField.clear();
        explanationField.clear();
        showInformation(message);
    }

    public void refreshTables() {

        inventoryTable.setItems(FXCollections.observableArrayList(
                inventorySystem.getAllInventory()));

        historyTable.setItems(FXCollections.observableArrayList(
                inventorySystem.getAllHistory()));

        if (selectedProductLabel != null) {
            selectedProductLabel.setText(
                    "Select a product from the table above.");
        }

        inventoryTable.refresh();
        historyTable.refresh();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Inventory Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Inventory Management");
        alert.setHeaderText("Operation Successful");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters retained for tests or other GUI integrations.
    public Button getRestockButton() { return restockButton; }
    public Button getAdjustButton() { return adjustButton; }
    public Button getDeductButton() { return deductButton; }
    public Button getRefreshButton() { return refreshButton; }
    public TableView<InventoryOfProduct> getInventoryTable() { return inventoryTable; }
    public TableView<StockRecordedChange> getHistoryTable() { return historyTable; }
    public Stage getStage() { return stage; }
    public boolean canManageStock() { return canManageStock; }
}
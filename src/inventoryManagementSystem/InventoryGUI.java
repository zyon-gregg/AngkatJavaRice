package inventoryManagementSystem;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Inventory screen. It is a BorderPane, so SceneManager can show it inside the main window.
 * canEdit = true  -> Manager view (restock / adjust buttons visible)
 * canEdit = false -> Clerk view (read-only)
 */
public class InventoryGUI extends BorderPane {

    private final InventorySystem inventorySystem;
    private final String currentUser;
    private final boolean canEdit;

    private TableView<InventoryOfProduct> inventoryTable;
    private TableView<StockRecordedChange> historyTable;

    private Button restockButton;
    private Button adjustButton;
    private Button refreshButton;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public InventoryGUI(InventorySystem inventorySystem, String currentUser, boolean canEdit) {
        this.inventorySystem = inventorySystem;
        this.currentUser = currentUser;
        this.canEdit = canEdit;
        buildScreen();
        refreshTables();
    }

    private void buildScreen() {
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(15));

        Label inventoryTitle = new Label("Inventory Management");
        inventoryTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshTables());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, inventoryTitle, spacer, refreshButton);

        restockButton = new Button("Restock Product");
        adjustButton = new Button("Adjust Stock");
        restockButton.setOnAction(e -> handleRestock());
        adjustButton.setOnAction(e -> handleAdjust());

        HBox operationButtons = new HBox(10, restockButton, adjustButton);
        // Clerks can only view
        operationButtons.setVisible(canEdit);
        operationButtons.setManaged(canEdit);

        inventoryTable = createInventoryTable();
        historyTable = createHistoryTable();
        inventoryTable.setPrefHeight(250);
        historyTable.setPrefHeight(250);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        Label historyTitle = new Label("Stock Movement History");

        mainContainer.getChildren().addAll(header, operationButtons, inventoryTable,
                new Separator(), historyTitle, historyTable);
        setCenter(mainContainer);
    }

    // ---------- Actions ----------

    private void handleRestock() {
        InventoryOfProduct selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a product in the table first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Restock Product");
        dialog.setHeaderText("Restock " + selected.getProduct().getProductName());
        dialog.setContentText("Amount to add (kg):");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            double amount = Double.parseDouble(result.get().trim());
            inventorySystem.restockProduct(selected.getProduct().getProductCode(),
                    amount, currentUser, "Restock");
            refreshTables();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid number.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleAdjust() {
        // TODO: call the adjust method from InventorySystem once its signature is confirmed
        showError("Adjust Stock is not connected yet.");
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    // ---------- Tables ----------

    private TableView<InventoryOfProduct> createInventoryTable() {
        TableView<InventoryOfProduct> table = new TableView<>();

        TableColumn<InventoryOfProduct, String> codeColumn = new TableColumn<>("Product Code");
        codeColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getProduct().getProductCode()));

        TableColumn<InventoryOfProduct, String> productColumn = new TableColumn<>("Product Name");
        productColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getProduct().getProductName()));

        TableColumn<InventoryOfProduct, String> stockColumn = new TableColumn<>("Current Stock (kg)");
        stockColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("%.2f", d.getValue().getProductStockInKG())));

        TableColumn<InventoryOfProduct, String> statusColumn = new TableColumn<>("Stock Status");
        statusColumn.setCellValueFactory(d -> {
            InventoryOfProduct record = d.getValue();
            String code = record.getProduct().getProductCode();
            String status;
            if (record.getProductStockInKG() == 0) {
                status = "OUT OF STOCK";
            } else if (inventorySystem.isLowOnStock(code)) {
                status = "LOW STOCK";
            } else {
                status = "IN STOCK";
            }
            return new ReadOnlyStringWrapper(status);
        });

        TableColumn<InventoryOfProduct, String> updatedColumn = new TableColumn<>("Last Updated");
        updatedColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().getLastUpdated() == null ? "N/A" : d.getValue().getLastUpdated().format(dateFormat)));

        table.getColumns().addAll(codeColumn, productColumn, stockColumn, statusColumn, updatedColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TableView<StockRecordedChange> createHistoryTable() {
        TableView<StockRecordedChange> table = new TableView<>();

        TableColumn<StockRecordedChange, String> idColumn = new TableColumn<>("Record ID");
        idColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getRecordId()));

        TableColumn<StockRecordedChange, String> codeColumn = new TableColumn<>("Product Code");
        codeColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getProductCode()));

        TableColumn<StockRecordedChange, String> typeColumn = new TableColumn<>("Change Type");
        typeColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getChangeType()));

        TableColumn<StockRecordedChange, String> previousColumn = new TableColumn<>("Previous Stock");
        previousColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("%.2f", d.getValue().getPreviousStock())));

        TableColumn<StockRecordedChange, String> newColumn = new TableColumn<>("New Stock");
        newColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("%.2f", d.getValue().getNewStock())));

        TableColumn<StockRecordedChange, String> amountColumn = new TableColumn<>("Amount Changed");
        amountColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(String.format("%+.2f", d.getValue().getAmountChanged())));

        TableColumn<StockRecordedChange, String> dateColumn = new TableColumn<>("Date/Time");
        dateColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(
                d.getValue().getTime() == null ? "N/A" : d.getValue().getTime().format(dateFormat)));

        TableColumn<StockRecordedChange, String> userColumn = new TableColumn<>("Performed By");
        userColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getPerformedBy()));

        TableColumn<StockRecordedChange, String> reasonColumn = new TableColumn<>("Explanation");
        reasonColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getExplanation()));

        table.getColumns().addAll(idColumn, codeColumn, typeColumn, previousColumn,
                newColumn, amountColumn, dateColumn, userColumn, reasonColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    public void refreshTables() {
        inventoryTable.setItems(FXCollections.observableArrayList(inventorySystem.getAllInventory()));
        historyTable.setItems(FXCollections.observableArrayList(inventorySystem.getAllHistory()));
    }

    // ---------- Getters ----------

    public Button getRestockButton() { return restockButton; }
    public Button getAdjustButton() { return adjustButton; }
    public Button getRefreshButton() { return refreshButton; }
    public TableView<InventoryOfProduct> getInventoryTable() { return inventoryTable; }
    public TableView<StockRecordedChange> getHistoryTable() { return historyTable; }
}
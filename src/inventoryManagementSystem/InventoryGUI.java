package inventoryManagementSystem;

import javafx.collections.FXCollections;
import javafx.beans.property.ReadOnlyStringWrapper;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class InventoryGUI {

    // Window components
    private Stage stage;
    private Scene scene;
    private BorderPane root;

    // Backend
    private InventorySystem inventorySystem;

    // Tables
    private TableView<InventoryOfProduct> inventoryTable;
    private TableView<StockRecordedChange> historyTable;

    // Buttons
    private Button restockButton;
    private Button adjustButton;
    private Button refreshButton;

    // Date format
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public InventoryGUI(InventorySystem inventorySystem) {
        this.inventorySystem = inventorySystem;
        this.stage = new Stage();
        this.root = new BorderPane();
        this.scene = new Scene(root, 1100, 750);
    }

    // Sets the components of the Stage
    public void setStageComponents() {

        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(15));

        Label inventoryTitle = new Label("Inventory Management");

        refreshButton = new Button("Refresh");

        HBox header = new HBox(10);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(inventoryTitle,spacer,refreshButton);

        restockButton = new Button("Restock Product");
        adjustButton = new Button("Adjust Stock");

        HBox operationButtons = new HBox(
                10,
                restockButton,
                adjustButton
        );

        inventoryTable = createInventoryTable();


        Label historyTitle = new Label("Stock Movement History");

        historyTable = createHistoryTable();

  
        inventoryTable.setPrefHeight(250);
        historyTable.setPrefHeight(250);

        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
        VBox.setVgrow(historyTable, Priority.ALWAYS);


        mainContainer.getChildren().addAll(header,operationButtons, inventoryTable, new Separator(), historyTitle, historyTable);
        
        refreshButton.setOnAction(e -> refreshTables());

        this.root.setCenter(mainContainer);

        this.stage.setTitle("Angkat - Inventory Management");
        this.stage.setScene(this.scene);

        refreshTables();

        this.stage.show();
    }


    private TableView<InventoryOfProduct> createInventoryTable() {

        TableView<InventoryOfProduct> table = new TableView<>();

        // Product Code
        TableColumn<InventoryOfProduct, String> codeColumn = new TableColumn<>("Product Code");
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProduct().getProductCode()));
        
        // Product Name
        TableColumn<InventoryOfProduct, String> productColumn = new TableColumn<>("Product Name");
        productColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProduct().getProductName()));

        // Current Stock
        TableColumn<InventoryOfProduct, String> stockColumn = new TableColumn<>("Current Stock (kg)");

        stockColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f",data.getValue().getProductStockInKG())));

        // Stock Status
        TableColumn<InventoryOfProduct, String> statusColumn = new TableColumn<>("Stock Status");
        
        statusColumn.setCellValueFactory(data -> {
        	InventoryOfProduct record = data.getValue();
            String productCode =record.getProduct().getProductCode();
            String status;

            if (record.getProductStockInKG() == 0) {
                status = "OUT OF STOCK";
            } else if (inventorySystem.isLowOnStock(productCode)) {
                status = "LOW STOCK";
            } else {
                status = "IN STOCK";
            }

            return new ReadOnlyStringWrapper(status);
        });

        // Last Updated
        TableColumn<InventoryOfProduct, String> updatedColumn = new TableColumn<>("Last Updated");

        updatedColumn.setCellValueFactory(data -> {
            if (data.getValue().getLastUpdated() == null) {
                return new ReadOnlyStringWrapper("N/A");
            }

            return new ReadOnlyStringWrapper(data.getValue().getLastUpdated().format(dateFormat));
        });

        table.getColumns().addAll(codeColumn, productColumn, stockColumn,statusColumn,updatedColumn);

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        return table;
    }

   

    private TableView<StockRecordedChange> createHistoryTable() {
        TableView<StockRecordedChange> table = new TableView<>();

        // Record ID
        TableColumn<StockRecordedChange, String> idColumn = new TableColumn<>("Record ID");
        idColumn.setCellValueFactory(data ->new ReadOnlyStringWrapper(data.getValue().getRecordId()));

        // Product Code
        TableColumn<StockRecordedChange, String> codeColumn = new TableColumn<>("Product Code");
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProductCode()));

        // Change Type
        TableColumn<StockRecordedChange, String> typeColumn = new TableColumn<>("Change Type");
        typeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getChangeType()));

        // Previous Stock
        TableColumn<StockRecordedChange, String> previousColumn = new TableColumn<>("Previous Stock");
        previousColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f",data.getValue().getPreviousStock())));

        // New Stock
        TableColumn<StockRecordedChange, String> newColumn = new TableColumn<>("New Stock");
        newColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f", data.getValue().getNewStock())));

        // Amount Changed
        TableColumn<StockRecordedChange, String> amountColumn = new TableColumn<>("Amount Changed");
        amountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%+.2f",data.getValue().getAmountChanged())));

        // Date and Time
        TableColumn<StockRecordedChange, String> dateColumn = new TableColumn<>("Date/Time");
        dateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTime() == null ? "N/A" : data.getValue().getTime().format(dateFormat)));

        // Performed By
        TableColumn<StockRecordedChange, String> userColumn = new TableColumn<>("Performed By");    
        userColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPerformedBy()));

        // Explanation
        TableColumn<StockRecordedChange, String> reasonColumn = new TableColumn<>("Explanation");
        reasonColumn.setCellValueFactory(data ->new ReadOnlyStringWrapper(data.getValue().getExplanation()));

        table.getColumns().addAll(
                idColumn,
                codeColumn,
                typeColumn,
                previousColumn,
                newColumn,
                amountColumn,
                dateColumn,
                userColumn,
                reasonColumn
        );
        
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );


        return table;
    }

   
    public void refreshTables() {
        inventoryTable.setItems(FXCollections.observableArrayList(inventorySystem.getAllInventory()));
        historyTable.setItems(FXCollections.observableArrayList(inventorySystem.getAllHistory()));
    }
    
    //getters

    public Button getRestockButton() {
        return restockButton;
    }

    public Button getAdjustButton() {
        return adjustButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public TableView<InventoryOfProduct> getInventoryTable() {
        return inventoryTable;
    }

    public TableView<StockRecordedChange> getHistoryTable() {
        return historyTable;
    }

    public Stage getStage() {
        return stage;
    }
}

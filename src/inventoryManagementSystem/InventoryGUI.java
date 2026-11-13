
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

    // Current user (could be manager or clerk)
    private String currentUsername;

    // Tables
    private TableView<InventoryOfProduct> inventoryTable;
    private TableView<StockRecordedChange> historyTable;

    
    private TextField quantityField;
    private TextField explanationField;

    private Label selectedProductLabel;

    // Buttons
    private Button restockButton;
    private Button adjustButton;
    private Button deductButton;
    private Button refreshButton;

    // Maximum stock
    private static final double MAX_STOCK_IN_KG = 1000.00;

    // Date formatter
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public InventoryGUI(InventorySystem inventorySystem) {
        this(inventorySystem, "Manager");
    }

    // with username
    public InventoryGUI(
            InventorySystem inventorySystem,
            String currentUsername) {

        this.inventorySystem = inventorySystem;
        this.currentUsername = currentUsername;

        this.stage = new Stage();
        this.root = new BorderPane();
        this.scene = new Scene(root, 1100, 750);
    }
    
    public void setStageComponents() {

        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(15));

   
        Label inventoryTitle = new Label("Inventory Management");

        refreshButton = new Button("Refresh");

        HBox header = new HBox(10);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(inventoryTitle, spacer, refreshButton);

        // Inventory table
        inventoryTable = createInventoryTable();
        inventoryTable.setPrefHeight(200);

   

        Label operationTitle = new Label("Inventory Operations");

        selectedProductLabel = new Label("Select a product from the table above.");


        quantityField = new TextField();
        quantityField.setPromptText("Enter quantity in kilograms");

        explanationField = new TextField();
        explanationField.setPromptText("Reason for stock change");

  
        GridPane operationForm = new GridPane();

        operationForm.setHgap(10);
        operationForm.setVgap(8);

        operationForm.add(new Label("Quantity (kg):"), 0, 0);
        operationForm.add(quantityField, 1, 0);
        operationForm.add(new Label("Explanation:"), 0, 1);
        operationForm.add(explanationField, 1, 1);

        // Operation buttons
        restockButton = new Button("Restock");
        adjustButton = new Button("Adjust Stock");
        deductButton = new Button("Deduct Stock (Test)");

        HBox operationButtons = new HBox(10, restockButton, adjustButton, deductButton);

        VBox operationSection = new VBox(10, operationTitle, selectedProductLabel, operationForm, operationButtons);

        //history table
        Label historyTitle = new Label("Stock Movement History");

        historyTable = createHistoryTable();
        historyTable.setPrefHeight(200);

        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        mainContainer.getChildren().addAll(header, inventoryTable, new Separator(), operationSection, new Separator(), historyTitle, historyTable);

        //event handlers
        refreshButton.setOnAction(e -> refreshTables());
        restockButton.setOnAction(e -> handleRestock());
        adjustButton.setOnAction(e -> handleAdjustment());
        deductButton.setOnAction(e -> handleDeduction());


        inventoryTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        	
        	if (newValue == null) {
        		selectedProductLabel.setText("Select a product from the table above.");
        		
        	} else {
        		String productName = newValue.getProduct().getProductName();
        		String productCode = newValue.getProduct().getProductCode();
        		
        		double stock = newValue.getProductStockInKG();
        		
        		selectedProductLabel.setText("Selected: " + productName + " (" + productCode + ")" + " | Current Stock: " + String.format("%.2f kg", stock));
        		
        	}
                
        });

     
        this.root.setCenter(mainContainer);

        this.stage.setTitle("Angkat - Inventory Management");
        this.stage.setScene(this.scene);

        refreshTables();

        this.stage.show();
    }



    private TableView<InventoryOfProduct> createInventoryTable() {

        TableView<InventoryOfProduct> table = new TableView<>();
        
        //column for product code
        TableColumn<InventoryOfProduct, String> codeColumn = new TableColumn<>("Product Code");
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProduct().getProductCode()));
        
        
        //column for product name
        TableColumn<InventoryOfProduct, String> productColumn = new TableColumn<>("Product Name");
        productColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProduct().getProductName()));
        
        
        //column for stock amount
        TableColumn<InventoryOfProduct, String> stockColumn = new TableColumn<>("Current Stock (kg)");
        stockColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f", data.getValue().getProductStockInKG())));
        
        
        //this one's for status
        TableColumn<InventoryOfProduct, String> statusColumn = new TableColumn<>("Stock Status");
        statusColumn.setCellValueFactory(data -> {InventoryOfProduct record = data.getValue();
        
        String productCode = record.getProduct().getProductCode();
        
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
        
        
        //updated column
        TableColumn<InventoryOfProduct, String> updatedColumn = new TableColumn<>("Last Updated");
        updatedColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getLastUpdated() == null ? "N/A" : data.getValue().getLastUpdated().format(dateFormat)));

        table.getColumns().addAll(codeColumn, productColumn, stockColumn, statusColumn, updatedColumn);

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        return table;
    }

    
    //table view for recording changes
    private TableView<StockRecordedChange> createHistoryTable() {

        TableView<StockRecordedChange> table = new TableView<>();

        TableColumn<StockRecordedChange, String> idColumn = new TableColumn<>("Record ID");
        idColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getRecordId()));

        TableColumn<StockRecordedChange, String> codeColumn = new TableColumn<>("Product Code");
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProductCode()));

        TableColumn<StockRecordedChange, String> typeColumn = new TableColumn<>("Change Type");
        typeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getChangeType()));

        TableColumn<StockRecordedChange, String> previousColumn = new TableColumn<>("Previous Stock");
        previousColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f", data.getValue().getPreviousStock())));

        TableColumn<StockRecordedChange, String> newColumn = new TableColumn<>("New Stock");
        newColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f", data.getValue().getNewStock())));

        TableColumn<StockRecordedChange, String> amountColumn = new TableColumn<>("Amount Changed");
        amountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%+.2f", data.getValue().getAmountChanged())));

        TableColumn<StockRecordedChange, String> dateColumn = new TableColumn<>("Date/Time");
        dateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTime() == null ? "N/A" : data.getValue().getTime().format(dateFormat)));

        TableColumn<StockRecordedChange, String> userColumn = new TableColumn<>("Performed By");
        userColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPerformedBy()));

        TableColumn<StockRecordedChange, String> reasonColumn = new TableColumn<>("Explanation");
        reasonColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getExplanation()));

        table.getColumns().addAll(idColumn, codeColumn, typeColumn, previousColumn, newColumn, amountColumn, dateColumn, userColumn, reasonColumn);

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        return table;
    }


   
    //handling stock
    private void handleRestock() {

        InventoryOfProduct selected = inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a product first.");
            return;
        }

        Double quantity = getQuantityInput();

        if (quantity == null) {
            return;
        }

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

        if (reason == null) {
            return;
        }

        inventorySystem.restockProduct(
                selected.getProduct().getProductCode(),
                quantity,
                currentUsername,
                reason
        );

        completeOperation("Stock successfully restocked.");
    }


    
    //adjusting stock
    private void handleAdjustment() {

        InventoryOfProduct selected =
                inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a product first.");
            return;
        }

        Double quantity = getQuantityInput();

        if (quantity == null) {
            return;
        }

        if (quantity < 0) {
            showError("Stock quantity cannot be negative.");
            return;
        }

        if (quantity > MAX_STOCK_IN_KG) {
            showError("Stock cannot exceed 1000 kg.");
            return;
        }

        String reason = getExplanation();

        if (reason == null) {
            return;
        }

        inventorySystem.editStock(
                selected.getProduct().getProductCode(),
                quantity,
                currentUsername,
                reason
        );

        completeOperation("Stock successfully adjusted.");
    }


    //reducing stock
    private void handleDeduction() {

        InventoryOfProduct selected = inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a product first.");
            return;
        }

        Double quantity = getQuantityInput();

        if (quantity == null) {
            return;
        }

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

        if (reason == null) {
            return;
        }

        inventorySystem.reduceStock(
                selected.getProduct().getProductCode(),
                quantity,
                currentUsername,
                reason
        );

        completeOperation("Stock successfully deducted.");
    }


   
    //input validation
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

        } catch (NumberFormatException e) {

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

        inventoryTable.setItems(
                FXCollections.observableArrayList(
                        inventorySystem.getAllInventory()
                )
        );

        historyTable.setItems(
                FXCollections.observableArrayList(
                        inventorySystem.getAllHistory()
                )
        );
        
        System.out.print("Github testing");

        selectedProductLabel.setText(
                "Select a product from the table above."
        );
        
        inventoryTable.refresh();
        historyTable.refresh();
    }


    

    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Inventory Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);

        alert.showAndWait();
    }


    private void showInformation(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Inventory Management");
        alert.setHeaderText("Operation Successful");
        alert.setContentText(message);

        alert.showAndWait();
    }

    public Button getRestockButton() {
        return restockButton;
    }

    public Button getAdjustButton() {
        return adjustButton;
    }

    public Button getDeductButton() {
        return deductButton;
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

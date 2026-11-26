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

    //The main components of the GUI
    private Stage stage;
    private Scene scene;
    private BorderPane root;

    //integrating the back end of the inventory system
    //This is abstraction because we separated the duties of the back end and the GUI instead of combining them in a one large class
    private final InventorySystem inventorySystem;
    private final String currentUsername;
    private final boolean canManageStock; //this is a checker to check who is using the inventory window since only managers can access the operations while clerks can only view.

    //Tables
    private TableView<InventoryOfProduct> inventoryTable;
    private TableView<StockRecordedChange> historyTable;
    
    //This attributes are only when manager is using the inventory window
    //This sets to be null when a clerk accesses it
    private TextField quantityField;
    private TextField explanationField;
    private Label selectedProductLabel;
    
    //the buttons
    private Button restockButton;
    private Button adjustButton;
    private Button deductButton;
    private Button refreshButton;

    private static final double MAX_STOCK_IN_KG = 1000.00; //max stock constant
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public InventoryGUI(InventorySystem inventorySystem, String currentUsername, boolean canManageStock) {
    	

        if (inventorySystem == null) {//ensures that the back end exists
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
        
        // Loading the css from the same package as InventoryGUI.
        java.net.URL stylesheet = getClass().getResource("InventoryGUIDesign.css");

        if (stylesheet == null) {
            throw new IllegalStateException("InventoryGUIDesign.css was not found in inventoryManagementSystem.");
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

        //StackPane keeps the title centered while the refresh button stays on the right.
        StackPane header = new StackPane();
        header.getStyleClass().add("inventory-header");
        StackPane.setAlignment(refreshButton, javafx.geometry.Pos.CENTER_RIGHT);
        header.getChildren().addAll(inventoryTitle, refreshButton);

        //The tab for the product's inventory table
        inventoryTable = createInventoryTable();
        inventoryTable.setPrefHeight(350);

        VBox inventoryTabContent = new VBox(10);
        inventoryTabContent.getStyleClass().add("inventory-tab-content");
        inventoryTabContent.setPadding(new Insets(10));
        inventoryTabContent.getChildren().add(inventoryTable);
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);

        if (canManageStock) {
            Separator operationsDivider = new Separator(); //adding a divider between the table view and the stock operation
            operationsDivider.getStyleClass().add("inventory-operations-divider");
            inventoryTabContent.getChildren().add(operationsDivider);
            inventoryTabContent.getChildren().add(createManagerOperationsSection());
            registerInventorySelectionListener();
            
        } else {
            Label readOnlyNotice = new Label("Inventory window is read only for Clerks. Managers are only the one's who can perform the stock operations");
            readOnlyNotice.getStyleClass().add("inventory-read-only-notice");
            readOnlyNotice.setWrapText(true);
            inventoryTabContent.getChildren().add(readOnlyNotice);
        }

        Tab inventoryTab = new Tab("Stock of Products");
        inventoryTab.setClosable(false);
        inventoryTab.setContent(inventoryTabContent);

        //the second tab is for the history table view
        historyTable = createHistoryTable();
        historyTable.setPrefHeight(500);

        VBox historyTabContent = new VBox(10);
        historyTabContent.getStyleClass().add("inventory-tab-content");
        historyTabContent.setPadding(new Insets(10));
        historyTabContent.getChildren().add(historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        Tab historyTab = new Tab("History");
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
            stage.setTitle("Angkat - Inventory Overview (Read Only for Clerks)");
        }

        stage.setScene(scene);

        refreshTables();
        stage.show();
    }

    //This one is a method to build the portion of the stock operations in the window for manager use
    private VBox createManagerOperationsSection() {

        Label operationTitle = new Label("----- Inventory Operations -----");
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

        //restock button
        restockButton = new Button("Restock");
        restockButton.setOnAction(event -> handleRestock());
        restockButton.getStyleClass().add("inventory-restock-button");
        
        //adjust stock button
        adjustButton = new Button("Adjust Stock");
        adjustButton.setOnAction(event -> handleAdjustment());
        adjustButton.getStyleClass().add("inventory-adjust-button");
        
        
        //deduct stock button
        deductButton = new Button("Deduct Stock");           
        deductButton.setOnAction(event -> handleDeduction());
        deductButton.getStyleClass().add("inventory-deduct-button");

        HBox operationButtons = new HBox(10, restockButton, adjustButton, deductButton);
        operationButtons.getStyleClass().add("inventory-operation-buttons");

        VBox operationsSection = new VBox(10, operationTitle, selectedProductLabel, operationForm, operationButtons);
        operationsSection.getStyleClass().add("inventory-operations-section");

        return operationsSection;
    }
    
    //a listener for selecting a product in the inventory record table view
    private void registerInventorySelectionListener() {
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
    
    //creates a table view for all the product's stock
    private TableView<InventoryOfProduct> createInventoryTable() {

        TableView<InventoryOfProduct> table = new TableView<>();
        table.getStyleClass().add("inventory-table");

        //Column for code
        TableColumn<InventoryOfProduct, String> codeColumn = new TableColumn<>("Product Code");      
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProduct().getProductCode()));

        //Column for product's name
        TableColumn<InventoryOfProduct, String> productColumn = new TableColumn<>("Product Name");
        productColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProduct().getProductName()));

        //Column for the current stock of a product
        TableColumn<InventoryOfProduct, String> stockColumn = new TableColumn<>("Current Stock (kg)");
        stockColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f", data.getValue().getProductStockInKG())));

        //Column for the status of the product's stock (low, in stock, out)
        TableColumn<InventoryOfProduct, String> statusColumn =new TableColumn<>("Stock Status");
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
        
        
        //Column for date and time for the most recent edit of that product's stock
        TableColumn<InventoryOfProduct, String> updatedColumn = new TableColumn<>("Last Updated");
        updatedColumn.setCellValueFactory(data -> {
        	
            if (data.getValue().getLastUpdated() == null) {
                return new ReadOnlyStringWrapper("N/A");
            }

            String updatedText = data.getValue().getLastUpdated().format(dateFormat);
            return new ReadOnlyStringWrapper(updatedText);
        });

        table.getColumns().addAll(codeColumn, productColumn, stockColumn, statusColumn, updatedColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No inventory records available.")); //when the table view is empty

        return table;
    }
    
    //creates a table view for the history (recorded changes of stocks across all products)
    private TableView<StockRecordedChange> createHistoryTable() {

        TableView<StockRecordedChange> table = new TableView<>();
        table.getStyleClass().add("inventory-table");

        //Column for record Id
        TableColumn<StockRecordedChange, String> idColumn = new TableColumn<>("Record ID");
        idColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getRecordId()));

        //Column for product Id
        TableColumn<StockRecordedChange, String> codeColumn = new TableColumn<>("Product Code");
        codeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getProductCode()));

        //Column for what kind of change performed on a stock
        TableColumn<StockRecordedChange, String> typeColumn = new TableColumn<>("Change Type");
        typeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getChangeType()));

        //Column for the product's previous stock
        TableColumn<StockRecordedChange, String> previousColumn = new TableColumn<>("Previous Stock");
        previousColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f", data.getValue().getPreviousStock())));

        //Column for the product's new stock after a stock operation
        TableColumn<StockRecordedChange, String> newColumn = new TableColumn<>("New Stock");
        newColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%.2f", data.getValue().getNewStock())));

        //Column for the amount changed (New stock - previous stock)
        TableColumn<StockRecordedChange, String> amountColumn = new TableColumn<>("Amount Changed");
        amountColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("%+.2f", data.getValue().getAmountChanged())));

        //Column for date
        TableColumn<StockRecordedChange, String> dateColumn = new TableColumn<>("Date/Time");
        dateColumn.setCellValueFactory(data -> {
        	
            if (data.getValue().getTime() == null) { //in case it fails to get the date
                return new ReadOnlyStringWrapper("N/A");
            }

            String recordedTime = data.getValue().getTime().format(dateFormat);
            
            return new ReadOnlyStringWrapper(recordedTime);
        });
        
        
        //Column for the name of user who performed the stock operation
        TableColumn<StockRecordedChange, String> userColumn = new TableColumn<>("Performed By");
        userColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getPerformedBy()));

        //Column for the explanation/reason
        TableColumn<StockRecordedChange, String> reasonColumn = new TableColumn<>("Explanation");
        reasonColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getExplanation()));

        //adds all to the table
        table.getColumns().addAll(idColumn, codeColumn, typeColumn, previousColumn, newColumn, amountColumn, dateColumn, userColumn, reasonColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    
    private boolean requireManagerPermission() { //this is a method to ensure that the stock operations can be performed ONLY by managers
        if (!canManageStock) {
            showError("You do not have permission to modify inventory. Please contact a Manager.");
            return false;
        }
        
        return true;
    }
    
    //a stock operation for restocking a product's stock by adding a stock to the product's current stock
    private void handleRestock() {

        if (!requireManagerPermission()) {
        	return;
        }

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
        
        //restockProduct(product code, quantity, name of user, and explanation)
        inventorySystem.restockProduct(selected.getProduct().getProductCode(), quantity, currentUsername, reason);
        
        completeOperation("Stock successfully restocked.");
    }
    
    //this is a method for adjusting a stock to a specific amount for correction.
    //Restock adds, adjustment sets the stock to a specific amount
    private void handleAdjustment() {

        if (!requireManagerPermission()) {
        	return;
        }

        InventoryOfProduct selected = inventoryTable.getSelectionModel().getSelectedItem();
        
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
        
        //edit stock parameters: product code, quantity, name of user, explanation
        inventorySystem.editStock(selected.getProduct().getProductCode(), quantity, currentUsername, reason);

        completeOperation("Stock successfully adjusted.");
    }
    
    //a method to handle the deductions of a stock
    private void handleDeduction() {

        if (!requireManagerPermission()) {//stock operations are only permitted to managers
        	return;
        }

        InventoryOfProduct selected = inventoryTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {//ensures there is a selected product's stock in the table view
            showError("Please select a product first.");
            return;
        }

        Double quantity = getQuantityInput();
        
        if (quantity == null) {
        	return;
        }

        if (quantity <= 0) { //validates the input. The requested amount cannot be negative
            showError("Deduction quantity must be greater than zero.");
            return;
        }

        double currentStock = selected.getProductStockInKG();
        
        if (quantity > currentStock) {//ensures that there is enough stock for the deduction process
            showError("Insufficient stock for this deduction.");
            return;
        }

        String reason = getExplanation();
        
        if (reason == null) {
        	return;
        }
        
        //reduceStock(code, quantity, name of user, explanation)
        inventorySystem.reduceStock(selected.getProduct().getProductCode(), quantity, currentUsername, reason);

        completeOperation("Stock successfully deducted.");
    }
    
    //a method to validate the input in the quantity text field
    private Double getQuantityInput() {

        String input = quantityField.getText().trim();
        
        if (input.isEmpty()) { //ensures there is an input in the quantity text field
            showError("Please enter a quantity.");
            return null;
        }

        try {
            double quantity = Double.parseDouble(input);
            
            if (!Double.isFinite(quantity)) {//ensures the input is not infinity
                showError("Please enter a valid finite number.");
                return null;
            }
            
            return quantity;
            
        } catch (NumberFormatException exception) {//ensures that is is actually a number
            showError("Invalid quantity. Please enter a number.");
            return null;
        }
    }
    
    //a method to to check if the text field for the explanation is empty or not
    private String getExplanation() {
        String reason = explanationField.getText().trim();
        
        if (reason.isEmpty()) {
            showError("Please provide an explanation for this operation.");
            return null;
        }
        
        return reason;
    }
    
    //when an operation is completed, it refreshes everything for a fresh input
    private void completeOperation(String message) {
        refreshTables();
        quantityField.clear();
        explanationField.clear();
        showInformation(message);
    }
    
    //method for refreshing the tables to ensure they're updated
    public void refreshTables() {

        inventoryTable.setItems(FXCollections.observableArrayList(inventorySystem.getAllInventory()));
        historyTable.setItems(FXCollections.observableArrayList(inventorySystem.getAllHistory()));

        if (selectedProductLabel != null) {
            selectedProductLabel.setText("Select a product from the table above.");   
        }

        inventoryTable.refresh();
        historyTable.refresh();
    }
    
    
    //Displays an alert window to show an error
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Inventory Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    //displays an alert window to inform that a stock operation was a success
    private void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Inventory Management");
        alert.setHeaderText("Operation Successful");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters retained for tests or other GUI integrations.
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
    
    public boolean canManageStock() { 
    	return canManageStock; 
    }
}
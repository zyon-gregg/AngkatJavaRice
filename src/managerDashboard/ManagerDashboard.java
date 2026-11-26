package managerDashboard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import Products.ProductDatabase;
import Products.ProductManagementView;
import inventoryManagementSystem.InventoryGUI;
import inventoryManagementSystem.InventorySystem;
import inventoryManagementSystem.StockRecordedChange;
import login.LoginManager;
import transaction.SalesTransactionSystem;
import transaction.Receipt;
import transaction.ReceiptItem;
import transaction.TransactionView;
import users.Buyer;
import users.User;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ManagerDashboard {
	//the manager dashboard

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    //Setting the stage, the specific manager to be associated with the dash board and a runnable for logout
    private final Stage stage;
    private final User manager;
    private final Runnable onLogout;

    //Imported the back end of all the Angkat system
    private final ProductDatabase productDatabase;
    private final InventorySystem inventorySystem;
    private final SalesTransactionSystem salesSystem;
    private final List<Buyer> buyers;
    private final LoginManager loginManager;
    
    //Other contents
    private BorderPane root;
    private VBox mainContent;
    private StackPane activityTableArea;

    //Labels for the overview cards un the business overview
    private Label totalProductsValue;
    private Label lowStockValue;
    private Label outOfStockValue;
    private Label inventoryRecordsValue;

    //Currently selected activity table
    private String currentActivity = "INVENTORY";

    public ManagerDashboard(Stage stage, User manager, ProductDatabase productDatabase, InventorySystem inventorySystem, SalesTransactionSystem salesSystem, List<Buyer> buyers, Runnable onLogout) { 
    	
    	
    	this(stage, manager, productDatabase, inventorySystem, salesSystem, buyers, null, onLogout);
           
    }

    public ManagerDashboard(Stage stage, User manager, ProductDatabase productDatabase, InventorySystem inventorySystem, SalesTransactionSystem salesSystem, List<Buyer> buyers, LoginManager loginManager, Runnable onLogout) {
        this.stage = stage;
        this.manager = manager;
        this.productDatabase = productDatabase;
        this.inventorySystem = inventorySystem;
        this.salesSystem = salesSystem;
        this.buyers = buyers;
        this.loginManager = loginManager;
        this.onLogout = onLogout;
    }

    //added this method show() so that the main can call the actual setStageComponents
    public void show() {
        setStageComponents();
    }

    //this is where the construction of the manager dashboard happens
    public void setStageComponents() {

    	//setting the root
        root = new BorderPane();
        root.getStyleClass().add("manager-dashboard-root");

        //the main contents of the GUI will be in vBox, so its layout is top to bottom
        mainContent = new VBox(24);
        mainContent.setPadding(new Insets(24));
        mainContent.setFillWidth(true);
        mainContent.getStyleClass().add("manager-main-content");

        //Adding the children
        mainContent.getChildren().addAll(createHeader(), createSectionDivider(), createBusinessOverview(), createManagementModules(), createSectionDivider(), createSystemActivity(), createFooter());

        //added scroll panes
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.getStyleClass().add("manager-scroll-pane");

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1250, 850);

        // Loading the stylesheet using its actual filename in this package.
        java.net.URL dashboardStylesheet = getClass().getResource("ManagerDashboardDesign.css");

        if (dashboardStylesheet != null) { //ensures proper load of the style sheet
            scene.getStylesheets().add(dashboardStylesheet.toExternalForm());
        } else {
            System.out.println("ManagerDashboardDesign.css was not found in the managerDashboard package.");
        }

        stage.setTitle("Angkat - Manager Dashboard");
        stage.setMinWidth(1050);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    //==================== Header layout ====================
    
    //uses a fall back when the user field is empty (user field is optional in the creation
    private String getDisplayValue(String value, String fallback) {
    	
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        if (fallback == null || fallback.trim().isEmpty()) {
            return "Not assigned";
        }

        return fallback;
    }

    private HBox createHeader() {

    	//title
        Label applicationName = new Label("ANGKAT");
        applicationName.getStyleClass().add("manager-brand-name");
        
        //subtitle
        Label applicationSubtitle = new Label("FISH PORT MANAGEMENT SYSTEM");
        applicationSubtitle.getStyleClass().add("manager-brand-subtitle");
        
        //grouping the title and subtitle together in a vbox layout
        VBox branding = new VBox(4, applicationName, applicationSubtitle);
        branding.getStyleClass().add("manager-branding");

        String displayName = getDisplayValue(manager.getName(), manager.getUserName());
        String displayID = getDisplayValue(manager.getUserID(), manager.getUserName());

        Label welcomeLabel = new Label("Welcome back, " + displayName + "!");
        welcomeLabel.getStyleClass().add("manager-welcome");
        
        //the details of the manager (ID and username)
        Label managerDetails = new Label("Manager ID: " + displayID + "   |   Username: " + manager.getUserName());
        managerDetails.getStyleClass().add("manager-details");
        
        //grouped them together in a vBox and placed it right of the header
        VBox managerInformation = new VBox(4, welcomeLabel, managerDetails);
        managerInformation.setAlignment(Pos.CENTER_RIGHT);
        managerInformation.getStyleClass().add("manager-information");

        //refresh button
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshDashboard());
        refreshButton.getStyleClass().addAll("manager-header-button", "manager-refresh-button");
       
        //logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> confirmLogout());
        logoutButton.getStyleClass().addAll("manager-header-button", "manager-logout-button");
        

        //added space between them
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        //constructing the header
        HBox header = new HBox(18, branding, spacer, managerInformation, refreshButton, logoutButton);

        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("manager-header");
        return header;
    }

    //added a divider. Used it to separate the header, the buttons, and the table view below
    private Separator createSectionDivider() {
        Separator divider = new Separator();
        divider.getStyleClass().add("manager-section-divider");
        return divider;
    }
    
    //=======================================================
    
    //==================== Overview Layout ====================

    private VBox createBusinessOverview() {//creates the portion of the business overview

    	//title
        Label sectionTitle = new Label("Business Overview");
        sectionTitle.getStyleClass().add("manager-section-title");

        //initializing the numbers to be displayed
        totalProductsValue = new Label("0");
        lowStockValue = new Label("0");
        outOfStockValue = new Label("0");
        inventoryRecordsValue = new Label("0");

        //added cards
        GridPane cards = new GridPane();
        cards.setHgap(14);
        cards.setVgap(14);
        cards.getStyleClass().add("manager-summary-grid");

        //a loop to initialize column constraints. Adjusting the spacing between the cards
        for (int i = 0; i < 4; i++) {
            javafx.scene.layout.ColumnConstraints column = new javafx.scene.layout.ColumnConstraints();
            column.setPercentWidth(25);
            column.setHgrow(Priority.ALWAYS);
            cards.getColumnConstraints().add(column);
        }

        cards.add(createSummaryCard("Total Products", totalProductsValue), 0, 0);
        cards.add(createSummaryCard("Low-stock Products", lowStockValue), 1, 0);
        cards.add(createSummaryCard("Out-of-stock Products", outOfStockValue), 2, 0);
        cards.add(createSummaryCard("Inventory Records", inventoryRecordsValue), 3, 0);

        updateSummaryCards();

        VBox section = new VBox(12, sectionTitle, cards);
        section.getStyleClass().add("manager-section");
        return section;
    }

    private VBox createSummaryCard(String title, Label value) { //creation of the summary cards

    	//title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("manager-summary-title");

        value.getStyleClass().add("manager-summary-value");
        
        //cards
        VBox card = new VBox(10, titleLabel, value);
        card.setPadding(new Insets(18));
        card.setMinHeight(105);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("manager-summary-card");

        //setting the colors of each card
        if ("Total Products".equals(title)) {
            card.getStyleClass().add("summary-blue");
            
        } else if ("Low-stock Products".equals(title)) {
            card.getStyleClass().add("summary-orange");
            
        } else if ("Out-of-stock Products".equals(title)) {
            card.getStyleClass().add("summary-red");
            
        } else {
            card.getStyleClass().add("summary-teal");
        }

        GridPane.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    //updating the summary cards for each in product and stock
    private void updateSummaryCards() {
        totalProductsValue.setText(String.valueOf(productDatabase.getAllProducts().size()));
        lowStockValue.setText(String.valueOf(inventorySystem.getProductsLowOnStock().size()));
        outOfStockValue.setText(String.valueOf(inventorySystem.getProductsOutOfStock().size()));
        inventoryRecordsValue.setText(String.valueOf(inventorySystem.getAllInventory().size()));
    }
    
    //=========================================================

   
    //==================== Products, Inventory, Sales, User management buttons ====================

    private VBox createManagementModules() {

        Label sectionTitle = new Label("Management Modules");
        sectionTitle.getStyleClass().add("manager-section-title");

        Button productButton = createModuleButton(
                "Product Management",
                "Manage seafood products and product information.",
                "manager-module-blue");

        Button inventoryButton = createModuleButton(
                "Inventory Management",
                "Monitor stock, restock products, and adjust quantities.",
                "manager-module-green");

        Button salesButton = createModuleButton(
                "Sales & Transactions",
                "Open the sales terminal and review payment records.",
                "manager-module-orange");

        Button userButton = createModuleButton(
                "User Management",
                "Review pending registrations and manage accounts.",
                "manager-module-purple");

        productButton.setOnAction(event -> openProductManagement());
        inventoryButton.setOnAction(event -> openInventoryManagement());
        salesButton.setOnAction(event -> openSalesManagement());
        userButton.setOnAction(event -> openUserManagement());

        GridPane moduleGrid = new GridPane();
        moduleGrid.setHgap(14);
        moduleGrid.setVgap(14);
        moduleGrid.getStyleClass().add("manager-module-grid");

        moduleGrid.add(productButton, 0, 0);
        moduleGrid.add(inventoryButton, 1, 0);
        moduleGrid.add(salesButton, 0, 1);
        moduleGrid.add(userButton, 1, 1);

        moduleGrid.getColumnConstraints().addAll(
                createEqualColumn(), createEqualColumn());

        VBox section = new VBox(12, sectionTitle, moduleGrid);
        section.getStyleClass().add("manager-section");
        return section;
    }

    private javafx.scene.layout.ColumnConstraints createEqualColumn() {
        javafx.scene.layout.ColumnConstraints column =
                new javafx.scene.layout.ColumnConstraints();
        column.setPercentWidth(50);
        column.setHgrow(Priority.ALWAYS);
        return column;
    }

    private Button createModuleButton(
            String title, String description, String colorStyle) {

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("manager-module-title");
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setWrapText(true);

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("manager-module-description");
        descriptionLabel.setAlignment(Pos.CENTER);
        descriptionLabel.setWrapText(true);

        Label actionLabel = new Label("OPEN MODULE  >");
        actionLabel.getStyleClass().add("manager-module-action");
        actionLabel.setAlignment(Pos.CENTER);

        VBox buttonContent = new VBox(
                9,
                titleLabel,
                descriptionLabel,
                actionLabel);
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setFillWidth(true);
        buttonContent.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);
        actionLabel.setMaxWidth(Double.MAX_VALUE);
        buttonContent.getStyleClass().add("manager-module-content");

        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setAlignment(Pos.CENTER);
        button.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(145);
        button.setPrefHeight(155);
        button.getStyleClass().addAll("manager-module-button", colorStyle);

        return button;
    }
    
    //=============================================================================================
    
    //========== The functionalities of the buttons ==========
    
    //opening of the product management window
    private void openProductManagement() {
    	ProductManagementView productView = new ProductManagementView(productDatabase, inventorySystem, true);
    	
    	Stage productStage = new Stage();
    	productStage.initOwner(stage);
    	productStage.setTitle("Angkat - Product Management");
    	
    	Scene productScene = new Scene(productView, 1000, 560);
    	
    	productScene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());

    	// Display Product Management in its own window.
    	productStage.setScene(productScene);

    	// Refresh the dashboard after Product Management closes.
    	productStage.setOnHidden(event -> refreshDashboard());
    	productStage.show();
    	
    }


    //for inventory window
    private void openInventoryManagement() {
    	InventoryGUI inventoryGUI = new InventoryGUI(inventorySystem, manager.getUserName(), true);

        // Refresh the dashboard after the Manager closes Inventory Management.
        inventoryGUI.getStage().setOnHidden(event -> refreshDashboard());
        inventoryGUI.setStageComponents();
    }

    //for sales management
    private void openSalesManagement() {
    	
    	TransactionView transactionView = new TransactionView(salesSystem, productDatabase, buyers);

    	//setting the stage
        Stage transactionStage = new Stage();
        transactionStage.initOwner(stage);
        transactionStage.setTitle("Angkat - Sales & Transactions");
        
        //setting the scene
        Scene transactionScene = new Scene(transactionView, 1150, 700);
        transactionScene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        transactionStage.setScene(transactionScene);
        transactionStage.setOnHidden(event -> refreshDashboard());
        transactionStage.show();
    }
    
    

    //for user management window
    private void openUserManagement() {
    	
        if (loginManager == null) {//checks if login manager is properly integrated
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Management");
            alert.setHeaderText("Login manager is not connected.");
            alert.setContentText("Please connect LoginManager");
            alert.initOwner(stage);
            alert.showAndWait();
            return;
        }

        UserManagementView userManagementView = new UserManagementView(loginManager, manager);

        Stage userStage = new Stage();
        userStage.initOwner(stage);
        userStage.setTitle("Angkat - User Management");
        
        Scene userScene = new Scene(userManagementView, 1050, 760);
        userScene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        userStage.setScene(userScene);
        userStage.show();
    }
    
    //========================================================

    //creates the system activity. Located at the bottom of the manager dashboard
    private VBox createSystemActivity() {

        Label sectionTitle = new Label("System Activity");
        sectionTitle.getStyleClass().add("manager-section-title");

        ToggleGroup activityGroup = new ToggleGroup();

        ToggleButton inventoryButton = new ToggleButton("Inventory Changes");
        inventoryButton.getStyleClass().add("manager-activity-toggle");
        
        ToggleButton transactionButton = new ToggleButton("Transactions");
        transactionButton.getStyleClass().add("manager-activity-toggle");

        inventoryButton.setToggleGroup(activityGroup);
        transactionButton.setToggleGroup(activityGroup);
        inventoryButton.setSelected(true);

        HBox selectors = new HBox(10, inventoryButton, transactionButton);
        selectors.setAlignment(Pos.CENTER_LEFT);
        selectors.getStyleClass().add("manager-activity-selectors");

        activityTableArea = new StackPane();
        activityTableArea.getStyleClass().add("manager-activity-table-area");
        refreshActivityTable();

        inventoryButton.setOnAction(event -> {
            currentActivity = "INVENTORY";
            refreshActivityTable();
        });

        transactionButton.setOnAction(event -> {
            currentActivity = "TRANSACTIONS";
            refreshActivityTable();
        });

        VBox section = new VBox(12, sectionTitle, selectors, activityTableArea);
        section.getStyleClass().add("manager-section");
        return section;
    }

    //refreshing the table
    private void refreshActivityTable() {

        if (activityTableArea == null) {//checks first if the table exists
            return;
        }

        if ("INVENTORY".equals(currentActivity)) { //refreshes the inventory history table
            activityTableArea.getChildren().setAll(createInventoryHistoryTable(8));
        } else { //refreshes the transaction history table
            activityTableArea.getChildren().setAll(createTransactionHistoryTable(8));
        }
    }

    
    //creates the tableview of the changes in the stocks of the product, the inventory
    private TableView<StockRecordedChange> createInventoryHistoryTable(int maximumRows) {

    	//setting the table
        TableView<StockRecordedChange> table = new TableView<>();
        table.getStyleClass().add("manager-activity-table");

        //column for id
        TableColumn<StockRecordedChange, String> idColumn = new TableColumn<>("Record ID");
        idColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getRecordId()));

        //product code
        TableColumn<StockRecordedChange, String> productColumn = new TableColumn<>("Product Code");
        productColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getProductCode()));

        //change type
        TableColumn<StockRecordedChange, String> changeColumn = new TableColumn<>("Change Type");
        changeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getChangeType()));

        //previous stock
        TableColumn<StockRecordedChange, String> beforeColumn = new TableColumn<>("Previous Stock");
        beforeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.format("%.2f kg", cell.getValue().getPreviousStock())));

        //new stock
        TableColumn<StockRecordedChange, String> afterColumn = new TableColumn<>("New Stock");
        afterColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.format("%.2f kg", cell.getValue().getNewStock())));

        //employee column
        TableColumn<StockRecordedChange, String> employeeColumn = new TableColumn<>("Performed By");
        employeeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPerformedBy()));

        //date and time
        TableColumn<StockRecordedChange, String> dateColumn = new TableColumn<>("Date and Time");
        dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatDateTime(cell.getValue().getTime())));

        //explanation for the change
        TableColumn<StockRecordedChange, String> explanationColumn = new TableColumn<>("Explanation");
        explanationColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getExplanation()));

        table.getColumns().addAll(idColumn, productColumn, changeColumn, beforeColumn, afterColumn, employeeColumn, dateColumn, explanationColumn);

        List<StockRecordedChange> records = new ArrayList<>(inventorySystem.getAllHistory());

        //Shows the newest stock changes first.
        for (int i = 0; i < records.size(); i++) {
            for (int j = i + 1; j < records.size(); j++) {
            	
                LocalDateTime firstTime = records.get(i).getTime();
                LocalDateTime secondTime = records.get(j).getTime();
                boolean moveSecondRecord = false;

                if (firstTime == null && secondTime != null) {
                    moveSecondRecord = true;
                    
                } else if (firstTime != null && secondTime != null && firstTime.isBefore(secondTime)) {
                    moveSecondRecord = true;
                }

                if (moveSecondRecord) {
                    StockRecordedChange temporaryRecord = records.get(i);
                    records.set(i, records.get(j));
                    records.set(j, temporaryRecord);
                }
            }
        }

        if (maximumRows > 0 && records.size() > maximumRows) {
            records = new ArrayList<>(records.subList(0, maximumRows));
        }

        table.setItems(FXCollections.observableArrayList(records));
        table.setPlaceholder(new Label("No inventory changes have been recorded."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(350);

        return table;
    }

    //creates the table view for transaction table
    private Node createTransactionHistoryTable(int maximumRows) {

        TableView<Receipt> table = new TableView<>();
        table.getStyleClass().add("manager-activity-table");

        TableColumn<Receipt, String> receiptIDColumn = new TableColumn<>("Receipt ID");
        receiptIDColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getTransactionID()));

        TableColumn<Receipt, String> buyerColumn =
                new TableColumn<>("Buyer");
        buyerColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getReceiptBuyerName(cell.getValue())));

        TableColumn<Receipt, String> employeeColumn =
                new TableColumn<>("Processed By");
        employeeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getReceiptEmployeeName(cell.getValue())));

        TableColumn<Receipt, String> paymentColumn =
                new TableColumn<>("Payment Method");
        paymentColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getReceiptPaymentMethod(cell.getValue())));

        TableColumn<Receipt, String> totalColumn =
                new TableColumn<>("Total Amount");
        totalColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                formatMoney(cell.getValue().getTotalAmount())));

        TableColumn<Receipt, String> dateColumn =
                new TableColumn<>("Date and Time");
        dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                formatDateTime(cell.getValue().getTransacTime())));

        TableColumn<Receipt, String> statusColumn =
                new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getReceiptStatus(cell.getValue())));

        table.getColumns().addAll(
                receiptIDColumn,
                buyerColumn,
                employeeColumn,
                paymentColumn,
                totalColumn,
                dateColumn,
                statusColumn);

        // Retrieve the saved receipts and place the newest transaction first.
        List<Receipt> records = new ArrayList<>(salesSystem.getAllReceipt());

        for (int i = 0; i < records.size(); i++) {
            for (int j = i + 1; j < records.size(); j++) {
                LocalDateTime firstTime = records.get(i).getTransacTime();
                LocalDateTime secondTime = records.get(j).getTransacTime();
                boolean moveSecondReceipt = false;

                if (firstTime == null && secondTime != null) {
                    moveSecondReceipt = true;
                } else if (firstTime != null && secondTime != null
                        && firstTime.isBefore(secondTime)) {
                    moveSecondReceipt = true;
                }

                if (moveSecondReceipt) {
                    Receipt temporaryReceipt = records.get(i);
                    records.set(i, records.get(j));
                    records.set(j, temporaryReceipt);
                }
            }
        }

        if (maximumRows > 0 && records.size() > maximumRows) {
            records = new ArrayList<>(records.subList(0, maximumRows));
        }

        table.setItems(FXCollections.observableArrayList(records));
        table.setPlaceholder(new Label("No completed transactions have been recorded."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(350);

        Button viewReceiptButton = new Button("View Selected Receipt");
        viewReceiptButton.getStyleClass().add("manager-view-receipt-button");
        viewReceiptButton.setOnAction(event -> {
            Receipt selectedReceipt = table.getSelectionModel().getSelectedItem();
            showReceiptDetails(selectedReceipt);
        });

        HBox tableActions = new HBox(10, viewReceiptButton);
        tableActions.setAlignment(Pos.CENTER_LEFT);
        tableActions.getStyleClass().add("manager-table-actions");

        VBox transactionHistory = new VBox(10, table, tableActions);
        transactionHistory.getStyleClass().add("manager-transaction-history");
        return transactionHistory;
    }

    private String getReceiptBuyerName(Receipt receipt) {
        if (receipt == null) {
            return "";
        }

        String buyerName = receipt.getBuyerName();
        if (buyerName == null || buyerName.trim().isEmpty()) {
            return "Not recorded";
        }

        return buyerName;
    }

    private String getReceiptEmployeeName(Receipt receipt) {
        if (receipt == null || receipt.getEmployee() == null) {
            return "Not recorded";
        }

        String employeeName = receipt.getEmployee().getName();
        if (employeeName == null || employeeName.trim().isEmpty()) {
            employeeName = receipt.getEmployee().getUserName();
        }

        if (employeeName == null || employeeName.trim().isEmpty()) {
            return "Not recorded";
        }

        return employeeName;
    }

    private String getReceiptPaymentMethod(Receipt receipt) {
        if (receipt == null || receipt.getPaymentMethod() == null) {
            return "Not recorded";
        }

        return receipt.getPaymentMethod();
    }

    private String getReceiptStatus(Receipt receipt) {
        if (receipt == null || receipt.getPurchaseStatus() == null) {
            return "Not recorded";
        }

        return receipt.getPurchaseStatus();
    }

    private String formatMoney(Double amount) {
        if (amount == null) {
            return "PHP 0.00";
        }

        return String.format("PHP %,.2f", amount);
    }

    /** Displays the selected receipt and its saved item details. */
    private void showReceiptDetails(Receipt receipt) {
        if (receipt == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(stage);
            alert.setTitle("Transaction Details");
            alert.setHeaderText(null);
            alert.setContentText("Select a transaction from the table first.");
            alert.showAndWait();
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Receipt ID: ").append(receipt.getTransactionID()).append("\n");
        details.append("Buyer: ").append(getReceiptBuyerName(receipt)).append("\n");
        details.append("Processed By: ").append(getReceiptEmployeeName(receipt)).append("\n");
        details.append("Date: ").append(formatDateTime(receipt.getTransacTime())).append("\n");
        details.append("Payment Method: ").append(getReceiptPaymentMethod(receipt)).append("\n\n");
        details.append("PURCHASED ITEMS\n");

        for (ReceiptItem item : receipt.getItems()) {
            details.append(item.getProductName())
                    .append(" (").append(item.getProductCode()).append(")")
                    .append("\n  ")
                    .append(String.format("%.2f kg x PHP %,.2f = PHP %,.2f",
                            item.getQuantityInKG(), item.getUnitPrice(), item.getSubtotal()))
                    .append("\n");
        }

        details.append("\nTotal: ").append(formatMoney(receipt.getTotalAmount())).append("\n");
        details.append("Amount Paid: ").append(formatMoney(receipt.getAmountPaid())).append("\n");

        if ("Cash".equals(receipt.getPaymentMethod())) {
            details.append("Cash Received: ").append(formatMoney(receipt.getCashReceived())).append("\n");
            details.append("Change: ").append(formatMoney(receipt.getChange())).append("\n");
        }

        details.append("Status: ").append(getReceiptStatus(receipt));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Transaction Details");
        alert.setHeaderText("Receipt " + receipt.getTransactionID());
        alert.setContentText(details.toString());
        alert.getDialogPane().setPrefWidth(520);
        alert.showAndWait();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DATE_FORMAT);
    }

    // -----------------------------------------------------------------
    // 5. Footer, refresh, and logout
    // -----------------------------------------------------------------

    private HBox createFooter() {

        Label applicationName =
                new Label("Angkat Fish Port Management System");
        applicationName.getStyleClass().add("manager-footer-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label portalName = new Label("Manager Portal");
        portalName.getStyleClass().add("manager-footer-label");

        HBox footer = new HBox(12, applicationName, spacer, portalName);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 0, 5, 0));
        footer.getStyleClass().add("manager-footer");

        return footer;
    }

    private void refreshDashboard() {
        updateSummaryCards();
        refreshActivityTable();
    }

    private void confirmLogout() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Logout");
        alert.setHeaderText("Log out of Angkat?");
        alert.setContentText("Are you sure you want to return to the login screen?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                if (onLogout != null) {
                    onLogout.run();
                } else {
                    stage.close();
                }
            }
        });
    }
}
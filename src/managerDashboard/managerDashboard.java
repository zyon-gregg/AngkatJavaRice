package managerDashboard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import Products.Product;
import Products.ProductDatabase;
import Products.ProductManagementView;
import inventoryManagementSystem.InventoryGUI;
import inventoryManagementSystem.InventoryOfProduct;
import inventoryManagementSystem.InventorySystem;
import inventoryManagementSystem.StockRecordedChange;
import users.Manager;

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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class managerDashboard {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final Stage stage;
   // private final Manager manager;
    private final String managerName;
    private final String managerID;

    // These are the same backend instances created in Main.java.
    private final ProductDatabase productDatabase;
    private final InventorySystem inventorySystem;

    private BorderPane root;
    private StackPane contentArea;
    private Label pageTitleLabel;
    private String currentPage = "Overview";

    public managerDashboard(Stage stage, String managerName, String managerID, ProductDatabase productDatabase, InventorySystem inventorySystem) {

        this.stage = stage;
        //this.manager = manager; //will store user object soon
        this.managerName = managerName;
        this.managerID = managerID;
        this.productDatabase = productDatabase;
        this.inventorySystem = inventorySystem;
    }

    //shows the manager dashboard
    public void show() {
        root = new BorderPane();
        contentArea = new StackPane();
        pageTitleLabel = new Label("Overview");

        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        root.setCenter(contentArea);

        showOverview();

        Scene scene = new Scene(root, 1250, 780);
        stage.setTitle("Angkat - Manager Dashboard");
        stage.setMinWidth(1050);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }
    

    // ---------------------------------------------------------------------
    // Main window layout
    // ---------------------------------------------------------------------

    private Node createTopBar() {
        Label managerDetails = new Label(managerName + "   |   ID: " + managerID + "   |   Manager");

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshCurrentPage());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(15, pageTitleLabel, spacer, managerDetails, refreshButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));

        return topBar;
    }

    private Node createSidebar() {
        Label applicationName = new Label("ANGKAT");
        Label roleLabel = new Label("MANAGER PANEL");

        Button overviewButton = createNavigationButton("Overview");
        Button productButton = createNavigationButton("Product Management");
        Button inventoryButton = createNavigationButton("Inventory Management");
        Button salesButton = createNavigationButton("Sales & Transactions");
        Button activityButton = createNavigationButton("Activity History");
        Button userButton = createNavigationButton("User Management");
        Button logoutButton = createNavigationButton("Logout");

        overviewButton.setOnAction(event -> showOverview());
        productButton.setOnAction(event -> openProductManagement());
        inventoryButton.setOnAction(event -> openInventoryManagement());
        salesButton.setOnAction(event -> showSalesPlaceholder());
        activityButton.setOnAction(event -> showActivityHistory());
        userButton.setOnAction(event -> showUserManagementPlaceholder());
        logoutButton.setOnAction(event -> confirmLogout());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox sidebar = new VBox(10,
                applicationName,
                roleLabel,
                new Separator(),
                overviewButton,
                productButton,
                inventoryButton,
                salesButton,
                activityButton,
                userButton,
                spacer,
                new Separator(),
                logoutButton);
        sidebar.setPadding(new Insets(20, 12, 20, 12));
        sidebar.setPrefWidth(225);

        return sidebar;
    }

    private Button createNavigationButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        return button;
    }

    private void displayPage(String pageName, Node pageContent) {
        currentPage = pageName;
        pageTitleLabel.setText(pageName);

        ScrollPane scrollPane = new ScrollPane(pageContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        contentArea.getChildren().setAll(scrollPane);
    }

    private void refreshCurrentPage() {
        if ("Overview".equals(currentPage)) {
            showOverview();
        } else if ("Activity History".equals(currentPage)) {
            showActivityHistory();
        }
    }

    // ---------------------------------------------------------------------
    // Overview page
    // ---------------------------------------------------------------------

    private void showOverview() {
    	
    	int totalProducts = productDatabase.getAllProducts().size();
        int totalInventoryRecords = inventorySystem.getAllInventory().size();
        int lowStockCount = inventorySystem.getProductsLowOnStock().size();
        int outOfStockCount = inventorySystem.getProductsOutOfStock().size();
        
        GridPane summaryCards = new GridPane();
        
        summaryCards.setHgap(12);
        summaryCards.setVgap(12);
        summaryCards.add(createSummaryCard("Total Products", totalProducts), 0, 0);
        summaryCards.add(createSummaryCard("Inventory Records", totalInventoryRecords), 1, 0);
        summaryCards.add(createSummaryCard("Low-stock Products", lowStockCount), 2, 0);
        summaryCards.add(createSummaryCard("Out-of-stock Products", outOfStockCount), 3, 0);

        VBox attentionSection = new VBox(8,
                new Label("Inventory Requiring Attention"),
                createInventoryAttentionTable());
        VBox recentActivitySection = new VBox(8,
                new Label("Recent Inventory Changes"),
                createInventoryHistoryTable(6));

        HBox tablesRow = new HBox(15, attentionSection, recentActivitySection);
        HBox.setHgrow(attentionSection, Priority.ALWAYS);
        HBox.setHgrow(recentActivitySection, Priority.ALWAYS);
        attentionSection.setPrefWidth(450);
        recentActivitySection.setPrefWidth(650);

        Label overviewDescription = new Label(
                "Monitor product records, stock levels, and recent inventory activity.");

        VBox page = new VBox(18,
                overviewDescription,
                summaryCards,
                tablesRow);
        page.setPadding(new Insets(22));
        page.setFillWidth(true);

        displayPage("Overview", page);
    }
    
    private VBox createSummaryCard(String title, int value) {
        Label titleLabel = new Label(title);
        Label valueLabel = new Label(String.valueOf(value));

        VBox card = new VBox(8, titleLabel, valueLabel);
        card.setPadding(new Insets(15));
        card.setMinWidth(150);
        card.setPrefWidth(210);
        GridPane.setHgrow(card, Priority.ALWAYS);
        return card;
    }
    
    private TableView<InventoryOfProduct> createInventoryAttentionTable() {
        TableView<InventoryOfProduct> table = new TableView<>();

        TableColumn<InventoryOfProduct, String> productColumn =
                new TableColumn<>("Product");
        productColumn.setCellValueFactory(cell -> {
            Product product = cell.getValue().getProduct();
            return new ReadOnlyStringWrapper(
                    product == null ? "Unknown" : product.getProductName());
        });
        
        TableColumn<InventoryOfProduct, String> stockColumn =
                new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                String.format("%.2f kg", cell.getValue().getProductStockInKG())));

        TableColumn<InventoryOfProduct, String> statusColumn =
                new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cell -> {
            double stock = cell.getValue().getProductStockInKG();
            return new ReadOnlyStringWrapper(
                    stock == 0 ? "Out of stock" : "Low stock");
        });
        
        table.getColumns().addAll(productColumn, stockColumn, statusColumn);

        ArrayList<InventoryOfProduct> attentionItems = new ArrayList<>();
        attentionItems.addAll(inventorySystem.getProductsLowOnStock());
        attentionItems.addAll(inventorySystem.getProductsOutOfStock());
        table.setItems(FXCollections.observableArrayList(attentionItems));
        table.setPlaceholder(new Label("No low-stock or out-of-stock products."));
        table.setPrefHeight(300);

        return table;
    }
    
 // ---------------------------------------------------------------------
    // Activity history
    // ---------------------------------------------------------------------

    private void showActivityHistory() {
        Label description = new Label(
                "Review inventory restocks, sale deductions, and manual stock adjustments.");

        VBox inventoryHistorySection = new VBox(8,
                new Label("Inventory Change History"),
                createInventoryHistoryTable(0));

        Label transactionHistoryNote = new Label(
                "Transaction history will be connected here when the sales/transaction "
                + "backend stores completed receipts and exposes its history records.");
        VBox transactionHistorySection = new VBox(8,
                new Label("Sales & Transaction History"),
                transactionHistoryNote);

        VBox page = new VBox(18,
                description,
                inventoryHistorySection,
                new Separator(),
                transactionHistorySection);
        page.setPadding(new Insets(22));

        displayPage("Activity History", page);
    }
    
    /**
     * @param maximumRows maximum number of records to show; use 0 for all records
     */
    private TableView<StockRecordedChange> createInventoryHistoryTable(int maximumRows) {
        TableView<StockRecordedChange> table = new TableView<>();

        TableColumn<StockRecordedChange, String> idColumn =
                new TableColumn<>("Record ID");
        idColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getRecordId()));

        TableColumn<StockRecordedChange, String> productColumn =
                new TableColumn<>("Product Code");
        productColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getProductCode()));

        TableColumn<StockRecordedChange, String> changeColumn =
                new TableColumn<>("Change Type");
        changeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getChangeType()));

        TableColumn<StockRecordedChange, String> beforeColumn =
                new TableColumn<>("Previous Stock");
        beforeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                String.format("%.2f kg", cell.getValue().getPreviousStock())));

        TableColumn<StockRecordedChange, String> afterColumn =
                new TableColumn<>("New Stock");
        afterColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                String.format("%.2f kg", cell.getValue().getNewStock())));

        TableColumn<StockRecordedChange, String> userColumn =
                new TableColumn<>("Performed By");
        userColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getPerformedBy()));

        TableColumn<StockRecordedChange, String> timeColumn =
                new TableColumn<>("Date and Time");
        timeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                formatDateTime(cell.getValue().getTime())));

        TableColumn<StockRecordedChange, String> reasonColumn =
                new TableColumn<>("Explanation");
        reasonColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getExplanation()));

        table.getColumns().addAll(idColumn, productColumn, changeColumn,
                beforeColumn, afterColumn, userColumn, timeColumn, reasonColumn);

        List<StockRecordedChange> records =
                new ArrayList<>(inventorySystem.getAllHistory());
        records.sort(Comparator.comparing(
                StockRecordedChange::getTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        if (maximumRows > 0 && records.size() > maximumRows) {
            records = new ArrayList<>(records.subList(0, maximumRows));
        }

        table.setItems(FXCollections.observableArrayList(records));
        table.setPlaceholder(new Label("No inventory changes have been recorded."));
        table.setPrefHeight(maximumRows > 0 ? 300 : 420);

        return table;
    }
    
    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMAT);
    }
    
    // ---------------------------------------------------------------------
    // Existing module windows and unfinished module placeholders
    // ---------------------------------------------------------------------

    private void openProductManagement() {
        ProductManagementView view = new ProductManagementView(productDatabase, true);

        Stage productStage = new Stage();
        productStage.initOwner(stage);
        productStage.setTitle("Angkat - Product Management");
        productStage.setScene(new Scene(view, 1000, 560));
        productStage.show();
    }
    
    private void openInventoryManagement() {
        InventoryGUI inventoryGUI = new InventoryGUI(inventorySystem);
        inventoryGUI.setStageComponents();
    }
    
    private void showSalesPlaceholder() {
        showPlaceholderPage(
                "Sales & Transactions",
                "The SalesTransactionSystem is not connected to this dashboard yet. "
                + "Once checkout and receipt-history methods are complete, this page "
                + "can open the POS and transaction history.");
    }
    
    private void showUserManagementPlaceholder() {
        showPlaceholderPage(
                "User Management",
                "Connect this page to your existing user-management screen when it is ready.");
    }
    
    private void showPlaceholderPage(String title, String message) {
        VBox page = new VBox(12, new Label(message));
        page.setPadding(new Insets(22));
        displayPage(title, page);
    }
    
    private void confirmLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Log out of Angkat?");
        alert.setContentText("Your current session will be closed.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Replace this with returning to your Login screen later.
                stage.close();
            }
        });
    }
}


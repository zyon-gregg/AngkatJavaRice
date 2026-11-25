package clerkDashboard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import Products.Product;
import Products.ProductDatabase;
import Products.ProductManagementView;
import inventoryManagementSystem.InventoryGUI;
import inventoryManagementSystem.InventoryOfProduct;
import inventoryManagementSystem.InventorySystem;
import inventoryManagementSystem.StockRecordedChange;
import transaction.SalesTransactionSystem;
import transaction.TransactionView;
import users.Buyer;

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

public class ClerkDashboard {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final Stage stage;
    private final String clerkName;
    private final String clerkID;
    private final String clerkUsername;

    private final ProductDatabase productDatabase;
    private final InventorySystem inventorySystem;
    private final SalesTransactionSystem salesSystem;
    private final List<Buyer> buyers;
    private final Runnable onLogout;

    private BorderPane root;
    private StackPane contentArea;
    private Label pageTitleLabel;
    private String currentPage = "Overview";

    /**
     * Preferred constructor. The username is kept separately from the
     * display name and user ID because it is used in inventory audit records.
     */
    public ClerkDashboard(Stage stage,
                          String clerkName,
                          String clerkID,
                          String clerkUsername,
                          ProductDatabase productDatabase,
                          InventorySystem inventorySystem,
                          SalesTransactionSystem salesSystem,
                          List<Buyer> buyers,
                          Runnable onLogout) {
        this.stage = stage;
        this.clerkName = clerkName;
        this.clerkID = clerkID;
        this.clerkUsername = clerkUsername;
        this.productDatabase = productDatabase;
        this.inventorySystem = inventorySystem;
        this.salesSystem = salesSystem;
        this.buyers = buyers;
        this.onLogout = onLogout;
    }

    /** Backward-compatible overload for any older callers. */
    public ClerkDashboard(Stage stage,
                          String clerkName,
                          String clerkID,
                          ProductDatabase productDatabase,
                          InventorySystem inventorySystem,
                          SalesTransactionSystem salesSystem,
                          List<Buyer> buyers,
                          Runnable onLogout) {
        this(stage, clerkName, clerkID, clerkID,
                productDatabase, inventorySystem, salesSystem, buyers, onLogout);
    }

    public void show() {
        root = new BorderPane();
        contentArea = new StackPane();
        pageTitleLabel = new Label("Overview");

        root.setTop(createTopBar());
        root.setLeft(createSidebar());
        root.setCenter(contentArea);

        showOverview();

        Scene scene = new Scene(root, 1250, 780);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm()); // this links to the css 
        stage.setTitle("Angkat Fish Port - Clerk Dashboard");
        stage.setMinWidth(1050);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    private Node createTopBar() {
        Label clerkDetails = new Label(
                clerkName + "   |   ID: " + clerkID + "   |   Clerk");

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshCurrentPage());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(15,
                pageTitleLabel,
                spacer,
                clerkDetails,
                refreshButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));

        return topBar;
    }

    private Node createSidebar() {
        Label applicationName = new Label("ANGKAT FISH PORT");
        Label roleLabel = new Label("CLERK TERMINAL");

        Button overviewButton = createNavigationButton("Overview");
        Button posButton = createNavigationButton("Point of Sale (POS)");
        Button productCatalogButton = createNavigationButton("Product Catalog");
        Button inventoryButton = createNavigationButton("Check Inventory");
        Button shiftHistoryButton = createNavigationButton("Recent Stock Activity");
        Button logoutButton = createNavigationButton("Logout");

        overviewButton.setOnAction(event -> showOverview());
        posButton.setOnAction(event -> openPointOfSale());
        productCatalogButton.setOnAction(event -> openProductCatalog());
        inventoryButton.setOnAction(event -> openInventoryCheck());
        shiftHistoryButton.setOnAction(event -> showShiftActivity());
        logoutButton.setOnAction(event -> confirmLogout());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox sidebar = new VBox(10,
                applicationName,
                roleLabel,
                new Separator(),
                overviewButton,
                posButton,
                productCatalogButton,
                inventoryButton,
                shiftHistoryButton,
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
        switch (currentPage) {
            case "Overview":
                showOverview();
                break;
            case "Point of Sale (POS)":
                openPointOfSale();
                break;
            case "Product Catalog":
                openProductCatalog();
                break;
            case "Check Inventory":
                openInventoryCheck();
                break;
            case "Recent Stock Activity":
                showShiftActivity();
                break;
        }
    }

    private void showOverview() {
        int availableProducts = productDatabase.getAllProducts().size();
        int lowStockCount = inventorySystem.getProductsLowOnStock().size();
        int outOfStockCount = inventorySystem.getProductsOutOfStock().size();

        GridPane summaryCards = new GridPane();
        summaryCards.setHgap(12);
        summaryCards.setVgap(12);
        summaryCards.add(createSummaryCard("Catalog Varieties", availableProducts), 0, 0);
        summaryCards.add(createSummaryCard("Low-stock Items", lowStockCount), 1, 0);
        summaryCards.add(createSummaryCard("Out-of-stock Items", outOfStockCount), 2, 0);

        Button startSaleBtn = new Button("Start New Sale (POS)");
        startSaleBtn.setOnAction(e -> openPointOfSale());

        Button checkPricesBtn = new Button("View Fish Prices & Catalog");
        checkPricesBtn.setOnAction(e -> openProductCatalog());

        Button verifyStockBtn = new Button("Check Stock Levels");
        verifyStockBtn.setOnAction(e -> openInventoryCheck());

        HBox quickActions = new HBox(12, startSaleBtn, checkPricesBtn, verifyStockBtn);

        VBox stockTableSection = new VBox(8,
                new Label("Current Catch & Stock Levels"),
                createCurrentStockTable());

        VBox lowStockSection = new VBox(8,
                new Label("Low / Out-of-Stock Alerts (Verify Before Bulk Orders)"),
                createInventoryAttentionTable());

        HBox tablesRow = new HBox(15, stockTableSection, lowStockSection);
        HBox.setHgrow(stockTableSection, Priority.ALWAYS);
        HBox.setHgrow(lowStockSection, Priority.ALWAYS);
        stockTableSection.setPrefWidth(600);
        lowStockSection.setPrefWidth(480);

        Label overviewDescription = new Label(
                "Process buyer orders, verify seafood prices, and check available stock in kilograms.");

        VBox page = new VBox(18,
                overviewDescription,
                quickActions,
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
        card.setPrefWidth(220);
        GridPane.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private TableView<InventoryOfProduct> createCurrentStockTable() {
        TableView<InventoryOfProduct> table = new TableView<>();

        TableColumn<InventoryOfProduct, String> productColumn =
                new TableColumn<>("Product");
        productColumn.setCellValueFactory(cell -> {
            Product product = cell.getValue().getProduct();
            if (product == null) {
                return new ReadOnlyStringWrapper("Unknown");
            }

            return new ReadOnlyStringWrapper(product.getProductName());
        });

        TableColumn<InventoryOfProduct, String> stockColumn =
                new TableColumn<>("Available Stock");
        stockColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                String.format("%.2f kg", cell.getValue().getProductStockInKG())));

        TableColumn<InventoryOfProduct, String> statusColumn =
                new TableColumn<>("Availability");
        statusColumn.setCellValueFactory(cell -> {
            double stock = cell.getValue().getProductStockInKG();
            if (stock == 0) {
                return new ReadOnlyStringWrapper("Out of stock");
            } else if (inventorySystem.getProductsLowOnStock().contains(cell.getValue())) {
                return new ReadOnlyStringWrapper("Low stock");
            }
            return new ReadOnlyStringWrapper("In stock");
        });

        table.getColumns().addAll(productColumn, stockColumn, statusColumn);
        table.setItems(FXCollections.observableArrayList(inventorySystem.getAllInventory()));
        table.setPlaceholder(new Label("No inventory records available."));
        table.setPrefHeight(320);

        return table;
    }

    private TableView<InventoryOfProduct> createInventoryAttentionTable() {
        TableView<InventoryOfProduct> table = new TableView<>();

        TableColumn<InventoryOfProduct, String> productColumn =
                new TableColumn<>("Product");
        productColumn.setCellValueFactory(cell -> {
            Product product = cell.getValue().getProduct();
            if (product == null) {
                return new ReadOnlyStringWrapper("Unknown");
            }

            return new ReadOnlyStringWrapper(product.getProductName());
        });

        TableColumn<InventoryOfProduct, String> stockColumn =
                new TableColumn<>("Stock");
        stockColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                String.format("%.2f kg", cell.getValue().getProductStockInKG())));

        TableColumn<InventoryOfProduct, String> statusColumn =
                new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cell -> {
            double stock = cell.getValue().getProductStockInKG();
            if (stock == 0) {
                return new ReadOnlyStringWrapper("Out of stock");
            }

            return new ReadOnlyStringWrapper("Low stock");
        });

        table.getColumns().addAll(productColumn, stockColumn, statusColumn);

        ArrayList<InventoryOfProduct> attentionItems = new ArrayList<>();
        attentionItems.addAll(inventorySystem.getProductsLowOnStock());
        attentionItems.addAll(inventorySystem.getProductsOutOfStock());
        table.setItems(FXCollections.observableArrayList(attentionItems));
        table.setPlaceholder(new Label("All products are well-stocked."));
        table.setPrefHeight(320);

        return table;
    }

    private void openPointOfSale() {
        syncInventoryRecords();
        TransactionView posView = new TransactionView(salesSystem, productDatabase, buyers);
        displayPage("Point of Sale (POS)", posView);
    }

    private void openProductCatalog() {
        ProductManagementView productView = new ProductManagementView(productDatabase, inventorySystem, false);
        displayPage("Product Catalog", productView);
    }

    private void openInventoryCheck() {

        // Clerks can inspect inventory and history but cannot modify stock.
        InventoryGUI inventoryGUI = new InventoryGUI(
                inventorySystem, clerkUsername, false);
        inventoryGUI.setStageComponents();
    }

    private void syncInventoryRecords() {
        for (Product p : productDatabase.getAllProducts()) {
            if (inventorySystem.getProductInventoryByCode(p.getProductCode()) == null) {
                inventorySystem.createInventoryRecord(p);
            }
        }
    }

    private void showShiftActivity() {
        Label description = new Label(
                "View recent stock deductions and restocks.");

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

        TableColumn<StockRecordedChange, String> afterColumn =
                new TableColumn<>("Remaining Stock");
        afterColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                String.format("%.2f kg", cell.getValue().getNewStock())));

        TableColumn<StockRecordedChange, String> timeColumn =
                new TableColumn<>("Date and Time");
        timeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                formatDateTime(cell.getValue().getTime())));

        table.getColumns().addAll(idColumn, productColumn, changeColumn, afterColumn, timeColumn);

        List<StockRecordedChange> records =
                new ArrayList<>(inventorySystem.getAllHistory());
        // Show the newest stock changes first.
        for (int i = 0; i < records.size(); i++) {
            for (int j = i + 1; j < records.size(); j++) {
                LocalDateTime firstTime = records.get(i).getTime();
                LocalDateTime secondTime = records.get(j).getTime();
                boolean moveSecondRecord = false;

                if (firstTime == null && secondTime != null) {
                    moveSecondRecord = true;
                } else if (firstTime != null && secondTime != null
                        && firstTime.isBefore(secondTime)) {
                    moveSecondRecord = true;
                }

                if (moveSecondRecord) {
                    StockRecordedChange temporaryRecord = records.get(i);
                    records.set(i, records.get(j));
                    records.set(j, temporaryRecord);
                }
            }
        }

        table.setItems(FXCollections.observableArrayList(records));
        table.setPlaceholder(new Label("No stock changes recorded yet."));
        table.setPrefHeight(420);

        VBox page = new VBox(18, description, table);
        page.setPadding(new Insets(22));

        displayPage("Recent Stock Activity", page);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.format(DATE_FORMAT);
    }

    private void confirmLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Log out of Angkat Fish Port?");
        alert.setContentText("You will be returned to the login screen.");

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
package clerkDashboard;

import Products.Product;
import Products.ProductDatabase;
import Products.ProductManagementView;
import inventoryManagementSystem.InventoryGUI;
import inventoryManagementSystem.InventoryOfProduct;
import inventoryManagementSystem.InventorySystem;
import inventoryManagementSystem.StockRecordedChange;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import transaction.SalesTransactionSystem;
import transaction.TransactionView;
import users.Buyer;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Main dashboard interface for Clerks in the Angkat Fish Port Management System.
 * Provides navigation to Overview metrics, Point of Sale (POS), read-only Product Catalog,
 * Inventory levels, and Stock Activity logs.
 */
public class ClerkDashboard {

    // Stock quantity threshold (in kg) below which an item is flagged as "Low stock"
    private static final double LOW_STOCK_THRESHOLD = 300.0;

    // Stage and logged-in clerk details
    private final Stage stage;
    private final String clerkFullName;
    private final String clerkId;
    private final String clerkUsername;

    // Shared backend systems passed from Main.java
    private final ProductDatabase products;
    private final InventorySystem inventory;
    private final SalesTransactionSystem sales;
    private final List<Buyer> buyers;
    private final Runnable onLogout;

    // Layout and navigation tracking
    private BorderPane root;
    private Label pageTitleLabel;
    private String activeTab = "OVERVIEW";
    private Stage inventoryWindow;
    private String inventoryPreviousTab;

    // Sidebar navigation buttons
    private Button overviewBtn;
    private Button posBtn;
    private Button catalogBtn;
    private Button inventoryBtn;
    private Button activityBtn;

    /**
     * Initializes the Clerk Dashboard with the active stage, clerk profile, and backend services.
     */
    public ClerkDashboard(
            Stage stage,
            String clerkFullName,
            String clerkId,
            String clerkUsername,
            ProductDatabase products,
            InventorySystem inventory,
            SalesTransactionSystem sales,
            List<Buyer> buyers,
            Runnable onLogout
    ) {
        this.stage = stage;
        this.clerkFullName = clerkFullName;
        this.clerkId = clerkId;
        this.clerkUsername = clerkUsername;
        this.products = products;
        this.inventory = inventory;
        this.sales = sales;
        this.buyers = buyers;
        this.onLogout = onLogout;
    }

    /**
     * Builds the main dashboard layout, loads the CSS stylesheet, and displays it on the stage.
     */
    public void show() {
        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setTop(buildTopBar());

        // Load Overview as the default landing view
        showOverview();

        Scene scene = new Scene(root, 1250, 780);
        URL cssUrl = getClass().getResource("/clerkDashboard/clerkDashboardDesign.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle("Angkat Fish Port - Clerk Dashboard");
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    // -----------------------------------------------------------------
    // SIDEBAR & TOP BAR
    // -----------------------------------------------------------------

    /**
     * Creates the left navigation sidebar with branding, module buttons, and a logout button.
     */
    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(235);
        sidebar.getStyleClass().add("sidebar");

        // Brand header with optional port logo
        HBox brandBox = new HBox(10);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        URL logoUrl = getClass().getResource("/images/logo.png");
        if (logoUrl != null) {
            ImageView logoView = new ImageView(new Image(logoUrl.toExternalForm()));
            logoView.setFitWidth(36);
            logoView.setFitHeight(36);
            logoView.setPreserveRatio(true);
            brandBox.getChildren().add(logoView);
        }

        VBox titleStack = new VBox(2);
        Label brandTitle = new Label("ANGKAT FISH PORT");
        brandTitle.getStyleClass().add("brand-title");
        Label brandSub = new Label("CLERK TERMINAL");
        brandSub.getStyleClass().add("brand-subtitle");
        titleStack.getChildren().addAll(brandTitle, brandSub);
        brandBox.getChildren().add(titleStack);

        Separator sep = new Separator();
        sep.getStyleClass().add("sidebar-divider");
        VBox.setMargin(sep, new Insets(8, 0, 8, 0));

        // Navigation buttons
        overviewBtn = makeNavButton("Overview");
        overviewBtn.setOnAction(e -> showOverview());

        posBtn = makeNavButton("Point of Sale (POS)");
        posBtn.setOnAction(e -> showPOS());

        catalogBtn = makeNavButton("Product Catalog");
        catalogBtn.setOnAction(e -> showCatalog());

        inventoryBtn = makeNavButton("Check Inventory");
        inventoryBtn.setOnAction(e -> showInventory());

        activityBtn = makeNavButton("Recent Stock Activity");
        activityBtn.setOnAction(e -> showStockActivity());

        // Pushes the logout button to the bottom of the sidebar
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.getStyleClass().add("logout-btn");
        logoutBtn.setOnAction(e -> confirmLogout());

        sidebar.getChildren().addAll(
                brandBox, sep,
                overviewBtn, posBtn, catalogBtn, inventoryBtn, activityBtn,
                spacer, logoutBtn
        );

        return sidebar;
    }

    /**
     * Helper to create a full-width styled sidebar navigation button.
     */
    private Button makeNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-btn");
        return btn;
    }

    /**
     * Highlights the currently selected sidebar button and updates the top bar page title.
     */
    private void updateActiveNav(Button selected, String tabKey, String pageTitle) {
        this.activeTab = tabKey;
        if (pageTitleLabel != null) {
            pageTitleLabel.setText(pageTitle);
        }

        for (Button b : new Button[]{overviewBtn, posBtn, catalogBtn, inventoryBtn, activityBtn}) {
            b.getStyleClass().removeAll("nav-btn", "nav-btn-active");
            b.getStyleClass().add(b == selected ? "nav-btn-active" : "nav-btn");
        }
    }

    /**
     * Builds the top header bar showing the current view title, clerk badge, and refresh button.
     */
    private HBox buildTopBar() {
        HBox topBar = new HBox(14);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("topbar");

        pageTitleLabel = new Label("Overview");
        pageTitleLabel.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String displayName = (clerkFullName != null && !clerkFullName.isBlank()) ? clerkFullName : clerkUsername;
        Label userBadge = new Label(displayName + "   |   ID: " + clerkId + "   |   Clerk");
        userBadge.getStyleClass().add("user-badge");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("refresh-btn");
        refreshBtn.setOnAction(e -> refreshCurrentScreen());

        topBar.getChildren().addAll(pageTitleLabel, spacer, userBadge, refreshBtn);
        return topBar;
    }

    /**
     * Reloads whichever screen is currently open to reflect the latest data.
     */
    private void refreshCurrentScreen() {
        switch (activeTab) {
            case "POS" -> showPOS();
            case "CATALOG" -> showCatalog();
            case "INVENTORY" -> showInventory();
            case "ACTIVITY" -> showStockActivity();
            default -> showOverview();
        }
    }

    /**
     * Displays a confirmation prompt before logging out and returning to the login screen.
     */
    private void confirmLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Sign out of Clerk Terminal?");
        alert.setContentText("Any unsaved screen changes will be closed.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            // Close the separate Inventory window before leaving the Clerk Dashboard.
            if (inventoryWindow != null && inventoryWindow.isShowing()) {
                inventoryWindow.close();
            }

            if (onLogout != null) {
                onLogout.run();
            }
        }
    }

    // -----------------------------------------------------------------
    // VIEWS / NAVIGATION
    // -----------------------------------------------------------------

    /**
     * Displays the Overview home screen with quick actions, stock summary cards, and stock tables.
     */
    private void showOverview() {
        updateActiveNav(overviewBtn, "OVERVIEW", "Overview");
        syncInventoryRecords();

        VBox content = new VBox(18);
        content.setPadding(new Insets(22, 26, 22, 26));

        Label subtitle = new Label("Process buyer orders, verify seafood prices, and check available stock in kilograms.");
        subtitle.getStyleClass().add("subtitle-label");

        // Quick-action shortcut buttons
        HBox quickActions = new HBox(12);
        Button startSaleBtn = new Button("Start New Sale (POS)");
        startSaleBtn.getStyleClass().add("action-primary-btn");
        startSaleBtn.setOnAction(e -> showPOS());

        Button viewPricesBtn = new Button("View Fish Prices & Catalog");
        viewPricesBtn.getStyleClass().add("action-secondary-btn");
        viewPricesBtn.setOnAction(e -> showCatalog());

        Button checkStockBtn = new Button("Check Stock Levels");
        checkStockBtn.getStyleClass().add("action-secondary-btn");
        checkStockBtn.setOnAction(e -> showInventory());

        quickActions.getChildren().addAll(startSaleBtn, viewPricesBtn, checkStockBtn);

        // Compute stock counts and populate table rows
        List<Product> allProducts = products.getAllProducts();
        int totalVarieties = allProducts.size();
        int lowStockCount = 0;
        int outOfStockCount = 0;

        ObservableList<StockRow> allRows = FXCollections.observableArrayList();
        ObservableList<StockRow> alertRows = FXCollections.observableArrayList();

        for (Product p : allProducts) {
            InventoryOfProduct inv = inventory.getProductInventoryByCode(p.getProductCode());
            double qty = (inv != null) ? inv.getProductStockInKG() : 0.0;

            String status;
            if (qty <= 0.0) {
                status = "Out of stock";
                outOfStockCount++;
            } else if (qty < LOW_STOCK_THRESHOLD) {
                status = "Low stock";
                lowStockCount++;
            } else {
                status = "In stock";
            }

            StockRow row = new StockRow(
                    p.getProductName(),
                    String.format("₱%.2f / kg", p.getProductRawPrice()),
                    String.format("%.2f kg", qty),
                    status
            );
            allRows.add(row);

            // Add to alerts table if stock is low or depleted
            if (!status.equals("In stock")) {
                alertRows.add(row);
            }
        }

        // Summary KPI cards
        HBox kpiRow = new HBox(16);
        VBox card1 = makeStatCard("Catalog Varieties", String.valueOf(totalVarieties), "stat-value-blue");
        VBox card2 = makeStatCard("Low-stock Items (< 300 kg)", String.valueOf(lowStockCount), "stat-value-amber");
        VBox card3 = makeStatCard("Out-of-stock Items", String.valueOf(outOfStockCount), "stat-value-red");
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        kpiRow.getChildren().addAll(card1, card2, card3);

        // Side-by-side catch overview and low-stock alert tables
        HBox tablesRow = new HBox(18);
        VBox.setVgrow(tablesRow, Priority.ALWAYS);

        VBox leftPanel = buildStockTablePanel("Current Catch & Stock Levels", allRows, true);
        VBox rightPanel = buildStockTablePanel("Low / Out-of-Stock Alerts (Verify Before Bulk Orders)", alertRows, false);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        tablesRow.getChildren().addAll(leftPanel, rightPanel);

        content.getChildren().addAll(subtitle, quickActions, kpiRow, tablesRow);
        root.setCenter(content);
    }

    /**
     * Opens the Point of Sale (POS) transaction screen in the center area.
     */
    private void showPOS() {
        updateActiveNav(posBtn, "POS", "Point of Sale (POS)");
        syncInventoryRecords();
        root.setCenter(new TransactionView(sales, products, buyers));
    }

    /**
     * Opens the Product Catalog in read-only mode (clerks cannot add, edit, or delete products).
     */
    private void showCatalog() {
        updateActiveNav(catalogBtn, "CATALOG", "Product Catalog (Read-Only)");
        root.setCenter(new ProductManagementView(products, inventory, false));
    }

    /**
     * Opens the Inventory view in a separate read-only window for clerks.
     */
    private void showInventory() {

        // InventoryGUI is a standalone window, not a JavaFX Node that can be
        // inserted into the Clerk Dashboard's BorderPane.
        if (inventoryWindow != null && inventoryWindow.isShowing()) {
            updateActiveNav(inventoryBtn, "INVENTORY", "Check Inventory");
            inventoryWindow.toFront();
            return;
        }

        inventoryPreviousTab = activeTab;
        updateActiveNav(inventoryBtn, "INVENTORY", "Check Inventory");
        syncInventoryRecords();

        String actor = clerkUsername;
        if (actor == null || actor.isBlank()) {
            actor = clerkFullName;
        }

        InventoryGUI inventoryGUI = new InventoryGUI(inventory, actor, false);
        inventoryWindow = inventoryGUI.getStage();
        inventoryWindow.initOwner(stage);

        inventoryWindow.setOnHidden(event -> {
            inventoryWindow = null;

            // Return to the dashboard screen that was open before Inventory.
            // Do not reopen Inventory when the separate window is closed.
            if ("INVENTORY".equals(activeTab)) {
                restorePreviousScreen(inventoryPreviousTab);
            }

            inventoryPreviousTab = null;
        });

        inventoryGUI.setStageComponents();
    }

    /** Restores the dashboard view that was active before opening Inventory. */
    private void restorePreviousScreen(String previousTab) {
        if ("POS".equals(previousTab)) {
            showPOS();
        } else if ("CATALOG".equals(previousTab)) {
            showCatalog();
        } else if ("ACTIVITY".equals(previousTab)) {
            showStockActivity();
        } else {
            showOverview();
        }
    }

    /**
     * Displays the chronological audit log of inventory restocks, sales deductions, and adjustments.
     */
    private void showStockActivity() {
        updateActiveNav(activityBtn, "ACTIVITY", "Recent Stock Activity");

        VBox container = new VBox(14);
        container.setPadding(new Insets(22, 26, 22, 26));

        VBox card = new VBox(12);
        card.getStyleClass().add("panel-card");
        VBox.setVgrow(card, Priority.ALWAYS);

        Label title = new Label("Inventory Restock & Deduction Log");
        title.getStyleClass().add("panel-title");

        TableView<StockRecordedChange> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        TableColumn<StockRecordedChange, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTime() != null ? c.getValue().getTime().format(fmt) : "-"
        ));

        TableColumn<StockRecordedChange, String> prodCol = new TableColumn<>("Product");
        prodCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getProduct() != null ? c.getValue().getProduct().getProductName() : c.getValue().getProductCode()
        ));

        TableColumn<StockRecordedChange, String> typeCol = new TableColumn<>("Action");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getChangeType()));

        TableColumn<StockRecordedChange, String> qtyCol = new TableColumn<>("Change (kg)");
        qtyCol.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%+.2f kg", c.getValue().getAmountChanged())
        ));

        TableColumn<StockRecordedChange, String> newStockCol = new TableColumn<>("New Stock (kg)");
        newStockCol.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.2f kg", c.getValue().getNewStock())
        ));

        TableColumn<StockRecordedChange, String> userCol = new TableColumn<>("Recorded By");
        userCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPerformedBy()));

        TableColumn<StockRecordedChange, String> noteCol = new TableColumn<>("Notes");
        noteCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExplanation()));

        table.getColumns().addAll(timeCol, prodCol, typeCol, qtyCol, newStockCol, userCol, noteCol);

        // Sort logs newest first
        List<StockRecordedChange> logs = new ArrayList<>(inventory.getAllHistory());
        logs.sort(Comparator.comparing(StockRecordedChange::getTime, Comparator.nullsLast(Comparator.reverseOrder())));
        table.setItems(FXCollections.observableArrayList(logs));

        card.getChildren().addAll(title, table);
        container.getChildren().add(card);
        root.setCenter(container);
    }

    // -----------------------------------------------------------------
    // UI HELPERS
    // -----------------------------------------------------------------

    /**
     * Builds a styled metric card for the Overview summary row.
     */
    private VBox makeStatCard(String title, String value, String valueStyleClass) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("stat-title");

        Label valLbl = new Label(value);
        valLbl.getStyleClass().add(valueStyleClass);

        card.getChildren().addAll(titleLbl, valLbl);
        return card;
    }

    /**
     * Builds a titled card containing a stock table with color-coded availability status.
     */
    private VBox buildStockTablePanel(String heading, ObservableList<StockRow> rows, boolean includePriceColumn) {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("panel-card");

        Label title = new Label(heading);
        title.getStyleClass().add("panel-title");

        TableView<StockRow> table = new TableView<>(rows);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<StockRow, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().productName));

        table.getColumns().add(nameCol);

        if (includePriceColumn) {
            TableColumn<StockRow, String> priceCol = new TableColumn<>("Selling Price");
            priceCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().price));
            table.getColumns().add(priceCol);
        }

        TableColumn<StockRow, String> stockCol = new TableColumn<>("Available Stock");
        stockCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().stock));

        TableColumn<StockRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Out of stock" -> setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                        case "Low stock" -> setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    }
                }
            }
        });

        table.getColumns().addAll(stockCol, statusCol);
        panel.getChildren().addAll(title, table);
        return panel;
    }

    /**
     * Ensures every product in ProductDatabase has a corresponding record in InventorySystem.
     */
    private void syncInventoryRecords() {
        for (Product p : products.getAllProducts()) {
            if (inventory.getProductInventoryByCode(p.getProductCode()) == null) {
                inventory.createInventoryRecord(p);
            }
        }
    }

    /**
     * Lightweight row model used to display stock levels and status in Overview tables.
     */
    private static class StockRow {
        final String productName;
        final String price;
        final String stock;
        final String status;

        StockRow(String productName, String price, String stock, String status) {
            this.productName = productName;
            this.price = price;
            this.stock = stock;
            this.status = status;
        }
    }
}

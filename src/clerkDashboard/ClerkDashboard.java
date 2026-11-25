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

public class ClerkDashboard {

    private static final double LOW_STOCK_THRESHOLD = 300.0;

    private final Stage stage;
    private final String clerkFullName;
    private final String clerkId;
    private final String clerkUsername;
    private final ProductDatabase products;
    private final InventorySystem inventory;
    private final SalesTransactionSystem sales;
    private final List<Buyer> buyers;
    private final Runnable onLogout;

    private BorderPane root;
    private Label pageTitleLabel;
    private String activeTab = "OVERVIEW";

    private Button overviewBtn;
    private Button posBtn;
    private Button catalogBtn;
    private Button inventoryBtn;
    private Button activityBtn;

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

    public void show() {
        root = new BorderPane();
        root.setLeft(buildSidebar());
        root.setTop(buildTopBar());

        showOverview();

        Scene scene = new Scene(root, 1250, 780);
        URL cssUrl = getClass().getResource("/clerkDashboard/ClerkDashboardDesign.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle("Angkat Fish Port - Clerk Dashboard");
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    // SIDEBAR & TOP BAR

    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(235);
        sidebar.getStyleClass().add("sidebar");

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

    private Button makeNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-btn");
        return btn;
    }

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

    private void refreshCurrentScreen() {
        switch (activeTab) {
            case "POS" -> showPOS();
            case "CATALOG" -> showCatalog();
            case "INVENTORY" -> showInventory();
            case "ACTIVITY" -> showStockActivity();
            default -> showOverview();
        }
    }

    private void confirmLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Sign out of Clerk Terminal?");
        alert.setContentText("Any unsaved screen changes will be closed.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK && onLogout != null) {
            onLogout.run();
        }
    }

    // VIEWS / NAVIGATION

    private void showOverview() {
        updateActiveNav(overviewBtn, "OVERVIEW", "Overview");
        syncInventoryRecords();

        VBox content = new VBox(18);
        content.setPadding(new Insets(22, 26, 22, 26));

        Label subtitle = new Label("Process buyer orders, verify seafood prices, and check available stock in kilograms.");
        subtitle.getStyleClass().add("subtitle-label");

        // Quick Action Buttons
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

        // Metrics Calculation
        List<Product> allProducts = products.getAllProducts();
        int totalVarieties = allProducts.size();
        int lowStockCount = 0;
        int outOfStockCount = 0;

        ObservableList<StockRow> allRows = FXCollections.observableArrayList();
        ObservableList<StockRow> alertRows = FXCollections.observableArrayList();

        for (Product p : allProducts) {
            InventoryOfProduct inv = inventory.getProductInventoryByCode(p.getProductCode());
            double qty = (inv != null) ? inv.getQuantity() : 0.0;

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
                    String.format("₱%.2f / kg", p.getRawPrice()),
                    String.format("%.2f kg", qty),
                    status
            );
            allRows.add(row);

            if (!status.equals("In stock")) {
                alertRows.add(row);
            }
        }

        // KPI Cards Row
        HBox kpiRow = new HBox(16);
        VBox card1 = makeStatCard("Catalog Varieties", String.valueOf(totalVarieties), "stat-value-blue");
        VBox card2 = makeStatCard("Low-stock Items (< 300 kg)", String.valueOf(lowStockCount), "stat-value-amber");
        VBox card3 = makeStatCard("Out-of-stock Items", String.valueOf(outOfStockCount), "stat-value-red");
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        kpiRow.getChildren().addAll(card1, card2, card3);

        // Side-by-Side Tables
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

    private void showPOS() {
        updateActiveNav(posBtn, "POS", "Point of Sale (POS)");
        syncInventoryRecords();
        root.setCenter(new TransactionView(sales, products, buyers));
    }

    private void showCatalog() {
        updateActiveNav(catalogBtn, "CATALOG", "Product Catalog (Read-Only)");
        root.setCenter(new ProductManagementView(products, false));
    }

    private void showInventory() {
        updateActiveNav(inventoryBtn, "INVENTORY", "Check Inventory");
        syncInventoryRecords();
        String actor = (clerkUsername != null && !clerkUsername.isBlank()) ? clerkUsername : clerkFullName;
        root.setCenter(new InventoryGUI(inventory, actor, false));
    }

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

        List<StockRecordedChange> logs = new ArrayList<>(inventory.getStockChangeHistory());
        logs.sort(Comparator.comparing(StockRecordedChange::getTime, Comparator.nullsLast(Comparator.reverseOrder())));
        table.setItems(FXCollections.observableArrayList(logs));

        card.getChildren().addAll(title, table);
        container.getChildren().add(card);
        root.setCenter(container);
    }

    // UI HELPERS

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

    private VBox buildStockTablePanel(String heading, ObservableList<StockRow> rows, boolean includePriceColumn) {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("

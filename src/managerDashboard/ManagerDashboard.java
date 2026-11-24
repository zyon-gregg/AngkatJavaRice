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

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // Window and manager information
    private final Stage stage;
    private final User manager;
    private final Runnable onLogout;

    // Shared backend instances
    private final ProductDatabase productDatabase;
    private final InventorySystem inventorySystem;
    private final SalesTransactionSystem salesSystem;
    private final List<Buyer> buyers;
    private final LoginManager loginManager;

    // Main JavaFX components
    private BorderPane root;
    private VBox mainContent;
    private StackPane activityTableArea;

    // Summary card values, retained so they can be refreshed
    private Label totalProductsValue;
    private Label lowStockValue;
    private Label outOfStockValue;
    private Label inventoryRecordsValue;

    // Currently selected activity table
    private String currentActivity = "INVENTORY";

    public ManagerDashboard(
            Stage stage,
            User manager,
            ProductDatabase productDatabase,
            InventorySystem inventorySystem,
            SalesTransactionSystem salesSystem,
            List<Buyer> buyers,
            Runnable onLogout) {
        this(stage, manager, productDatabase, inventorySystem,
                salesSystem, buyers, null, onLogout);
    }

    public ManagerDashboard(
            Stage stage,
            User manager,
            ProductDatabase productDatabase,
            InventorySystem inventorySystem,
            SalesTransactionSystem salesSystem,
            List<Buyer> buyers,
            LoginManager loginManager,
            Runnable onLogout) {

        this.stage = stage;
        this.manager = manager;
        this.productDatabase = productDatabase;
        this.inventorySystem = inventorySystem;
        this.salesSystem = salesSystem;
        this.buyers = buyers;
        this.loginManager = loginManager;
        this.onLogout = onLogout;
    }

    /** Alias retained so Main.java can call dashboard.show(). */
    public void show() {
        setStageComponents();
    }

    /** Builds and displays the dashboard window. */
    public void setStageComponents() {

        root = new BorderPane();
        mainContent = new VBox(24);
        mainContent.setPadding(new Insets(24));
        mainContent.setFillWidth(true);

        // Add the dashboard sections from top to bottom.
        mainContent.getChildren().addAll(
                createHeader(),
                new Separator(),
                createBusinessOverview(),
                createManagementModules(),
                createSystemActivity(),
                createFooter());

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1250, 850);
        stage.setTitle("Angkat - Manager Dashboard");
        stage.setMinWidth(1050);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    // -----------------------------------------------------------------
    // 1. Header
    // -----------------------------------------------------------------

    /** Uses a fallback when an optional User field has not been assigned yet. */
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

        Label applicationName = new Label("ANGKAT");
        Label applicationSubtitle = new Label("FISH PORT MANAGEMENT SYSTEM");
        VBox branding = new VBox(4, applicationName, applicationSubtitle);

        String displayName = getDisplayValue(
                manager.getName(), manager.getUserName());
        String displayID = getDisplayValue(
                manager.getUserID(), manager.getUserName());

        Label welcomeLabel = new Label("Welcome, " + displayName + "!");
        Label managerDetails = new Label(
                "Manager ID: " + displayID
                + "   |   Username: " + manager.getUserName());
        VBox managerInformation = new VBox(4, welcomeLabel, managerDetails);
        managerInformation.setAlignment(Pos.CENTER_RIGHT);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshDashboard());

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> confirmLogout());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(
                18,
                branding,
                spacer,
                managerInformation,
                refreshButton,
                logoutButton);

        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // -----------------------------------------------------------------
    // 2. Business overview
    // -----------------------------------------------------------------

    private VBox createBusinessOverview() {

        Label sectionTitle = new Label("Business Overview");

        totalProductsValue = new Label("0");
        lowStockValue = new Label("0");
        outOfStockValue = new Label("0");
        inventoryRecordsValue = new Label("0");

        GridPane cards = new GridPane();
        cards.setHgap(14);
        cards.setVgap(14);

        cards.add(createSummaryCard("Total Products", totalProductsValue), 0, 0);
        cards.add(createSummaryCard("Low-stock Products", lowStockValue), 1, 0);
        cards.add(createSummaryCard("Out-of-stock Products", outOfStockValue), 2, 0);
        cards.add(createSummaryCard("Inventory Records", inventoryRecordsValue), 3, 0);

        updateSummaryCards();

        VBox section = new VBox(12, sectionTitle, cards);
        return section;
    }

    private VBox createSummaryCard(String title, Label value) {

        Label titleLabel = new Label(title);

        VBox card = new VBox(10, titleLabel, value);
        card.setPadding(new Insets(18));
        card.setMinHeight(105);
        card.setMaxWidth(Double.MAX_VALUE);

        GridPane.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void updateSummaryCards() {

        totalProductsValue.setText(
                String.valueOf(productDatabase.getAllProducts().size()));

        lowStockValue.setText(
                String.valueOf(inventorySystem.getProductsLowOnStock().size()));

        outOfStockValue.setText(
                String.valueOf(inventorySystem.getProductsOutOfStock().size()));

        inventoryRecordsValue.setText(
                String.valueOf(inventorySystem.getAllInventory().size()));
    }

    // -----------------------------------------------------------------
    // 3. Management module buttons
    // -----------------------------------------------------------------

    private VBox createManagementModules() {

        Label sectionTitle = new Label("Management Modules");

        Button productButton = createModuleButton(
                "Product Management",
                "Manage seafood products and product information.");

        Button inventoryButton = createModuleButton(
                "Inventory Management",
                "Monitor stock, restock products, and adjust quantities.");

        Button salesButton = createModuleButton(
                "Sales & Transactions",
                "Open the sales terminal and review payment records.");

        Button userButton = createModuleButton(
                "User Management",
                "Review pending registrations and manage accounts.");

        productButton.setOnAction(event -> openProductManagement());
        inventoryButton.setOnAction(event -> openInventoryManagement());
        salesButton.setOnAction(event -> openSalesManagement());
        userButton.setOnAction(event -> openUserManagement());

        GridPane moduleGrid = new GridPane();
        moduleGrid.setHgap(14);
        moduleGrid.setVgap(14);

        moduleGrid.add(productButton, 0, 0);
        moduleGrid.add(inventoryButton, 1, 0);
        moduleGrid.add(salesButton, 0, 1);
        moduleGrid.add(userButton, 1, 1);

        moduleGrid.getColumnConstraints().addAll(
                createEqualColumn(), createEqualColumn());

        VBox section = new VBox(12, sectionTitle, moduleGrid);
        return section;
    }

    private javafx.scene.layout.ColumnConstraints createEqualColumn() {
        javafx.scene.layout.ColumnConstraints column =
                new javafx.scene.layout.ColumnConstraints();
        column.setPercentWidth(50);
        column.setHgrow(Priority.ALWAYS);
        return column;
    }

    private Button createModuleButton(String title, String description) {

        Label titleLabel = new Label(title);
        Label descriptionLabel = new Label(description);
        Label actionLabel = new Label("OPEN MODULE  >");

        VBox buttonContent = new VBox(
                9,
                titleLabel,
                descriptionLabel,
                actionLabel);

        Button button = new Button();
        button.setGraphic(buttonContent);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(125);
        button.setPrefHeight(135);

        return button;
    }

    private void openProductManagement() {

        ProductManagementView productView =
                new ProductManagementView(productDatabase, inventorySystem, true);

        Stage productStage = new Stage();
        productStage.initOwner(stage);
        productStage.setTitle("Angkat - Product Management");
        productStage.setScene(new Scene(productView, 1000, 560));
        productStage.show();
    }

    private void openInventoryManagement() {

        InventoryGUI inventoryGUI = new InventoryGUI(
                inventorySystem, manager.getUserName(), true);
        inventoryGUI.setStageComponents();
    }

    private void openSalesManagement() {

        TransactionView transactionView =
                new TransactionView(salesSystem, productDatabase, buyers);

        Stage transactionStage = new Stage();
        transactionStage.initOwner(stage);
        transactionStage.setTitle("Angkat - Sales & Transactions");
        transactionStage.setScene(new Scene(transactionView, 1150, 700));
        transactionStage.show();
    }

    private void openUserManagement() {
        if (loginManager == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Management");
            alert.setHeaderText("Login manager is not connected.");
            alert.setContentText("Please connect LoginManager in Main.java.");
            alert.initOwner(stage);
            alert.showAndWait();
            return;
        }

        UserManagementView userManagementView =
                new UserManagementView(loginManager, manager);

        Stage userStage = new Stage();
        userStage.initOwner(stage);
        userStage.setTitle("Angkat - User Management");
        userStage.setScene(new Scene(userManagementView, 1050, 760));
        userStage.show();
    }

    // -----------------------------------------------------------------
    // 4. System activity
    // -----------------------------------------------------------------

    private VBox createSystemActivity() {

        Label sectionTitle = new Label("System Activity");

        ToggleGroup activityGroup = new ToggleGroup();

        ToggleButton inventoryButton = new ToggleButton("Inventory Changes");
        ToggleButton transactionButton = new ToggleButton("Transactions");

        inventoryButton.setToggleGroup(activityGroup);
        transactionButton.setToggleGroup(activityGroup);
        inventoryButton.setSelected(true);

        HBox selectors = new HBox(10, inventoryButton, transactionButton);
        selectors.setAlignment(Pos.CENTER_LEFT);

        activityTableArea = new StackPane();
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
        return section;
    }

    private void refreshActivityTable() {

        if (activityTableArea == null) {
            return;
        }

        if ("INVENTORY".equals(currentActivity)) {
            activityTableArea.getChildren().setAll(createInventoryHistoryTable(8));
        } else {
            activityTableArea.getChildren().setAll(createTransactionHistoryPlaceholder());
        }
    }

    /**
     * Shows the latest inventory changes.
     * maximumRows = 0 means show all history records.
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

        TableColumn<StockRecordedChange, String> employeeColumn =
                new TableColumn<>("Performed By");
        employeeColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getPerformedBy()));

        TableColumn<StockRecordedChange, String> dateColumn =
                new TableColumn<>("Date and Time");
        dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                formatDateTime(cell.getValue().getTime())));

        TableColumn<StockRecordedChange, String> explanationColumn =
                new TableColumn<>("Explanation");
        explanationColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getExplanation()));

        table.getColumns().addAll(
                idColumn,
                productColumn,
                changeColumn,
                beforeColumn,
                afterColumn,
                employeeColumn,
                dateColumn,
                explanationColumn);

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

        if (maximumRows > 0 && records.size() > maximumRows) {
            records = new ArrayList<>(records.subList(0, maximumRows));
        }

        table.setItems(FXCollections.observableArrayList(records));
        table.setPlaceholder(new Label("No inventory changes have been recorded."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(350);

        return table;
    }

    /**
     * Transaction history is a placeholder until Receipt.java's getters and
     * the latest SalesTransactionSystem history API are confirmed.
     */
    private Node createTransactionHistoryPlaceholder() {

        Label message = new Label(
                "Transaction history will appear here once the Receipt "
                + "fields and transaction-history methods are connected.");

        Button openTransactionsButton = new Button("Open Sales & Transactions");
        openTransactionsButton.setOnAction(event -> openSalesManagement());

        VBox placeholder = new VBox(12, message, openTransactionsButton);
        placeholder.setAlignment(Pos.CENTER_LEFT);
        placeholder.setPadding(new Insets(20));
        placeholder.setMinHeight(150);

        return placeholder;
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

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label portalName = new Label("Manager Portal");

        HBox footer = new HBox(12, applicationName, spacer, portalName);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 0, 5, 0));

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
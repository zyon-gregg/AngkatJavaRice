package managerDashboard;

import login.LoginManager;
import users.User;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

public class UserManagementView extends BorderPane {

    private final LoginManager loginManager;
    private final User currentManager;

    private TableView<User> pendingTable;
    private TableView<User> accountTable;
    private Label messageLabel;

    public UserManagementView(LoginManager loginManager, User currentManager) {
        this.loginManager = loginManager;
        this.currentManager = currentManager;

        setPadding(new Insets(20));
        setTop(createHeader());
        setCenter(createContent());

        refreshTables();
    }

    private VBox createHeader() {
        Label title = new Label("User Management");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        Label subtitle = new Label(
                "Review registration requests and manage registered accounts.");

        messageLabel = new Label("Select an account to perform an action.");
        messageLabel.setStyle("-fx-text-fill: #555555;");

        VBox header = new VBox(6, title, subtitle, messageLabel);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private VBox createContent() {
        VBox content = new VBox(18);

        Label pendingTitle = new Label("Pending Registration Requests");
        pendingTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        pendingTable = createPendingTable();

        Button approveClerkButton = new Button("Approve as Clerk");
        Button approveManagerButton = new Button("Approve as Manager");
        Button rejectButton = new Button("Reject Request");
        Button refreshPendingButton = new Button("Refresh");

        approveClerkButton.setOnAction(event -> approveSelectedUser("CLERK"));
        approveManagerButton.setOnAction(event -> approveSelectedUser("MANAGER"));
        rejectButton.setOnAction(event -> rejectSelectedUser());
        refreshPendingButton.setOnAction(event -> refreshTables());

        HBox pendingButtons = new HBox(10,
                approveClerkButton,
                approveManagerButton,
                rejectButton,
                refreshPendingButton);
        pendingButtons.setAlignment(Pos.CENTER_LEFT);

        VBox pendingSection = new VBox(10,
                pendingTitle, pendingTable, pendingButtons);

        Label accountTitle = new Label("Registered Accounts");
        accountTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        accountTable = createAccountTable();

        Button deactivateButton = new Button("Deactivate Account");
        Button activateButton = new Button("Activate Account");
        Button refreshAccountsButton = new Button("Refresh");

        deactivateButton.setOnAction(event -> deactivateSelectedUser());
        activateButton.setOnAction(event -> activateSelectedUser());
        refreshAccountsButton.setOnAction(event -> refreshTables());

        HBox accountButtons = new HBox(10,
                deactivateButton,
                activateButton,
                refreshAccountsButton);
        accountButtons.setAlignment(Pos.CENTER_LEFT);

        VBox accountSection = new VBox(10,
                accountTitle, accountTable, accountButtons);

        content.getChildren().addAll(pendingSection, accountSection);
        VBox.setVgrow(accountTable, Priority.ALWAYS);

        return content;
    }

    private TableView<User> createPendingTable() {
        TableView<User> table = new TableView<User>();

        TableColumn<User, String> nameColumn = new TableColumn<User, String>("Full Name");
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getDisplayValue(cell.getValue().getName())));

        TableColumn<User, String> usernameColumn =
                new TableColumn<User, String>("Username");
        usernameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getDisplayValue(cell.getValue().getUserName())));

        TableColumn<User, String> roleColumn = new TableColumn<User, String>("Requested Role");
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getDisplayValue(cell.getValue().getUserAccessLevel())));

        table.getColumns().addAll(nameColumn, usernameColumn, roleColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(190);
        table.setPlaceholder(new Label("There are no pending registration requests."));

        return table;
    }

    private TableView<User> createAccountTable() {
        TableView<User> table = new TableView<User>();

        TableColumn<User, String> idColumn = new TableColumn<User, String>("User ID");
        idColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getDisplayValue(cell.getValue().getUserID())));

        TableColumn<User, String> nameColumn = new TableColumn<User, String>("Full Name");
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getDisplayValue(cell.getValue().getName())));

        TableColumn<User, String> usernameColumn =
                new TableColumn<User, String>("Username");
        usernameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getDisplayValue(cell.getValue().getUserName())));

        TableColumn<User, String> roleColumn = new TableColumn<User, String>("Role");
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                getDisplayValue(cell.getValue().getUserAccessLevel())));

        TableColumn<User, String> statusColumn = new TableColumn<User, String>("Status");
        statusColumn.setCellValueFactory(cell -> {
            String status = "INACTIVE";

            if (cell.getValue().getActivated()) {
                status = "ACTIVE";
            }

            return new ReadOnlyStringWrapper(status);
        });

        table.getColumns().addAll(
                idColumn, nameColumn, usernameColumn, roleColumn, statusColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(230);
        table.setPlaceholder(new Label("There are no registered accounts."));

        return table;
    }

    private void refreshTables() {
        ObservableList<User> pendingUsers = FXCollections.observableArrayList();
        for (User user : loginManager.getPending()) {
            if (user != null) {
                pendingUsers.add(user);
            }
        }
        pendingTable.setItems(pendingUsers);

        ObservableList<User> registeredUsers = FXCollections.observableArrayList();
        for (User user : loginManager.getAccounts()) {
            if (user != null) {
                registeredUsers.add(user);
            }
        }
        accountTable.setItems(registeredUsers);
    }

    private void approveSelectedUser(String role) {
        User selectedUser = pendingTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            messageLabel.setText("Select a pending registration first.");
            return;
        }

        boolean approved = loginManager.approvePendingUser(selectedUser, role);

        if (approved) {
            messageLabel.setText("Account approved as " + role + ".");
        } else {
            messageLabel.setText("The registration could not be approved.");
        }

        refreshTables();
    }

    private void rejectSelectedUser() {
        User selectedUser = pendingTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            messageLabel.setText("Select a pending registration first.");
            return;
        }

        boolean rejected = loginManager.rejectPendingUser(selectedUser);

        if (rejected) {
            messageLabel.setText("Registration request rejected.");
        } else {
            messageLabel.setText("The registration request could not be rejected.");
        }

        refreshTables();
    }

    private void deactivateSelectedUser() {
        User selectedUser = accountTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            messageLabel.setText("Select a registered account first.");
            return;
        }

        if (selectedUser.getUserName().equalsIgnoreCase(currentManager.getUserName())) {
            messageLabel.setText("You cannot deactivate your own Manager account.");
            return;
        }

        boolean deactivated = loginManager.deactivateAccount(selectedUser);

        if (deactivated) {
            messageLabel.setText("Account deactivated.");
        } else {
            messageLabel.setText("The account could not be deactivated.");
        }

        refreshTables();
    }

    private void activateSelectedUser() {
        User selectedUser = accountTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            messageLabel.setText("Select a registered account first.");
            return;
        }

        boolean activated = loginManager.activateAccount(selectedUser);

        if (activated) {
            messageLabel.setText("Account activated.");
        } else {
            messageLabel.setText("The account could not be activated.");
        }

        refreshTables();
    }

    private String getDisplayValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Not assigned";
        }

        return value;
    }

}
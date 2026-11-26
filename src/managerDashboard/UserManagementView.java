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
	
	//the GUI for managing account requests by Managers

	//imported the back end of login, as well as the manager
    private final LoginManager loginManager;
    private final User currentManager;

    //the tables for pending and current accounts
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

    private VBox createHeader() { //method for creating the header of the user management window
    	
    	//the title
        Label title = new Label("User Management");
        title.getStyleClass().add("header-label");
        
        //The subtitle
        Label subtitle = new Label("Approve or disapprove pending accounts. Manage current accounts as well!");

        //message label
        messageLabel = new Label("Select an account to perform an action.");
        messageLabel.setStyle("-fx-text-fill: #555555;");

        //vBox as the layout of the header
        VBox header = new VBox(6, title, subtitle, messageLabel);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    private VBox createContent() { //creating the main window
        VBox content = new VBox(18);

        //title for pending
        Label pendingTitle = new Label("Pending Account requests");
        pendingTitle.getStyleClass().add("sub-header");
        
        pendingTable = createPendingTable();

        //the buttons
        
        //approve the request as clerk button
        Button approveClerkButton = new Button("Approve as Clerk");
        approveClerkButton.setOnAction(event -> approveSelectedUser("CLERK"));
        
        //approve the request as manager button
        Button approveManagerButton = new Button("Approve as Manager");
        approveManagerButton.setOnAction(event -> approveSelectedUser("MANAGER"));
        
        
        //reject account button
        Button rejectButton = new Button("Reject Request");
        rejectButton.setOnAction(event -> rejectSelectedUser());
        
        //refresh the pending table button
        Button refreshPendingButton = new Button("Refresh");
        refreshPendingButton.setOnAction(event -> refreshTables());

        //laid them out in a horizontal order
        HBox pendingButtons = new HBox(10, approveClerkButton, approveManagerButton, rejectButton, refreshPendingButton);
        pendingButtons.setAlignment(Pos.CENTER_LEFT);

        //pending section uses the vBox layout
        VBox pendingSection = new VBox(10, pendingTitle, pendingTable, pendingButtons);

        Label accountTitle = new Label("Registered Accounts");
        accountTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        accountTable = createAccountTable();
        
        //the buttons for the managing registered account portion
        //deactivate button
        Button deactivateButton = new Button("Deactivate Account");
        deactivateButton.setOnAction(event -> deactivateSelectedUser());
        
        //activate account button
        Button activateButton = new Button("Activate Account");
        activateButton.setOnAction(event -> activateSelectedUser());
        
        //refresh
        Button refreshAccountsButton = new Button("Refresh");
        refreshAccountsButton.setOnAction(event -> refreshTables());

        HBox accountButtons = new HBox(10, deactivateButton, activateButton, refreshAccountsButton);
        accountButtons.setAlignment(Pos.CENTER_LEFT);

        VBox accountSection = new VBox(10, accountTitle, accountTable, accountButtons);

        content.getChildren().addAll(pendingSection, accountSection);
        VBox.setVgrow(accountTable, Priority.ALWAYS);

        return content;
    }

    private TableView<User> createPendingTable() { //creates a table view for pending accounts table
    	
        TableView<User> table = new TableView<User>();

        //column for names
        TableColumn<User, String> nameColumn = new TableColumn<User, String>("Full Name");
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(getDisplayValue(cell.getValue().getName())));

        //column for username associated with the pending account
        TableColumn<User, String> usernameColumn = new TableColumn<User, String>("Username");
        usernameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(getDisplayValue(cell.getValue().getUserName())));

        //column for the role requested by the pending account
        TableColumn<User, String> roleColumn = new TableColumn<User, String>("Requested Role");
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(getDisplayValue(cell.getValue().getUserAccessLevel())));

        //adding the columns
        table.getColumns().addAll(nameColumn, usernameColumn, roleColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(190);
        table.setPlaceholder(new Label("No pending account requests so far!"));

        return table;
    }

    private TableView<User> createAccountTable() { //this one creates the table view for current accounts within the angkat system
    	
    	TableView<User> table = new TableView<User>();

    	//column for user ID
        TableColumn<User, String> idColumn = new TableColumn<User, String>("User ID");
        idColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(getDisplayValue(cell.getValue().getUserID())));

        //column for names
        TableColumn<User, String> nameColumn = new TableColumn<User, String>("Full Name");
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(getDisplayValue(cell.getValue().getName())));

        //column for username associated with the account
        TableColumn<User, String> usernameColumn = new TableColumn<User, String>("Username");
        usernameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(getDisplayValue(cell.getValue().getUserName())));

        //columns for role of the account
        TableColumn<User, String> roleColumn = new TableColumn<User, String>("Role");
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(getDisplayValue(cell.getValue().getUserAccessLevel())));

        //Columns for the status
        TableColumn<User, String> statusColumn = new TableColumn<User, String>("Status");
        statusColumn.setCellValueFactory(cell -> {
            String status = "INACTIVE";

            if (cell.getValue().getActivated()) {
                status = "ACTIVE";
            }

            return new ReadOnlyStringWrapper(status);
        });
        
        //no column for the passwords for privacy

        table.getColumns().addAll(idColumn, nameColumn, usernameColumn, roleColumn, statusColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(230);
        table.setPlaceholder(new Label("There are no registered accounts."));

        return table;
    }

    private void refreshTables() {//refreshes the tables
    	
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

    private void approveSelectedUser(String role) {//method for approving an account in pending acounts
        User selectedUser = pendingTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) { //ensures a pending account is selected
        	Label  messageLabel = new Label("Select a pending registration first.");
        	messageLabel.getStyleClass().add("sub-header");
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

    private void rejectSelectedUser() {//method for rejecting a pending account
    	
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

    private void deactivateSelectedUser() { //method for the deactivating an account
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

    private void activateSelectedUser() { //method for activating a selected user
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
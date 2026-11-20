package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Login screen. It does not know who the users are: it asks the authenticator
 * (username, password) -> role, or null if the login is wrong.
 * On success it calls onSuccess(username, role).
 */
public class LoginView extends VBox {
//
	private final TextField usernameField = new TextField();
	private final PasswordField passwordField = new PasswordField();
	private final Label errorLabel = new Label();
	//Random Comment
	public LoginView(BiFunction<String, String, String> authenticator, BiConsumer<String, String> onSuccess) {
		setAlignment(Pos.CENTER);
		setSpacing(14);
		setPadding(new Insets(40));

		Label title = new Label("Angkat");
		title.setStyle("-fx-font-size: 40px; -fx-font-weight: bold;");

		Label subtitle = new Label("Sign in to continue");
		subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");

		usernameField.setPromptText("Username");
		passwordField.setPromptText("Password");
		usernameField.setMaxWidth(280);
		passwordField.setMaxWidth(280);

		errorLabel.setStyle("-fx-text-fill: #c0392b;");
		errorLabel.setManaged(false);
		errorLabel.setVisible(false);

		Button loginButton = new Button("Log In");
		loginButton.setPrefSize(280, 44);
		loginButton.setDefaultButton(true); // Enter key triggers it

		loginButton.setOnAction(e -> {
			String username = usernameField.getText().trim();
			String role = authenticator.apply(username, passwordField.getText());
			if (role == null) {
				showError("Incorrect username or password.");
				passwordField.clear();
			} else {
				onSuccess.accept(username, role);
			}
		});

		VBox.setMargin(usernameField, new Insets(20, 0, 0, 0));
		getChildren().addAll(title, subtitle, usernameField, passwordField, errorLabel, loginButton);
	}

	private void showError(String message) {
		errorLabel.setText(message);
		errorLabel.setManaged(true);
		errorLabel.setVisible(true);
	}
}
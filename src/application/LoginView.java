package application;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class LoginView extends VBox {

    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label errorLabel = new Label();

    public LoginView(BiFunction<String, String, String> authenticator,
                     BiConsumer<String, String> onSuccess) {
        this(authenticator, onSuccess, null);
    }

    public LoginView(BiFunction<String, String, String> authenticator,
                     BiConsumer<String, String> onSuccess,
                     Runnable onRegister) {
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
        loginButton.setDefaultButton(true);

        loginButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String role = authenticator.apply(username, passwordField.getText());

            if (role == null) {
                showError("Incorrect login details or account awaiting approval.");
                passwordField.clear();
            } else {
                onSuccess.accept(username, role);
            }
        });

        Button registerButton = new Button("Request an Account");
        registerButton.setPrefWidth(280);
        registerButton.setOnAction(event -> {
            if (onRegister != null) {
                onRegister.run();
            }
        });

        VBox.setMargin(usernameField, new Insets(20, 0, 0, 0));
        getChildren().addAll(
                title, subtitle, usernameField, passwordField,
                errorLabel, loginButton, registerButton);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }
}
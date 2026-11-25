package application;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView extends StackPane {

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
        

        Image backgroundImage = new Image(getClass().getResourceAsStream("/images/background.png"));
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, false, true);
        BackgroundImage background = new BackgroundImage(
                backgroundImage, 
                BackgroundRepeat.NO_REPEAT, 
                BackgroundRepeat.NO_REPEAT, 
                BackgroundPosition.CENTER, 
                backgroundSize
        );
        this.setBackground(new Background(background));

 
        VBox loginCard = new VBox(14);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(40));
        loginCard.setMaxSize(380, 550); 
        loginCard.getStyleClass().add("login-card"); // Connects to your CSS white floating card

 
        Image logoImage = new Image(getClass().getResourceAsStream("/images/logo.png"));
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(180); // Adjust size as needed
        logoView.setPreserveRatio(true);

        Label title = new Label("Angkat");
        title.getStyleClass().add("header-label"); // Uses your CSS theme

        Label subtitle = new Label("Sign in to continue");
        subtitle.getStyleClass().add("sub-header");

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        usernameField.setMaxWidth(280);
        passwordField.setMaxWidth(280);

        errorLabel.getStyleClass().add("warning-label");
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
        registerButton.getStyleClass().add("button-secondary"); // Uses CSS outline style
        
        registerButton.setOnAction(event -> {
            if (onRegister != null) {
                onRegister.run();
            }
        });

        VBox.setMargin(usernameField, new Insets(20, 0, 0, 0));
        

        loginCard.getChildren().addAll(
                logoView, // Add logo to the top of the card
                title, 
                subtitle, 
                usernameField, 
                passwordField,
                errorLabel, 
                loginButton, 
                registerButton
        );

        getChildren().add(loginCard);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }
}
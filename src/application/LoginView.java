package application;


//for the Logic & Callbacks (Delegating authentication to Main.java)
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

//for the Layout & Positioning (Spacing, alignment, and layering)
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// for the UI Controls (Interactive elements for the user)
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

//for the Images & Backgrounds (Visuals for the fish theme)
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

/*
 * Basically this loginview serves as the passageway or gate of the app's UI.  through this we can access manage's DB and clerk's DB
 * it extends as a stackpane so we can layer a background image behind.
 *
 */

public class LoginView extends StackPane {
    // This are UI components that need to be accessed by multiple methods (like clearing the password on error)
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label errorLabel = new Label();

    //Convenience constructor for when the user doesn't need to register a new account.
    public LoginView(BiFunction<String, String, String> authenticator,
                     BiConsumer<String, String> onSuccess) {
        this(authenticator, onSuccess, null);
    }

    
    /**
     * Main constructor for the Login Screen.
     * @param authenticator A function from Main.java that checks the DB and returns the user role (or null if failed).
     * @param onSuccess A function from Main.java that tells the app what to do if the login works.
     * @param onRegister A function that opens the registration dialog.
     */
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

        // Create a VBox to hold all the input fields and buttons. 
        // The "login-card" CSS class gives it the white, floating, shadowed look.

        VBox loginCard = new VBox(14);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(40));
        loginCard.setMaxSize(380, 550); 
        loginCard.getStyleClass().add("login-card"); // Connects to your CSS white floating card
       
        // Load the circular fish logo and constrain its width so it looks good on the card.
        Image logoImage = new Image(getClass().getResourceAsStream("/images/logo.png"));
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(180); // Adjust size as needed
        logoView.setPreserveRatio(true);

        
        // Title and subtitle.
        Label title = new Label("Angkat");
        title.getStyleClass().add("header-label"); // Uses your CSS theme

        Label subtitle = new Label("Sign in to continue");
        subtitle.getStyleClass().add("sub-header");

        //the input fields for the user to log-in
        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        usernameField.setMaxWidth(280);
        passwordField.setMaxWidth(280);
        
     // We use setManaged(false) so that when it's hidden, it doesn't take up empty space in the VBox.
        errorLabel.getStyleClass().add("warning-label");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        //this is the button and button logic
        Button loginButton = new Button("Log In");
        loginButton.setPrefSize(280, 44);
        loginButton.setDefaultButton(true);  // Allows pressing "Enter" on the keyboard to log in
  
        //the authentication process
        loginButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            
         // Calling the external authenticator function passed in from Main.java
            String role = authenticator.apply(username, passwordField.getText());

            if (role == null) {
            	// this means failed and Show error and wipe the password field for security
                showError("Incorrect login details or account awaiting approval.");
                passwordField.clear();
            } else {
            	// Success then Hand control back to Main.java to load the dashboard
                onSuccess.accept(username, role);
            }
        });

        Button registerButton = new Button("Request an Account");
        registerButton.setPrefWidth(280);
        registerButton.getStyleClass().add("button-secondary"); // Uses CSS outline style
        
        
        // Only trigger the registration dialog if the function was actually provided
        registerButton.setOnAction(event -> {
            if (onRegister != null) {
                onRegister.run();
            }
        });

        //assembling the layout 
        //pushing the input fields down slightly so they are not too close to the subtitle.
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
        // Finally, add the card to the StackPane (which already holds the background)
        getChildren().add(loginCard);
    }

    /**
     * Helper method to reveal the error message smoothly.
     * Setting setManaged(true) ensures the UI adjusts to make room for the text.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }
}
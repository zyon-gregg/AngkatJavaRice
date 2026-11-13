package application;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Handles window switching. The app has ONE Scene, and this class swaps what is shown inside it.
 */
public class SceneManager {

	private final Scene scene;

	public SceneManager(Stage stage, double width, double height) {
		this.scene = new Scene(new BorderPane(), width, height);
		stage.setScene(scene);
	}

	/** Replaces whatever is on screen with this screen. */
	public void setRoot(Parent screen) {
		scene.setRoot(screen);
	}

	/** Shows a screen with a "Back to Menu" bar on top, so the screens themselves need no back button. */
	public void showWithBackButton(Parent screen, Runnable onBack) {
		Button back = new Button("Back to Menu");
		back.setOnAction(e -> onBack.run());

		HBox bar = new HBox(back);
		bar.setPadding(new Insets(10, 16, 0, 16));

		BorderPane wrapper = new BorderPane();
		wrapper.setTop(bar);
		wrapper.setCenter(screen);
		scene.setRoot(wrapper);
	}
}
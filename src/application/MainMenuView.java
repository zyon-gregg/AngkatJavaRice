package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Menu screen with one button per module.
 * It does not know how screens are opened: it just runs the actions it is given,
 * so the same class can be reused later (for example inside a dashboard).
 */
public class MainMenuView extends VBox {

	public MainMenuView(Runnable onProducts, Runnable onInventory, Runnable onTransactions) {
		setAlignment(Pos.CENTER);
		setSpacing(18);
		setPadding(new Insets(40));

		Label title = new Label("Angkat");
		title.setStyle("-fx-font-size: 40px; -fx-font-weight: bold;");

		Label subtitle = new Label("Fish Port Management System");
		subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");

		Button productsButton = menuButton("Product Management", onProducts);
		Button inventoryButton = menuButton("Inventory Management", onInventory);
		Button transactionsButton = menuButton("Transactions", onTransactions);
		VBox.setMargin(productsButton, new Insets(24, 0, 0, 0));

		getChildren().addAll(title, subtitle, productsButton, inventoryButton, transactionsButton);
	}

	/** Builds every menu button the same way. */
	private Button menuButton(String text, Runnable action) {
		Button button = new Button(text);
		button.setPrefSize(280, 56);
		button.setStyle("-fx-font-size: 16px;");
		button.setOnAction(e -> action.run());
		return button;
	}
}
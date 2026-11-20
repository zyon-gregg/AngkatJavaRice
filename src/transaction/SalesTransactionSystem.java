package transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import Products.Product;
import Products.ProductDatabase;
import inventoryManagementSystem.InventoryOfProduct;
import inventoryManagementSystem.InventorySystem;
import users.Buyer;
import users.BuyerDatabase;
import users.User;

public class SalesTransactionSystem {
	private HashMap<String,Cart> cartHashMap = new HashMap<>();
	private HashMap<String,Receipt> receiptHashMap = new HashMap<>();
	private BuyerDatabase buyers;
	private ProductDatabase database;
	private InventorySystem invSystem;
	private static int nextIdCart = 1;
	private static int nextIdReceipt = 1;
	private User currentUser;


	public SalesTransactionSystem(ProductDatabase products, InventorySystem inventory){
		this.database = products;
		this.invSystem = inventory;
	}

	public void setCurrentUser(User user) {
		this.currentUser = user;
	}

	private String currentLevel() {
		return currentUser == null ? "UNKNOWN" : currentUser.getUserAccessLevel();
	}

	public Cart createCart(Buyer buyer) {
		if (buyer == null) {
			throw new IllegalArgumentException("Buyer is required.");
		}
		String formattedCart = String.format("S%05d",nextIdCart);
		String formattedReceipt = String.format("R%05d",nextIdCart);
		Cart emptyCart = new Cart();
		Receipt receipt = new Receipt();
		buyer.setBuyerCart(emptyCart);
		emptyCart.setSaleID(formattedCart);
		emptyCart.setReceipt(receipt);
		emptyCart.setProcessing(currentUser);
		emptyCart.setEmployeeLevelofProcessing(currentLevel());  
		receipt.setTransactionID(formattedReceipt);
		receipt.setCart(emptyCart);
		cartHashMap.put(formattedCart, emptyCart);              
		nextIdCart += 1;
		nextIdReceipt += 1;
		return emptyCart;
	}

	public double cartTotal(Cart cart) {
		return cart.getTotalCartValue();
	}

	public boolean cartEmpty(Cart cart) {
		return cart.isEmpty();
	}

	// CHANGED: returns 0 when the product has no stock record, instead of crashing
	public double productStock(Product product) {
		InventoryOfProduct record = invSystem.getProductInventoryByCode(product.getProductCode());
		return record == null ? 0 : record.getProductStockInKG();
	}

	// CHANGED: >= so a buyer can take exactly all the remaining stock
	public boolean checkProductStockWithQuantity(Product product, Double quantity) {
		return productStock(product) >= quantity;
	}

	//---------------------------------------------------------------------------------------

	/**
	 * CHANGED: throws IllegalArgumentException with a readable message instead of printing to the console,
	 * so the GUI can show it. On success it also marks the cart as paid, stamps the time,
	 * and stores the receipt.
	 */
	public void completeSale(Buyer buyer, String paymentMethod) {
		Cart cart = buyer.getBuyerCart();
		Receipt receipt = cart.getReceipt();

		if (Boolean.TRUE.equals(cart.getIsPaid())) {
			throw new IllegalArgumentException("This sale is already paid.");
		}
		if (cart.isEmpty()) {
			throw new IllegalArgumentException("The cart is empty.");
		}
		if (paymentMethod == null || paymentMethod.isBlank()) {
			throw new IllegalArgumentException("Payment method is required.");
		}

		cart.updateCartValue();
		double total = cart.getTotalCartValue();
		if (buyer.getBudget() < total) {
			throw new IllegalArgumentException(String.format(
					"Buyer budget (PHP %,.2f) is less than the cart total (PHP %,.2f).", buyer.getBudget(), total));
		}

		// Stock may have changed since the items were added, so check again
		for (Map.Entry<Product, Double> item : cart.getCart().entrySet()) {
			if (!checkProductStockWithQuantity(item.getKey(), item.getValue())) {
				throw new IllegalArgumentException("Not enough stock for " + item.getKey().getProductName() + ".");
			}
		}

		// TODO: reduce stock for every item in the cart using the InventorySystem method for removing stock
		// (reason "Sale", performed by the current user). Not done yet because that method has not been seen.

		double change = buyer.getBudget() - total;

		receipt.setAmountPaid(buyer.getBudget());
		receipt.setChange(change);
		receipt.setPaymentMethod(paymentMethod);
		receipt.setEmployee(currentUser);
		receipt.setEmployeeLevel(currentLevel());
		receipt.setTransacTime(LocalDateTime.now());
		receipt.setPurchaseStatus("Complete Purchase!");
		cart.setIsPaid(true);
		receiptHashMap.put(receipt.getTransactionID(), receipt);
	}

	public Collection<Receipt> getAllReceipt() {
		return this.receiptHashMap.values();
	}

	// CHANGED: now actually sorted by receipt ID
	public ArrayList<Receipt> getAllReceiptByID(){
		ArrayList<Receipt> sortedByID = new ArrayList<>(this.receiptHashMap.values());
		sortedByID.sort(Comparator.comparing(Receipt::getTransactionID));
		return sortedByID;
	}
}

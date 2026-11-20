package transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Products.Product;
import Products.ProductDatabase;
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
	
	public Cart createCart(Buyer buyer) {
		String formattedCart = String.format("S%05d",nextIdCart);
		String formattedReceipt = String.format("R%05d",nextIdCart);
		Cart emptyCart = new Cart();
		Receipt receipt = new Receipt();
		buyer.setBuyerCart(emptyCart);
		buyer.getBuyerCart().setSaleID(formattedCart);
		buyer.getBuyerCart().setReceipt(receipt);
		buyer.getBuyerCart().setProcessing(currentUser);
		buyer.getBuyerCart().setEmployeeLevelofProcessing(currentUser.getUserAccessLevel());
		buyer.getBuyerCart().getReceipt().setTransactionID(formattedReceipt);
		buyer.getBuyerCart().getReceipt().setCart(emptyCart);
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
	
	public double productStock(Product product) {
		return invSystem.getProductInventoryByCode(product.getProductCode()).getProductStockInKG();
	}
	
	public boolean checkProductStockWithQuantity(Product product, Double quantity) {
		if(productStock(product) > quantity) {
			return true;
		}
		return false;
	}
	
	//---------------------------------------------------------------------------------------
	
	public void completeSale(Buyer buyer, String paymentMethod) {
		if(buyer.getBudget() < buyer.getBuyerCart().getTotalCartValue()) {
			System.out.println("Buyer Budget is Less Than Cart's Total Value");
			return;
		}
		
		double total = buyer.getBuyerCart().getTotalCartValue();
		double change = buyer.getBudget() - total;
		
		buyer.getBuyerReceipt().setAmountPaid(buyer.getBudget());
		buyer.getBuyerReceipt().setChange(change);
		buyer.getBuyerReceipt().setPaymentMethod(paymentMethod);
		buyer.getBuyerReceipt().setEmployee(currentUser);
		buyer.getBuyerReceipt().setEmployeeLevel(currentUser.getUserAccessLevel());
		buyer.getBuyerReceipt().setPurchaseStatus("Complete Purchase!");
		
		return;
		
	}
	
	public Collection<Receipt> getAllReceipt() {
		return this.receiptHashMap.values();
	}
	
	public ArrayList<Receipt> getAllReceiptByID(){
		ArrayList<Receipt> sortedByID = new ArrayList<>();
		for(Receipt receipt : this.receiptHashMap.values()) {
			sortedByID.add(receipt);
		}
		return sortedByID;
	}
}	

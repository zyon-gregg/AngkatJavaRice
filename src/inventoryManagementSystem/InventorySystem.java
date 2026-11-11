package inventoryManagementSystem;

import java.time.LocalDateTime;
import java.util.*;
import Products.Product;

public class InventorySystem {
	
	
	private final static double MAX_STOCK_IN_KG = 1000.00;
	private final static double CRITICAL_LEVEL_IN_KG = 300.00;
	
	private final HashMap <String, InventoryOfProduct> inventoryRecords;
	private final HashMap <String, StockRecordedChange> history;
	
	private int nextId = 1; //generating IDs for StockRecordedChange. Remind to save this counter.
	
	public InventorySystem() {
		inventoryRecords = new HashMap<String, InventoryOfProduct>();
		history = new HashMap<String, StockRecordedChange>();
	}
	
	private String generateID() {
		return "SC" + String.format("%03d", nextId++);
	}
	
	//inventory record management
	
	public void createInventoryRecord(Product product) {
		
		if(product == null) {
			System.out.println("No product to create a stock record with.");
			return;
		}
		
		String productCode = product.getProductCode();
		
		if (!inventoryRecords.containsKey(productCode)) {
			
			InventoryOfProduct newInventory = new InventoryOfProduct(product);
			inventoryRecords.put(productCode, newInventory);
			
			
		} else {
			System.out.println("This product already has a recorded storage.");
		}
		
		
	}; 
	
	public InventoryOfProduct getProductInventoryByCode(String productCode) { //if you want to get the stock record of a specific product by its code.
		if (inventoryRecords.isEmpty()) {
			System.out.println("Inventory records does not exist yet. There is no product to restock");
			return null;
		}
		
		if (!inventoryRecords.containsKey(productCode)) {//the product is not in inventory records
			System.out.println("The stock record of that product does not exist");
			return null;
			
		}
		
		return inventoryRecords.get(productCode);

	}
	
	public ArrayList<InventoryOfProduct> getAllInventory() { //this may be needed to show in the GUI (Possibly used for table view
		
		ArrayList<InventoryOfProduct> listOfInventoryRecords = new ArrayList<>();
		
		//traversing through the hash map
		
		for (InventoryOfProduct currentProduct : inventoryRecords.values()) {
			listOfInventoryRecords.add(currentProduct);
		}
		
		return listOfInventoryRecords;
		
	}
	
	//audit features
	public ArrayList<StockRecordedChange> getAllHistory() { //used for showing. GUI.
		
		ArrayList<StockRecordedChange> listOfAllRecords = new ArrayList<>();
		
		for (StockRecordedChange currentRecord : history.values()) {
			listOfAllRecords.add(currentRecord);
		}
		
		return listOfAllRecords;
		
	}
	
	public ArrayList<StockRecordedChange> getHistoryByProduct(String productId) { //for GUI
		
		ArrayList<StockRecordedChange> listOfAllRecords = new ArrayList<>();
		
		for (StockRecordedChange currentRecord : history.values()) {
			
			if (currentRecord.getProductCode().equals(productId)) {
				listOfAllRecords.add(currentRecord);
				
			}
			
		}
		
		return listOfAllRecords;
		
	}
	
	
	//stock operations
	public void restockProduct(String productCode, double quantity, String nameOfUser, String explanation) {
		
		if (inventoryRecords.isEmpty()) {
			System.out.println("Inventory records does not exist yet. There is no product to restock");
			return;
		}
		
		if (!inventoryRecords.containsKey(productCode)) {//the product is not in inventory records
			System.out.println("The stock record of that product does not exist");
			return;
			
		}
		
		if (quantity <= 0) {//detects if the requested quantity is negative or 0. This is an invalid input
			System.out.println("Requested quantity is invalid because it is negative or zero");
			return;
			
		}
		
		Double calculatedStock = inventoryRecords.get(productCode).getProductStockInKG() + quantity;
		
		if (calculatedStock > MAX_STOCK_IN_KG) {
			System.out.println("This amount of requested stock will exceed the maximum capacity of the product.");
		
		} else {
			
			Product productToBeRecorded = inventoryRecords.get(productCode).getProduct();
			
			String generatedID = generateID();
			
			StockRecordedChange record = new StockRecordedChange(
					generatedID, 
					productToBeRecorded, 
					StockRecordedChange.RESTOCK, 
					inventoryRecords.get(productCode).getProductStockInKG(), 
					calculatedStock, 
					LocalDateTime.now(), 
					nameOfUser, 
					explanation );
			
			
			history.put(generatedID, record);
			
			inventoryRecords.get(productCode).setProductStockInKG(calculatedStock); //updates 
			
		}
				
	}
	
	public void reduceStock(String productCode, double quantity, String nameOfUser, String explanation) { //this will be called when a customer reduces a stock. You need a product code and a requested quantity
		
		if (inventoryRecords.isEmpty()) {
			System.out.println("Inventory records does not exist yet. There is no product to reduce");
			return;
		}
		
		if (!inventoryRecords.containsKey(productCode)) {//the product is not in inventory records
			System.out.println("The stock record of that product does not exist");
			return;
			
		}
		
		if (quantity <= 0) { //Zero is made invalid because that implies nothing is changed.
			System.out.println("Requested quantity is invalid because it is negative or zero.");
			return;
		}
		
		
		Double calculatedStock = inventoryRecords.get(productCode).getProductStockInKG() - quantity;
		
		if (calculatedStock < 0) {
			System.out.println("Not enough stock to suffice the requested quantity");
		
		} else {
			Product productToBeRecorded = inventoryRecords.get(productCode).getProduct();
			
			String generatedID = generateID();
			
			String changeType = StockRecordedChange.SALE_DEDUCTION;
			
			StockRecordedChange record = new StockRecordedChange(
					generatedID, 
					productToBeRecorded, 
					changeType, 
					inventoryRecords.get(productCode).getProductStockInKG(), 
					calculatedStock, 
					LocalDateTime.now(), 
					nameOfUser, 
					explanation );
			
			
			history.put(generatedID, record);
			inventoryRecords.get(productCode).setProductStockInKG(calculatedStock); //updates it
		}
		
		
		
	}
	
	public void editStock(String productCode, double quantity, String nameOfUser, String explanation) { //manually corrects a stock of a product if needed
		
		if (inventoryRecords.isEmpty()) {
			System.out.println("Inventory records does not exist yet. There is no product to restock");
			return;
		}
		
		if (!inventoryRecords.containsKey(productCode)) {//the product is not in inventory records
			System.out.println("The stock record of that product does not exist");
			return;
			
		}
		
		if (quantity < 0) {//detects if the requested quantity is negative. This is an invalid input
			System.out.println("Requested quantity is invalid because it is negative.");
			return;
			
		}
		
		if (quantity > MAX_STOCK_IN_KG) {
			System.out.println("Exceeds the max stock of the product");
			return;
		}
		
		Product productToBeRecorded = inventoryRecords.get(productCode).getProduct();
		
		String generatedID = generateID();
		
		StockRecordedChange record = new StockRecordedChange(
				generatedID, 
				productToBeRecorded, 
				StockRecordedChange.STOCK_ADJUSTMENT, 
				inventoryRecords.get(productCode).getProductStockInKG(), 
				quantity, 
				LocalDateTime.now(), 
				nameOfUser, 
				explanation );
		
		
		history.put(generatedID, record);
		
		inventoryRecords.get(productCode).setProductStockInKG(quantity);
		
	}
	
	//stock checking
	public boolean hasSufficientStock(String productCode, double quantity) {
		//this could potentially be used for sales system
		InventoryOfProduct productStock = getProductInventoryByCode(productCode);
		
		if (productStock == null) { //checks if a stock record of that product exists
			return false;
		}
		
		if (quantity <= 0 || quantity > MAX_STOCK_IN_KG) { //checks if the requested quantity is valid
			return false;
		}
		
		if (quantity <= productStock.getProductStockInKG()) { //checks if the requested quantity can be computed with that product's stock
			return true;
		} else {
			return false;
		}
		
	}
	
	public boolean isLowOnStock(String productCode) {
		
		InventoryOfProduct productStock = getProductInventoryByCode(productCode);
		
		if (productStock == null) {
			return false;
		}
		
		if (productStock.getProductStockInKG() > 0 && productStock.getProductStockInKG() <= CRITICAL_LEVEL_IN_KG) {
			return true;
			
		} else {
			return false;
		}
	}
	
	//inventory monitoring
	
	public ArrayList<InventoryOfProduct> getProductsLowOnStock() { //for table view. Viewing of products that are low in stock
		

		ArrayList<InventoryOfProduct> listOfProductsLowOnStock = new ArrayList<>();
		
		//traversing through the hash map
		
		for (InventoryOfProduct currentProduct : inventoryRecords.values()) {
			
			if (currentProduct.getProductStockInKG() > 0 && currentProduct.getProductStockInKG() <= CRITICAL_LEVEL_IN_KG) {
				listOfProductsLowOnStock.add(currentProduct);
			}
		}
		
		return listOfProductsLowOnStock;
		
	}
	
	public ArrayList<InventoryOfProduct> getProductsOutOfStock() { //for table view purposes. Viewing of products that are depleted of stock.
		
		
		ArrayList<InventoryOfProduct> listOfDepletedProducts = new ArrayList<>();
		
		for (InventoryOfProduct currentProduct : inventoryRecords.values()) {
			if (currentProduct.getProductStockInKG() == 0) {
				listOfDepletedProducts.add(currentProduct);
			}
			
		}
		
		return listOfDepletedProducts;	
	}
	
}

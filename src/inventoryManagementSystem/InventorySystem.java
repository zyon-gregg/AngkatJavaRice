package inventoryManagementSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Products.Product;

public class InventorySystem {
	//the backend for the inventory system
	
	
	//constants
    private static final double MAX_STOCK_IN_KG = 1000.00; //limits every product's stock to 1000 KG
    private static final double CRITICAL_LEVEL_IN_KG = 300.00; //if a product is below the critical level, it will be tagged as low in stock (or out of stock if 0)
    
    //the hashmaps where inventoryOfStock and StockRecordedChange are stored
    private final HashMap<String, InventoryOfProduct> inventoryRecords; //the key is the product's code while the value is its stock
    private final HashMap<String, StockRecordedChange> history;
    private int nextId = 1;

    public InventorySystem() {
        inventoryRecords = new HashMap<>();
        history = new HashMap<>();
    }

    private String generateID() {//a generator of ID for StockRecordedChange
        String id = "SC" + String.format("%03d", nextId);
        nextId++;
        return id;
    }
    
    public synchronized boolean restoreInventoryRecord(Product product, double stock, LocalDateTime lastUpdated) { //this is used at loading a InventoryOfProduct

        if (product == null || lastUpdated == null) { //checks if product and last updated aren't empty
            return false;
        }

        String productCode = product.getProductCode();

        if (productCode == null || productCode.trim().isEmpty()) { //checks if that product code actually exists
            return false;
        }

        if (stock < 0 || stock > MAX_STOCK_IN_KG || Double.isNaN(stock) || Double.isInfinite(stock)) { //validates the stock of the InventoryOfProduct object. Cannot be negative, exceed max capacity, indeterminate, or infinite
            return false;
        }
        
        
        //checks if that InventoryOfProduct already exists to avoid any duplicates
        if (inventoryRecords.containsKey(productCode)) {
            return false;
        }
        
        //then reconstructs the loaded InventoryOfProduct and puts it at inventoryRecords hashmap
        InventoryOfProduct restoredInventory = new InventoryOfProduct(product, stock, lastUpdated);
        inventoryRecords.put(productCode, restoredInventory);

        return true;
    }
    
    public synchronized boolean restoreInventoryHistory(StockRecordedChange record) { //this method is used for reconstructing StockRecordedChange from a load
    	
    	
    	//Validation to make sure that all the attributes of the record are present
        if (record == null || record.getProduct() == null || record.getTime() == null || record.getRecordId() == null || record.getRecordId().trim().isEmpty()) {
            return false;
        }
        
        
        String recordID = record.getRecordId();
        
        //checks if this record already exists in history hashmap to avoid duplication
        if (history.containsKey(recordID)) {
            return false;
        }
        
        //then stores the record at history hashmap
        history.put(recordID, record);

        // Continue generating stock history IDs after the highest restored ID.
        if (recordID.startsWith("SC")) {
            try {
                int savedID = Integer.parseInt(recordID.substring(2));

                if (savedID >= nextId) {
                    nextId = savedID + 1; //this ensures that the nextId counter is right when the inventory records another change outside load
                }
            } catch (NumberFormatException e) {
                // Keep the current nextId if the saved ID is not numeric.
            }
        }

        return true;
    }
    
    //Creates an inventory record with an opening stock of 0 kg.
    //Returns false if the product is invalid or already has a record.
    public synchronized boolean createInventoryRecord(Product product) {
        if (product == null) { //product must exist first
            return false;
        }

        String productCode = product.getProductCode();

        if (productCode == null || productCode.trim().isEmpty()) { //checks if the product has code first
            return false;
        }

        if (inventoryRecords.containsKey(productCode)) {//prevents duplicates of a product's stock
            return false;
        }

        InventoryOfProduct inventory = new InventoryOfProduct(product);
        inventoryRecords.put(productCode, inventory);
        return true;
    }

    //A product can only be removed when its current stock is 0 kg.
    //A missing record is also safe to remove because there is nothing to delete.
    public synchronized boolean canRemoveInventoryRecord(String productCode) {
        if (productCode == null || productCode.trim().isEmpty()) {
            return false;
        }

        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null) {
            return true;
        }

        if (inventory.getProductStockInKG() != 0) {
            return false;
        }

        return true;
    }

    //Removes an empty inventory record. Stock history is kept separately,
    //so previous stock changes are not deleted with the current record.
    public synchronized boolean removeInventoryRecord(String productCode) {
        if (!canRemoveInventoryRecord(productCode)) {
            return false;
        }

        inventoryRecords.remove(productCode);
        return true;
    }
    
    //retrieve's the record of a product's stock via its product code
    public InventoryOfProduct getProductInventoryByCode(String productCode) {
        if (productCode == null) {
            return null;
        }

        return inventoryRecords.get(productCode);
    }
    
    //a getter to retrieve all the stocks of all products
    public ArrayList<InventoryOfProduct> getAllInventory() {
        return new ArrayList<>(inventoryRecords.values());
    }
    
    //this one's for the history, the recorded changes in stock
    public ArrayList<StockRecordedChange> getAllHistory() {
        return new ArrayList<>(history.values());
    }
    
    //this one's a getter for all the changes in stock of a specific product
    public ArrayList<StockRecordedChange> getHistoryByProduct(String productCode) {
        ArrayList<StockRecordedChange> productHistory = new ArrayList<>();

        if (productCode == null) {
            return productHistory;
        }

        for (StockRecordedChange record : history.values()) {
            if (productCode.equals(record.getProductCode())) {
                productHistory.add(record);
            }
        }

        return productHistory;
    }

    //Adds quantity to the current stock.
    //One of the stock operations. Only manager is able to do this.
    public synchronized void restockProduct(String productCode, double quantity, String nameOfUser, String explanation) {

        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null) {
            System.out.println("The inventory record for this product does not exist.");
            return;
        }

        if (quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            System.out.println("Restock quantity requested is invalid");
            return;
        }

        double previousStock = inventory.getProductStockInKG();
        double newStock = previousStock + quantity;

        if (previousStock < 0 || Double.isNaN(previousStock) || Double.isInfinite(previousStock) || newStock > MAX_STOCK_IN_KG || Double.isInfinite(newStock)) {
            System.out.println("Restocking would exceed the maximum stock of 1000 kg.");
            return;
        }

        addHistoryRecord(inventory, StockRecordedChange.RESTOCK, previousStock, newStock, nameOfUser, explanation);
        inventory.setProductStockInKG(newStock);
    }

    //Manually reduces stock. This is used by the Inventory Management screen.
    //Added this operation to give managers the ability to manually reduce a stock for a known explanation
    public synchronized void reduceStock(String productCode, double quantity, String nameOfUser, String explanation) {

        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null) {
            System.out.println("The inventory record for this product does not exist.");
            return;
        }

        if (quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            System.out.println("Deduction quantity is invalid.");
            return;
        }

        if (nameOfUser == null || nameOfUser.trim().isEmpty()) {
            System.out.println("The employee processing the deduction could not be identified.");
            return;
        }

        double previousStock = inventory.getProductStockInKG();
        
        //just to make sure the current stock is valid
        if (previousStock < 0 || previousStock > MAX_STOCK_IN_KG || Double.isNaN(previousStock) || Double.isInfinite(previousStock)) {
            System.out.println("The current stock value is invalid. Correct it before deducting stock.");
            return;
        }
        
        //prevents a stock being reduced over by its original quantity
        if (quantity > previousStock) {
            System.out.println("Not enough stock for this deduction");
            return;
        }

        double newStock = previousStock - quantity;

        addHistoryRecord(inventory, StockRecordedChange.SALE_DEDUCTION, previousStock, newStock, nameOfUser, explanation);
        inventory.setProductStockInKG(newStock);
    }

    //Sets the stock to an exact quantity. This is used for manual stock adjustments.
    public synchronized void editStock(String productCode, double quantity, String nameOfUser, String explanation) {

        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null) {
            System.out.println("The inventory record for this product does not exist.");
            return;
        }

        if (quantity < 0 || quantity > MAX_STOCK_IN_KG || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            System.out.println("Stock must be between 0 and 1000 kg.");
            return;
        }

        double previousStock = inventory.getProductStockInKG();

        addHistoryRecord(inventory, StockRecordedChange.STOCK_ADJUSTMENT, previousStock, quantity, nameOfUser, explanation);
        inventory.setProductStockInKG(quantity);
    }

 
     //Used by checkout when a sale contains more than one product.
     //First, check every product. Only after all checks pass do we reduce stock.
     //This prevents a sale from deducting only some of its products.
    public synchronized boolean reduceStockForSale(Map<String, Double> quantitiesByProductCode, String nameOfUser, String explanation) {

        if (quantitiesByProductCode == null || quantitiesByProductCode.isEmpty()) {
            return false;
        }

        if (nameOfUser == null || nameOfUser.trim().isEmpty()) {
            return false;
        }

        String reason = "Sale inventory deduction";
        
        if (explanation != null && !explanation.trim().isEmpty()) {
            reason = explanation.trim();
        }
        
        HashMap<String, Double> newStockValues = new HashMap<>();
        
        //This is the first check. We ensure that every product is sufficient in stock before deducting any of it
        for (Map.Entry<String, Double> entry : quantitiesByProductCode.entrySet()) {
            String productCode = entry.getKey();
            Double quantity = entry.getValue();

            if (productCode == null || productCode.trim().isEmpty() || quantity == null || quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
                return false;
            }

            InventoryOfProduct inventory = inventoryRecords.get(productCode);

            if (inventory == null) {
                return false;
            }

            double currentStock = inventory.getProductStockInKG();

            if (currentStock < 0 || currentStock > MAX_STOCK_IN_KG || Double.isNaN(currentStock) || Double.isInfinite(currentStock)) {
                return false;
            }

            if (quantity > currentStock) {
                return false;
            }

            double newStock = currentStock - quantity;
            newStockValues.put(productCode, newStock);
        }
        
        //once all the requested products passed the checks, we can safely update the stock
        for (Map.Entry<String, Double> entry : quantitiesByProductCode.entrySet()) {
            String productCode = entry.getKey();
            InventoryOfProduct inventory = inventoryRecords.get(productCode);

            double previousStock = inventory.getProductStockInKG();
            double newStock = newStockValues.get(productCode);

            addHistoryRecord(inventory, StockRecordedChange.SALE_DEDUCTION, previousStock, newStock, nameOfUser, reason);
            inventory.setProductStockInKG(newStock);
        }

        return true;
    }
    
    //method for adding a record of a change. Generates every time there is a change in stock
    private void addHistoryRecord(InventoryOfProduct inventory, String changeType, double previousStock, double newStock, String nameOfUser, String explanation) {

        String performedBy = nameOfUser;
        
        //This is to prevent any errors when encountering an invalid name or explanation
        if (performedBy == null || performedBy.trim().isEmpty()) {
            performedBy = "Unknown";
        }

        String reason = "";
        if (explanation != null) {
        	reason = explanation.trim();
        }
        
        //creates a new Id for that record, constructs that record, and stores it
        String recordId = generateID();
        StockRecordedChange record = new StockRecordedChange(recordId, inventory.getProduct(), changeType, previousStock, newStock, LocalDateTime.now(), performedBy, reason);

        history.put(recordId, record);
    }
    
    
    //a method to see if the product's stock meets the requested quantity by the user
    public synchronized boolean hasSufficientStock(String productCode, double quantity) {
        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null || quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            return false;
        }

        double currentStock = inventory.getProductStockInKG();

        if (currentStock < 0 || currentStock > MAX_STOCK_IN_KG || Double.isNaN(currentStock) || Double.isInfinite(currentStock)) {
            return false;
        }

        return currentStock >= quantity; //returns true
    }
    
    
    //checks if a specific product's stock is low via its productCode
    public boolean isLowOnStock(String productCode) {
        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null) {//got to make sure that product's stock exists
            return false;
        }

        double stock = inventory.getProductStockInKG();
        return stock > 0 && stock <= CRITICAL_LEVEL_IN_KG; //returns true if the product's stock is more than zero but less that the critical level
    }
    
    //returns an array list of products that are low in stock
    public ArrayList<InventoryOfProduct> getProductsLowOnStock() {
        ArrayList<InventoryOfProduct> lowStockProducts = new ArrayList<>();

        for (InventoryOfProduct inventory : inventoryRecords.values()) {
            double stock = inventory.getProductStockInKG();

            if (stock > 0 && stock <= CRITICAL_LEVEL_IN_KG) {
                lowStockProducts.add(inventory);
            }
        }

        return lowStockProducts;
    }
    
    //this one returns an array list of products out of stock
    public ArrayList<InventoryOfProduct> getProductsOutOfStock() {
        ArrayList<InventoryOfProduct> outOfStockProducts = new ArrayList<>();

        for (InventoryOfProduct inventory : inventoryRecords.values()) {
            if (inventory.getProductStockInKG() == 0) {
                outOfStockProducts.add(inventory);
            }
        }

        return outOfStockProducts;
    }
}
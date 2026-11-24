package inventoryManagementSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import Products.Product;

public class InventorySystem {

    private static final double MAX_STOCK_IN_KG = 1000.00;
    private static final double CRITICAL_LEVEL_IN_KG = 300.00;

    private final HashMap<String, InventoryOfProduct> inventoryRecords;
    private final HashMap<String, StockRecordedChange> history;

    private int nextId = 1;

    public InventorySystem() {
        inventoryRecords = new HashMap<>();
        history = new HashMap<>();
    }

    private String generateID() {
        return "SC" + String.format("%03d", nextId++);
    }

    public void createInventoryRecord(Product product) {
        if (product == null) {
            System.out.println("No product to create a stock record with.");
            return;
        }

        String productCode = product.getProductCode();
        if (!inventoryRecords.containsKey(productCode)) {
            inventoryRecords.put(productCode, new InventoryOfProduct(product));
        } else {
            System.out.println("This product already has a recorded storage.");
        }
    }

    public InventoryOfProduct getProductInventoryByCode(String productCode) {
        if (productCode == null) {
            return null;
        }
        return inventoryRecords.get(productCode);
    }

    public ArrayList<InventoryOfProduct> getAllInventory() {
        return new ArrayList<>(inventoryRecords.values());
    }


    public ArrayList<StockRecordedChange> getAllHistory() {
        return new ArrayList<>(history.values());
    }

    public ArrayList<StockRecordedChange> getHistoryByProduct(String productCode) {
        ArrayList<StockRecordedChange> records = new ArrayList<>();
        
        if (productCode == null) {
            return records;
        }

        for (StockRecordedChange record : history.values()) {
            if (productCode.equals(record.getProductCode())) {
                records.add(record);
            }
        }
        
        return records;
    }

    public synchronized void restockProduct(String productCode, double quantity, String nameOfUser, String explanation) {

        InventoryOfProduct inventory = inventoryRecords.get(productCode);
        
        if (inventory == null) {
            System.out.println("The stock record of that product does not exist.");
            return;
        }
        
        if (!Double.isFinite(quantity) || quantity <= 0) {
            System.out.println("Requested quantity must be a finite number greater than zero.");
            return;
        }

        double previousStock = inventory.getProductStockInKG();
        double newStock = previousStock + quantity;
        
        if (!Double.isFinite(previousStock) || previousStock < 0 || !Double.isFinite(newStock) || newStock > MAX_STOCK_IN_KG) {
            System.out.println("This restock would exceed the maximum stock capacity or the current stock is invalid.");
            return;
        }

        addHistoryRecord(inventory, StockRecordedChange.RESTOCK, previousStock, newStock, nameOfUser, explanation);
        inventory.setProductStockInKG(newStock);
    }

    public synchronized void reduceStock(String productCode, double quantity, String nameOfUser, String explanation) {

        Map<String, Double> requested = new LinkedHashMap<>();
        requested.put(productCode, quantity);
        
        try {
            reduceStockForSale(requested, nameOfUser, explanation);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public synchronized void editStock(String productCode, double quantity, String nameOfUser, String explanation) {

        InventoryOfProduct inventory = inventoryRecords.get(productCode);
        
        if (inventory == null) {
            System.out.println("The stock record of that product does not exist.");
            return;
        }
        
        if (!Double.isFinite(quantity) || quantity < 0 || quantity > MAX_STOCK_IN_KG) {
            System.out.println("Stock must be a finite value between 0 and " + MAX_STOCK_IN_KG + " kg.");
            return;
        }

        double previousStock = inventory.getProductStockInKG();
        addHistoryRecord(inventory, StockRecordedChange.STOCK_ADJUSTMENT, previousStock, quantity, nameOfUser, explanation);
        inventory.setProductStockInKG(quantity);
    }

    public synchronized void reduceStockForSale(Map<String, Double> quantitiesByProductCode, String nameOfUser, String explanation) {

        if (quantitiesByProductCode == null || quantitiesByProductCode.isEmpty()) {
            throw new IllegalArgumentException("There are no products to deduct from inventory.");
        }

        if (nameOfUser == null || nameOfUser.isBlank()) {
            throw new IllegalArgumentException("The employee processing the sale could not be identified.");
        }

        // Normalize/aggregate quantities first. This also protects against
        // the same product code appearing more than once in an input map.
        Map<String, Double> aggregated = new LinkedHashMap<>();
        
        for (Map.Entry<String, Double> entry : quantitiesByProductCode.entrySet()) {
            String productCode = entry.getKey();
            Double quantity = entry.getValue();

            if (productCode == null || productCode.isBlank()) {
                throw new IllegalArgumentException("A sale item has an invalid product code.");
            }
            
            if (quantity == null || !Double.isFinite(quantity) || quantity <= 0) {
                throw new IllegalArgumentException("Quantity for product " + productCode + " must be a finite number greater than zero.");
            }

            double combined = aggregated.getOrDefault(productCode, 0.0) + quantity;
            
            if (!Double.isFinite(combined)) {
                throw new IllegalArgumentException("Combined quantity for product " + productCode + " is invalid.");
            }
            
            aggregated.put(productCode, combined);
        }

        // Phase 1: validate every item and calculate all resulting stock.
        Map<String, Double> resultingStock = new LinkedHashMap<>();
        
        for (Map.Entry<String, Double> entry : aggregated.entrySet()) {
            String productCode = entry.getKey();
            double quantity = entry.getValue();
            InventoryOfProduct inventory = inventoryRecords.get(productCode);

            if (inventory == null) {
                throw new IllegalArgumentException("No inventory record exists for product " + productCode + ".");
            }

            double previousStock = inventory.getProductStockInKG();
            
            if (!Double.isFinite(previousStock) || previousStock < 0 || previousStock > MAX_STOCK_IN_KG) {
                throw new IllegalArgumentException("Current stock for product " + productCode + " is invalid. Please correct the inventory first.");
            }
            
            if (quantity > previousStock) {
                throw new IllegalArgumentException(String.format("Insufficient stock for product %s. Available: %.2f kg; requested: %.2f kg.", productCode, previousStock, quantity));
            }

            resultingStock.put(productCode, previousStock - quantity);
        }

       
        String reason = (explanation == null || explanation.isBlank()) ? "Sale inventory deduction" : explanation.trim();

        for (Map.Entry<String, Double> entry : aggregated.entrySet()) {
            String productCode = entry.getKey();
            InventoryOfProduct inventory = inventoryRecords.get(productCode);
            double previousStock = inventory.getProductStockInKG();
            double newStock = resultingStock.get(productCode);

            addHistoryRecord(inventory, StockRecordedChange.SALE_DEDUCTION, previousStock, newStock, nameOfUser, reason);
            inventory.setProductStockInKG(newStock);
        }
    }

    private void addHistoryRecord(InventoryOfProduct inventory, String changeType, double previousStock, double newStock, String nameOfUser, String explanation) {

        String performedBy = (nameOfUser == null || nameOfUser.isBlank()) ? "Unknown" : nameOfUser.trim();
        String reason = explanation == null ? "" : explanation.trim();

        String generatedID = generateID();
        StockRecordedChange record = new StockRecordedChange(generatedID, inventory.getProduct(), changeType, previousStock, newStock, LocalDateTime.now(), performedBy, reason);

        history.put(generatedID, record);
    }

 

    public synchronized boolean hasSufficientStock(String productCode, double quantity) {
    	
        InventoryOfProduct inventory = inventoryRecords.get(productCode);
        
        if (inventory == null || !Double.isFinite(quantity) || quantity <= 0 || quantity > MAX_STOCK_IN_KG) {
            return false;
        }

        double currentStock = inventory.getProductStockInKG();
        return Double.isFinite(currentStock) && currentStock >= 0 && currentStock <= MAX_STOCK_IN_KG && quantity <= currentStock;
    }

    public boolean isLowOnStock(String productCode) {
        InventoryOfProduct inventory = inventoryRecords.get(productCode);
        
        if (inventory == null) {
            return false;
        }

        double stock = inventory.getProductStockInKG();
        return Double.isFinite(stock) && stock > 0 && stock <= CRITICAL_LEVEL_IN_KG;
    }

    public ArrayList<InventoryOfProduct> getProductsLowOnStock() {
    	
        ArrayList<InventoryOfProduct> lowStock = new ArrayList<>();
        
        for (InventoryOfProduct inventory : inventoryRecords.values()) {
        	
            double stock = inventory.getProductStockInKG();
            
            if (Double.isFinite(stock) && stock > 0 && stock <= CRITICAL_LEVEL_IN_KG) {
                lowStock.add(inventory);
            }
        }
        
        return lowStock;
    }

    public ArrayList<InventoryOfProduct> getProductsOutOfStock() {
    	
        ArrayList<InventoryOfProduct> outOfStock = new ArrayList<>();
        
        for (InventoryOfProduct inventory : inventoryRecords.values()) {
        	
            if (inventory.getProductStockInKG() == 0) {
                outOfStock.add(inventory);
            }
        }
        
        return outOfStock;
    }
}
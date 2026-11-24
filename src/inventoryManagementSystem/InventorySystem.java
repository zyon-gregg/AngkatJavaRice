package inventoryManagementSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        String id = "SC" + String.format("%03d", nextId);
        nextId++;
        return id;
    }

    // Creates an inventory record with an opening stock of 0 kg.
    public void createInventoryRecord(Product product) {
        if (product == null) {
            System.out.println("No product was provided.");
            return;
        }

        String productCode = product.getProductCode();

        if (inventoryRecords.containsKey(productCode)) {
            System.out.println("This product already has an inventory record.");
            return;
        }

        InventoryOfProduct inventory = new InventoryOfProduct(product);
        inventoryRecords.put(productCode, inventory);
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

    // Adds quantity to the current stock.
    public synchronized void restockProduct(String productCode, double quantity,
            String nameOfUser, String explanation) {

        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null) {
            System.out.println("The inventory record for this product does not exist.");
            return;
        }

        if (quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            System.out.println("Restock quantity must be greater than zero.");
            return;
        }

        double previousStock = inventory.getProductStockInKG();
        double newStock = previousStock + quantity;

        if (previousStock < 0 || Double.isNaN(previousStock) || Double.isInfinite(previousStock)
                || newStock > MAX_STOCK_IN_KG || Double.isInfinite(newStock)) {
            System.out.println("Restocking would exceed the maximum stock of 1000 kg.");
            return;
        }

        addHistoryRecord(inventory, StockRecordedChange.RESTOCK, previousStock, newStock,
                nameOfUser, explanation);
        inventory.setProductStockInKG(newStock);
    }

    // Manually reduces stock. This is used by the Inventory Management screen.
    public synchronized void reduceStock(String productCode, double quantity,
            String nameOfUser, String explanation) {

        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null) {
            System.out.println("The inventory record for this product does not exist.");
            return;
        }

        if (quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            System.out.println("Deduction quantity must be greater than zero.");
            return;
        }

        if (nameOfUser == null || nameOfUser.trim().isEmpty()) {
            System.out.println("The employee processing the deduction could not be identified.");
            return;
        }

        double previousStock = inventory.getProductStockInKG();

        if (previousStock < 0 || previousStock > MAX_STOCK_IN_KG
                || Double.isNaN(previousStock) || Double.isInfinite(previousStock)) {
            System.out.println("The current stock value is invalid. Correct it before deducting stock.");
            return;
        }

        if (quantity > previousStock) {
            System.out.println("There is not enough stock for this deduction.");
            return;
        }

        double newStock = previousStock - quantity;

        addHistoryRecord(inventory, StockRecordedChange.SALE_DEDUCTION, previousStock,
                newStock, nameOfUser, explanation);
        inventory.setProductStockInKG(newStock);
    }

    // Sets the stock to an exact quantity. This is used for manual stock adjustments.
    public synchronized void editStock(String productCode, double quantity,
            String nameOfUser, String explanation) {

        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null) {
            System.out.println("The inventory record for this product does not exist.");
            return;
        }

        if (quantity < 0 || quantity > MAX_STOCK_IN_KG
                || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
            System.out.println("Stock must be between 0 and 1000 kg.");
            return;
        }

        double previousStock = inventory.getProductStockInKG();

        addHistoryRecord(inventory, StockRecordedChange.STOCK_ADJUSTMENT, previousStock,
                quantity, nameOfUser, explanation);
        inventory.setProductStockInKG(quantity);
    }

    /*
     * Used by checkout when a sale contains more than one product.
     * First, check every product. Only after all checks pass do we reduce stock.
     * This prevents a sale from deducting only some of its products.
     */
    public synchronized void reduceStockForSale(Map<String, Double> quantitiesByProductCode,
            String nameOfUser, String explanation) {

        if (quantitiesByProductCode == null || quantitiesByProductCode.isEmpty()) {
            throw new IllegalArgumentException("There are no products in this sale.");
        }

        if (nameOfUser == null || nameOfUser.trim().isEmpty()) {
            throw new IllegalArgumentException("The employee processing the sale could not be identified.");
        }

        String reason = "Sale inventory deduction";
        if (explanation != null && !explanation.trim().isEmpty()) {
            reason = explanation.trim();
        }

        HashMap<String, Double> newStockValues = new HashMap<>();

        // First pass: check every product and calculate its new stock.
        for (Map.Entry<String, Double> entry : quantitiesByProductCode.entrySet()) {
            String productCode = entry.getKey();
            Double quantity = entry.getValue();

            if (productCode == null || productCode.trim().isEmpty() || quantity == null
                    || quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
                throw new IllegalArgumentException("A sale contains an invalid product or quantity.");
            }

            InventoryOfProduct inventory = inventoryRecords.get(productCode);

            if (inventory == null) {
                throw new IllegalArgumentException("No inventory record exists for product "
                        + productCode + ".");
            }

            double currentStock = inventory.getProductStockInKG();

            if (currentStock < 0 || currentStock > MAX_STOCK_IN_KG
                    || Double.isNaN(currentStock) || Double.isInfinite(currentStock)) {
                throw new IllegalArgumentException("Current stock for product " + productCode
                        + " is invalid. Correct the inventory before completing this sale.");
            }

            if (quantity > currentStock) {
                throw new IllegalArgumentException("Insufficient stock for product " + productCode
                        + ". Available: " + String.format("%.2f", currentStock)
                        + " kg; requested: " + String.format("%.2f", quantity) + " kg.");
            }

            double newStock = currentStock - quantity;
            newStockValues.put(productCode, newStock);
        }

        // Second pass: all products passed the checks, so now update the stock.
        for (Map.Entry<String, Double> entry : quantitiesByProductCode.entrySet()) {
            String productCode = entry.getKey();
            InventoryOfProduct inventory = inventoryRecords.get(productCode);

            double previousStock = inventory.getProductStockInKG();
            double newStock = newStockValues.get(productCode);

            addHistoryRecord(inventory, StockRecordedChange.SALE_DEDUCTION, previousStock,
                    newStock, nameOfUser, reason);
            inventory.setProductStockInKG(newStock);
        }
    }

    private void addHistoryRecord(InventoryOfProduct inventory, String changeType,
            double previousStock, double newStock, String nameOfUser, String explanation) {

        String performedBy = nameOfUser;
        if (performedBy == null || performedBy.trim().isEmpty()) {
            performedBy = "Unknown";
        }

        String reason = "";
        if (explanation != null) {
            reason = explanation.trim();
        }

        String recordId = generateID();
        StockRecordedChange record = new StockRecordedChange(recordId, inventory.getProduct(),
                changeType, previousStock, newStock, LocalDateTime.now(), performedBy, reason);

        history.put(recordId, record);
    }

    public synchronized boolean hasSufficientStock(String productCode, double quantity) {
        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null || quantity <= 0 || Double.isNaN(quantity)
                || Double.isInfinite(quantity)) {
            return false;
        }

        double currentStock = inventory.getProductStockInKG();

        if (currentStock < 0 || currentStock > MAX_STOCK_IN_KG
                || Double.isNaN(currentStock) || Double.isInfinite(currentStock)) {
            return false;
        }

        return currentStock >= quantity;
    }

    public boolean isLowOnStock(String productCode) {
        InventoryOfProduct inventory = inventoryRecords.get(productCode);

        if (inventory == null) {
            return false;
        }

        double stock = inventory.getProductStockInKG();
        return stock > 0 && stock <= CRITICAL_LEVEL_IN_KG;
    }

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

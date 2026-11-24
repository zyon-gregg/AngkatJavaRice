package transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Products.Product;
import Products.ProductDatabase;
import inventoryManagementSystem.InventoryOfProduct;
import inventoryManagementSystem.InventorySystem;
import users.Buyer;
import users.BuyerDatabase;
import users.SourceDatabase;
import users.User;

public class SalesTransactionSystem {

    private final HashMap<String, Cart> cartHashMap = new HashMap<>();
    private final HashMap<String, Receipt> receiptHashMap = new HashMap<>();

    private final BuyerDatabase buyers;
    private final ProductDatabase database;
    private final InventorySystem invSystem;
    private final SourceDatabase sources;

    private int nextIdCart = 1;
    private int nextIdReceipt = 1;
    private User currentUser;

    public SalesTransactionSystem(ProductDatabase products, InventorySystem inventory, BuyerDatabase buyers, SourceDatabase sources) {
        this.database = products;
        this.invSystem = inventory;
        this.buyers = buyers;
        this.sources = sources;
    }

    // The logged-in employee is set by the application before opening the POS.
    public void setCurrentUser(User user) {
        currentUser = user;
    }

    private String currentLevel() {
        if (currentUser == null) {
            return "UNKNOWN";
        }

        return currentUser.getUserAccessLevel();
    }

    // Creates a new cart for a buyer.
    public Cart createCart(Buyer buyer) {
        validateCurrentEmployee();

        if (buyer == null) {
            throw new IllegalArgumentException("Buyer is required.");
        }

        Cart oldCart = buyer.getBuyerCart();

        if (oldCart != null && !Boolean.TRUE.equals(oldCart.getIsPaid())) {
            throw new IllegalArgumentException("This buyer already has an unfinished sale. Complete or cancel it first.");
        }

        String saleID = String.format("S%05d", nextIdCart);
        nextIdCart++;

        String receiptID = String.format("R%05d", nextIdReceipt);
        nextIdReceipt++;

        Cart cart = new Cart();
        Receipt receipt = new Receipt();

        cart.setSaleID(saleID);
        cart.setReceipt(receipt);
        cart.setProcessing(currentUser);
        cart.setEmployeeLevelofProcessing(currentLevel());

        receipt.setTransactionID(receiptID);
        receipt.setCart(cart);

        buyer.setBuyerCart(cart);
        cartHashMap.put(saleID, cart);

        return cart;
    }

    public double cartTotal(Cart cart) {
        if (cart == null) {
            return 0.0;
        }

        cart.updateCartValue();
        
        return cart.getTotalCartValue();
    }

    public boolean cartEmpty(Cart cart) {
        if (cart == null) {
            return true;
        }

        return cart.isEmpty();
    }

    public double productStock(Product product) {
        if (product == null) {
            return 0.0;
        }

        InventoryOfProduct inventory = invSystem.getProductInventoryByCode(product.getProductCode());

        if (inventory == null) {
            return 0.0;
        }

        return inventory.getProductStockInKG();
    }

    public boolean checkProductStockWithQuantity(Product product, Double quantity) {
        if (product == null || quantity == null) {
            return false;
        }

        return invSystem.hasSufficientStock(product.getProductCode(), quantity);
    }

    // Used when the caller wants to pay the exact cart total.
    public Receipt completeSale(Buyer buyer, String paymentMethod) {
        double exactPayment = 0.0;

        if (buyer != null && buyer.getBuyerCart() != null) {
            exactPayment = buyer.getBuyerCart().getTotalCartValue();
        }

        return completeSale(buyer, paymentMethod, exactPayment);
    }

    public synchronized Receipt completeSale(Buyer buyer, String paymentMethod, double cashReceived) {

        validateCurrentEmployee();

        if (buyer == null) {
            throw new IllegalArgumentException("Buyer is required.");
        }

        Cart cart = buyer.getBuyerCart();

        if (cart == null) {
            throw new IllegalArgumentException("Start a sale before completing checkout.");
        }

        Receipt receipt = cart.getReceipt();

        if (receipt == null) {
            throw new IllegalArgumentException("The sale is missing its receipt record.");
        }

        if (Boolean.TRUE.equals(cart.getIsPaid())) {
            throw new IllegalArgumentException("This sale is already paid.");
        }

        if (cart.isEmpty()) {
            throw new IllegalArgumentException("The cart is empty.");
        }

        String selectedPaymentMethod = normalizePaymentMethod(paymentMethod);

        cart.updateCartValue();
        double total = cart.getTotalCartValue();

        if (total <= 0 || Double.isNaN(total) || Double.isInfinite(total)) {
            throw new IllegalArgumentException("The sale total is invalid.");
        }

        // The buyer's budget is the spending limit for this transaction.
        double buyerBudget = buyer.getBudget();

        if (buyerBudget < total || Double.isNaN(buyerBudget) || Double.isInfinite(buyerBudget)) {
            throw new IllegalArgumentException(String.format("Buyer spending limit (PHP %,.2f) is less than the cart total (PHP %,.2f).", buyerBudget, total));
        }

        double recordedCashReceived = 0.0;
        double change = 0.0;

        if (selectedPaymentMethod.equals("Cash")) {
            if (cashReceived < total || Double.isNaN(cashReceived) || Double.isInfinite(cashReceived)) {
                throw new IllegalArgumentException(String.format("Cash received must be at least PHP %,.2f.", total));
            }

            recordedCashReceived = cashReceived;
            change = cashReceived - total;
        }

        List<ReceiptItem> receiptItems = new ArrayList<>();
        Map<String, Double> quantitiesByProductCode = new LinkedHashMap<>();

        prepareSaleItems(cart, receiptItems, quantitiesByProductCode);

        for (Map.Entry<String, Double> entry : quantitiesByProductCode.entrySet()) {
            String productCode = entry.getKey();
            double quantity = entry.getValue();

            if (!invSystem.hasSufficientStock(productCode, quantity)) {
                Product product = findProductByCode(productCode);
                String productName = productCode;

                if (product != null) {
                    productName = product.getProductName();
                }

                throw new IllegalArgumentException("Not enough available stock for " + productName + ". Available: " + String.format("%.2f kg", productStock(product)) + ".");
            }
        }

        String employeeUsername = currentUser.getUserName();
        String auditExplanation = "Sale " + cart.getSaleID() + " / Receipt " + receipt.getTransactionID();

        // InventorySystem checks all products before deducting any of them.
        invSystem.reduceStockForSale(quantitiesByProductCode, employeeUsername, auditExplanation);

        // Save the receipt only after the stock deduction succeeds.
        receipt.setItems(receiptItems);
        receipt.setTotalAmount(total);
        receipt.setAmountPaid(total);
        receipt.setCashReceived(recordedCashReceived);
        receipt.setChange(change);
        receipt.setPaymentMethod(selectedPaymentMethod);
        receipt.setEmployee(currentUser);
        receipt.setEmployeeLevel(currentLevel());
        receipt.setTransacTime(LocalDateTime.now());
        receipt.setPurchaseStatus("COMPLETED");

        cart.setIsPaid(true);
        receiptHashMap.put(receipt.getTransactionID(), receipt);

        return receipt;
    }

    // Builds the receipt snapshots and combines quantities with the same product code.
    private void prepareSaleItems(Cart cart, List<ReceiptItem> receiptItems, Map<String, Double> quantitiesByProductCode) {

        for (Map.Entry<Product, Double> entry : cart.getCart().entrySet()) {
            Product product = entry.getKey();
            Double quantity = entry.getValue();

            if (product == null || quantity == null) {
                throw new IllegalArgumentException("The cart contains an invalid item.");
            }

            if (quantity <= 0 || Double.isNaN(quantity) || Double.isInfinite(quantity)) {
                throw new IllegalArgumentException("The cart contains an invalid quantity.");
            }

            String productCode = product.getProductCode();
            Product registeredProduct = findProductByCode(productCode);

            if (registeredProduct == null) {
                throw new IllegalArgumentException("Product " + productCode + " is no longer registered. Remove it from the cart and try again.");
            }

            double unitPrice = cart.getUnitPrice(product);
            String productName = cart.getProductName(product);

            ReceiptItem receiptItem = new ReceiptItem(productCode, productName,
                    quantity, unitPrice);
            receiptItems.add(receiptItem);

            Double previousQuantity = quantitiesByProductCode.get(productCode);
            double combinedQuantity = quantity;

            if (previousQuantity != null) {
                combinedQuantity = previousQuantity + quantity;
            }

            if (Double.isNaN(combinedQuantity) || Double.isInfinite(combinedQuantity)) {
                throw new IllegalArgumentException("The combined quantity for "
                        + productName + " is invalid.");
            }

            quantitiesByProductCode.put(productCode, combinedQuantity);
        }
    }

    // Checks that a valid employee is logged in before using the POS backend.
    private void validateCurrentEmployee() {
        if (currentUser == null) {
            throw new IllegalArgumentException(
                    "No employee is logged in. Please log in before processing a sale.");
        }

        Boolean isActivated = currentUser.getActivated();

        if (isActivated == null || isActivated == false) {
            throw new IllegalArgumentException("This employee account is not active.");
        }

        String role = currentUser.getUserAccessLevel();

        if (!"CLERK".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException(
                    "Only a Clerk or Manager can process a sale.");
        }

        String username = currentUser.getUserName();

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("The logged-in employee has no username.");
        }
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Select a payment method.");
        }

        String selectedMethod = paymentMethod.trim();

        if (selectedMethod.equalsIgnoreCase("Cash")) {
            return "Cash";
        }

        if (selectedMethod.equalsIgnoreCase("GCash")) {
            return "GCash";
        }

        if (selectedMethod.equalsIgnoreCase("Bank Transfer")) {
            return "Bank Transfer";
        }

        throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
    }

    private Product findProductByCode(String productCode) {
        if (productCode == null) {
            return null;
        }

        return database.searchProduct(productCode);
    }

    // Cancels every unfinished cart when an employee logs out.
    public synchronized int cancelAllUnpaidSales() {
        if (buyers == null) {
            return 0;
        }

        int cancelledCount = 0;

        for (Buyer buyer : buyers.returnBuyers()) {
            if (buyer == null) {
                continue;
            }

            Cart cart = buyer.getBuyerCart();

            if (cart != null && !Boolean.TRUE.equals(cart.getIsPaid())) {
                cancelSale(buyer);
                cancelledCount++;
            }
        }

        return cancelledCount;
    }

    // Removes an unfinished cart. A completed sale cannot be cancelled here.
    public synchronized boolean cancelSale(Buyer buyer) {
        if (buyer == null) {
            return false;
        }

        Cart cart = buyer.getBuyerCart();

        if (cart == null) {
            return false;
        }

        if (Boolean.TRUE.equals(cart.getIsPaid())) {
            throw new IllegalArgumentException("A completed sale cannot be cancelled as an unpaid cart.");
        }

        cartHashMap.remove(cart.getSaleID());
        buyer.setBuyerCart(null);

        return true;
    }

    public Collection<Receipt> getAllReceipt() {
        return new ArrayList<>(receiptHashMap.values());
    }

    public ArrayList<Receipt> getAllReceiptByID() {
        ArrayList<Receipt> sortedByID = new ArrayList<>(receiptHashMap.values());
        sortedByID.sort(Comparator.comparing(Receipt::getTransactionID));
        return sortedByID;
    }

    public Receipt getReceiptByID(String transactionID) {
        return receiptHashMap.get(transactionID);
    }
}
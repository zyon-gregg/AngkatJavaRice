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
import inventoryManagementSystem.InventorySystem;
import users.Buyer;
import users.BuyerDatabase;
import users.SourceDatabase;
import users.User;
import inventoryManagementSystem.InventoryOfProduct;

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

    public SalesTransactionSystem(ProductDatabase products,
                                  InventorySystem inventory,
                                  BuyerDatabase buyers,
                                  SourceDatabase sources) {
        this.database = products;
        this.invSystem = inventory;
        this.buyers = buyers;
        this.sources = sources;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private String currentLevel() {
        return currentUser == null ? "UNKNOWN" : currentUser.getUserAccessLevel();
    }

    public Cart createCart(Buyer buyer) {
        validateCurrentEmployee();
        if (buyer == null) {
            throw new IllegalArgumentException("Buyer is required.");
        }

        Cart existingCart = buyer.getBuyerCart();
        if (existingCart != null && !Boolean.TRUE.equals(existingCart.getIsPaid())) {
            throw new IllegalArgumentException("This buyer already has an unfinished sale. Complete or cancel it first.");
        }

        String saleID = String.format("S%05d", nextIdCart++);
        String receiptID = String.format("R%05d", nextIdReceipt++);

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
        return cart == null || cart.isEmpty();
    }

    public double productStock(Product product) {
        if (product == null) {
            return 0.0;
        }
        InventoryOfProduct record =
        	    invSystem.getProductInventoryByCode(product.getProductCode());
        return record == null ? 0.0 : record.getProductStockInKG();
    }

    public boolean checkProductStockWithQuantity(Product product, Double quantity) {
        return product != null && quantity != null
                && invSystem.hasSufficientStock(product.getProductCode(), quantity);
    }

    /**
     * Backwards-compatible overload. Callers that do not supply a cash tender
     * are treated as paying the exact total. TransactionView uses the explicit
     * three-argument overload for cash payments.
     */
    public Receipt completeSale(Buyer buyer, String paymentMethod) {
        Cart cart = buyer == null ? null : buyer.getBuyerCart();
        double exactPayment = cart == null ? 0.0 : cart.getTotalCartValue();
        return completeSale(buyer, paymentMethod, exactPayment);
    }

    /**
     * Completes a sale only after validating the employee, buyer spending limit,
     * payment, every product, and all inventory requirements. InventorySystem
     * then performs all stock deductions atomically and records their history.
     *
     * Buyer.budget is treated as the buyer's spending limit, not as cash handed
     * to the cashier. For Cash, cashReceived is the actual tendered amount.
     * GCash and Bank Transfer are simulated as exact full payments.
     */
    public synchronized Receipt completeSale(Buyer buyer,
                                             String paymentMethod,
                                             double cashReceived) {

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

        String normalizedPaymentMethod = normalizePaymentMethod(paymentMethod);

        cart.updateCartValue();
        double total = cart.getTotalCartValue();
        if (!Double.isFinite(total) || total <= 0) {
            throw new IllegalArgumentException("The sale total is invalid.");
        }

        double buyerBudget = buyer.getBudget();
        if (!Double.isFinite(buyerBudget) || buyerBudget < total) {
            throw new IllegalArgumentException(String.format(
                    "Buyer spending limit (PHP %,.2f) is less than the cart total (PHP %,.2f).",
                    buyerBudget, total));
        }

        double change = 0.0;
        double recordedCashReceived = 0.0;
        if ("Cash".equals(normalizedPaymentMethod)) {
            if (!Double.isFinite(cashReceived) || cashReceived < total) {
                throw new IllegalArgumentException(String.format(
                        "Cash received must be a finite amount of at least PHP %,.2f.", total));
            }
            recordedCashReceived = cashReceived;
            change = cashReceived - total;
        }

        // Build item snapshots and aggregate quantities by product code.
        // This preserves the prices/names used when the items entered the cart
        // and prevents duplicate Product objects with the same code bypassing
        // the total stock check.
        List<ReceiptItem> receiptItems = new ArrayList<>();
        Map<String, Double> quantitiesByProductCode = new LinkedHashMap<>();

        for (Map.Entry<Product, Double> entry : cart.getCart().entrySet()) {
            Product product = entry.getKey();
            Double quantity = entry.getValue();

            if (product == null || quantity == null
                    || !Double.isFinite(quantity) || quantity <= 0) {
                throw new IllegalArgumentException("The cart contains an invalid product or quantity.");
            }
            if (!isProductRegistered(product.getProductCode())) {
                throw new IllegalArgumentException("Product " + product.getProductCode()
                        + " is no longer registered. Remove it from the cart and try again.");
            }

            double unitPrice = cart.getUnitPrice(product);
            String productName = cart.getProductName(product);
            receiptItems.add(new ReceiptItem(
                    product.getProductCode(), productName, quantity, unitPrice));

            double combinedQuantity = quantitiesByProductCode.getOrDefault(
                    product.getProductCode(), 0.0) + quantity;
            if (!Double.isFinite(combinedQuantity)) {
                throw new IllegalArgumentException("Combined quantity for "
                        + productName + " is invalid.");
            }
            quantitiesByProductCode.put(product.getProductCode(), combinedQuantity);
        }

        // Provide a product-specific stock error before invoking the atomic
        // inventory operation. InventorySystem checks everything again under
        // its synchronized batch method before changing any quantity.
        for (Map.Entry<String, Double> entry : quantitiesByProductCode.entrySet()) {
            if (!invSystem.hasSufficientStock(entry.getKey(), entry.getValue())) {
                Product product = findProductByCode(entry.getKey());
                String name = product == null ? entry.getKey() : product.getProductName();
                throw new IllegalArgumentException("Not enough available stock for " + name
                        + ". Available: " + String.format("%.2f kg", productStock(product)) + ".");
            }
        }

        String employeeUsername = currentUser.getUserName();
        String auditExplanation = "Sale " + cart.getSaleID()
                + " / Receipt " + receipt.getTransactionID();

        // No receipt is marked paid and no payment is stored until this
        // all-or-nothing inventory operation succeeds.
        invSystem.reduceStockForSale(
                quantitiesByProductCode, employeeUsername, auditExplanation);

        receipt.setItems(receiptItems);
        receipt.setTotalAmount(total);
        receipt.setAmountPaid(total);
        receipt.setCashReceived(recordedCashReceived);
        receipt.setChange(change);
        receipt.setPaymentMethod(normalizedPaymentMethod);
        receipt.setEmployee(currentUser);
        receipt.setEmployeeLevel(currentLevel());
        receipt.setTransacTime(LocalDateTime.now());
        receipt.setPurchaseStatus("COMPLETED");

        cart.setIsPaid(true);
        receiptHashMap.put(receipt.getTransactionID(), receipt);

        return receipt;
    }

    private void validateCurrentEmployee() {
        if (currentUser == null) {
            throw new IllegalArgumentException("No employee is logged in. Please log in before processing a sale.");
        }
        if (!Boolean.TRUE.equals(currentUser.getActivated())) {
            throw new IllegalArgumentException("This employee account is not active.");
        }

        String role = currentUser.getUserAccessLevel();
        if (!"CLERK".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Only an authenticated Clerk or Manager can process a sale.");
        }
        if (currentUser.getUserName() == null || currentUser.getUserName().isBlank()) {
            throw new IllegalArgumentException("The logged-in employee has no username.");
        }
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Select a payment method.");
        }
        String value = paymentMethod.trim();
        if ("Cash".equalsIgnoreCase(value)) return "Cash";
        if ("GCash".equalsIgnoreCase(value)) return "GCash";
        if ("Bank Transfer".equalsIgnoreCase(value)) return "Bank Transfer";
        throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
    }

    private boolean isProductRegistered(String productCode) {
        return findProductByCode(productCode) != null;
    }

    private Product findProductByCode(String productCode) {
        if (productCode == null) {
            return null;
        }
        for (Product product : database.getAllProducts()) {
            if (productCode.equals(product.getProductCode())) {
                return product;
            }
        }
        return null;
    }

    /**
     * Cancels every unfinished cart when an employee logs out. This prevents
     * an abandoned buyer cart from blocking the next employee's sale.
     */
    public synchronized int cancelAllUnpaidSales() {
        if (buyers == null) {
            return 0;
        }

        int cancelledCount = 0;
        for (Buyer buyer : buyers.returnBuyers()) {
            if (buyer != null && buyer.getBuyerCart() != null
                    && !Boolean.TRUE.equals(buyer.getBuyerCart().getIsPaid())) {
                cancelSale(buyer);
                cancelledCount++;
            }
        }
        return cancelledCount;
    }

    /** Cancels an unpaid cart. Completed sales cannot be cancelled this way. */
    public synchronized boolean cancelSale(Buyer buyer) {
        if (buyer == null || buyer.getBuyerCart() == null) {
            return false;
        }

        Cart cart = buyer.getBuyerCart();
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
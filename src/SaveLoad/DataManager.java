package SaveLoad;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Products.Product;
import Products.ProductDatabase;
import inventoryManagementSystem.InventorySystem;
import inventoryManagementSystem.StockRecordedChange;
import login.LoginManager;
import transaction.SalesTransactionSystem;
import transaction.Receipt;
import transaction.ReceiptItem;
import users.Buyer;
import users.BuyerDatabase;
import users.Source;
import users.SourceDatabase;
import users.User;

import inventoryManagementSystem.InventoryOfProduct;
import java.time.LocalDateTime;

import java.io.IOException;


public class DataManager {
	
	private ProductDatabase products;
	private InventorySystem inventory;
	private BuyerDatabase buyers;
	private SourceDatabase sources;
	private LoginManager login;
	private SalesTransactionSystem sale;
	
	public DataManager(ProductDatabase p, InventorySystem s, BuyerDatabase b, SourceDatabase so, LoginManager l, SalesTransactionSystem sa) {
		this.products = p;
		this.inventory = s;
		this.buyers = b;
		this.sources = so;
		this.login = l;
		this.sale = sa;
	}
	
	public boolean saveProducts() { 
		File file = new File("src/SaveLoad/Products.txt");
		
		try(BufferedWriter writer = Files.newBufferedWriter(file.toPath())){
			
			List<Product> productsToBeSaved = products.getAllProducts();
			
			for (Product product : productsToBeSaved) {
				writer.write(product.viewState());
				writer.newLine();
				
			}
			
			return true;
			
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
			
		}
		
		
	}
	
	public boolean loadProducts() {
		
		File file = new File("src/SaveLoad/Products.txt");
		
		try(BufferedReader reader = Files.newBufferedReader(file.toPath())){
			
			String line = reader.readLine();
			
			while (line != null) {
				String[] extractedData = line.split("\\|", -1);
				
				String code = extractedData[0];
				String name = extractedData[1];
				String description = extractedData[2];
				String source = extractedData[3];
				double cost = Double.parseDouble(extractedData[4]);
				double rawPrice = Double.parseDouble(extractedData[5]);
				double marketPrice = Double.parseDouble(extractedData[6]);
				
				
				//polymorphism, this calls the other product constructor
				Product product = new Product(code, name, description, source, cost, rawPrice, marketPrice);
				products.addProduct(product);
				
				line = reader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	public boolean saveInventoryProducts() {
		File file = new File("src/SaveLoad/Inventory.txt");
		
		try(BufferedWriter writer = Files.newBufferedWriter(file.toPath())){
			
			ArrayList<InventoryOfProduct> inventoryOfProducts = inventory.getAllInventory();
			
			for (InventoryOfProduct productStock : inventoryOfProducts) {
				
				String data = productStock.getProduct().viewState() + "|" + productStock.getProductStockInKG() + "|" + productStock.getLastUpdated().toString();		
				writer.write(data);
				writer.newLine();
				
			}
			
			return true;
			
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
			
		}
		

	}
	
	public boolean loadInventoryProducts() {
		
		File file = new File("src/SaveLoad/Inventory.txt");
		
		try(BufferedReader reader = Files.newBufferedReader(file.toPath())){
			
			String line = reader.readLine();
			
			while (line != null) {
				String[] extractedData = line.split("\\|", -1);
				
				String code = extractedData[0];				
				double ProductStockInKG = Double.parseDouble(extractedData[7]);
				LocalDateTime lastUpdated = LocalDateTime.parse(extractedData[8]);
				
				
				Product product = products.searchProduct(code);
				boolean success = inventory.restoreInventoryRecord(product, ProductStockInKG, lastUpdated);
				
				line = reader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean saveInventoryHistory() {
		File file = new File("src/SaveLoad/StockRecordedHistory.txt");
		
		try(BufferedWriter writer = Files.newBufferedWriter(file.toPath())){
			
			ArrayList<StockRecordedChange> records = inventory.getAllHistory();
			
			for (StockRecordedChange record : records) {
				String data = record.getRecordId() 
						+ "|" + record.getProduct().getProductCode()
						+ "|" + record.getChangeType() 
						+ "|" + record.getPreviousStock() 
						+ "|" + record.getNewStock() 
						+ "|" + record.getTime().toString()
						+ "|" + record.getPerformedBy()
						+ "|" + record.getExplanation();
				
				writer.write(data);
				writer.newLine();
				
			}
			
			return true;
			
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
			
		}
		
	}
	
	public boolean loadInventoryHistory() {
		
		File file = new File("src/SaveLoad/StockRecordedHistory.txt");
		
		try(BufferedReader reader = Files.newBufferedReader(file.toPath())){
			
			String line = reader.readLine();
			
			while (line != null) {
				String[] extractedData = line.split("\\|", -1);
				
				String code = extractedData[0];
				String productCode = extractedData[1];
				
				Product restoredProduct = products.searchProduct(productCode);
				
				String changeType = extractedData[2];
				
				double previousStock = Double.parseDouble(extractedData[3]);
				double newStock = Double.parseDouble(extractedData[4]);
				
				LocalDateTime time = LocalDateTime.parse(extractedData[5]);
				
				String performedBy = extractedData[6];
				String explanation = extractedData[7];		
				
				//polymorphism, this calls the other product constructor
				StockRecordedChange restored = new StockRecordedChange(code, restoredProduct, changeType, previousStock, newStock, time, performedBy, explanation);
				boolean success = inventory.restoreInventoryHistory(restored);
				
				line = reader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public boolean saveBuyers() {
		File file = new File("src/SaveLoad/Buyers.txt");
		
		try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
			
			ArrayList<Buyer> buyersToBeSaved = buyers.returnBuyerList();
			
			for (Buyer buyer : buyersToBeSaved) {
	                String data = buyer.getUserName()
	                        + "|" + buyer.getPassword()
	                        + "|" + buyer.getName()
	                        + "|" + buyer.getBudget();
	                
	                writer.write(data);
	                writer.newLine();
	                
			}
			
			return true;
			
		} catch (IOException e) {            
			e.printStackTrace();	            
			return false;	        
		}
			
	}
	
	public boolean loadBuyers() {
		File file = new File("src/SaveLoad/Buyers.txt");
		
		try(BufferedReader reader = Files.newBufferedReader(file.toPath())){
			
			String line = reader.readLine();
			
			while (line != null) {
				String[] extractedData = line.split("\\|", -1);
				
				String username = extractedData[0];
                String password = extractedData[1];
                String name = extractedData[2];
                double budget = Double.parseDouble(extractedData[3]);
                

                Buyer buyer = new Buyer(username, password, name, budget);
                buyers.addBuyerToDatabase(buyer);
                
                line = reader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	 public boolean saveSources() {
		 File file = new File("src/SaveLoad/Sources2.txt");
		 
		 try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
			 
			 Collection<Source> sourcesToBeSaved = sources.returnSources();
			 
			 for (Source source : sourcesToBeSaved) {
				 
				 HashMap<Product, Double> sourceProducts = source.getSourceProductsAndQuantity();

	                if (sourceProducts.isEmpty()) {
	                    String data = source.getUserName()
	                            + "|" + source.getPassword()
	                            + "|" + source.getName() + "||";

	                    writer.write(data);
	                    writer.newLine();
	                    
	                } else {
	                	
	                    for (Map.Entry<Product, Double> entry : sourceProducts.entrySet()) {
	                        String data = source.getUserName()
	                                + "|" + source.getPassword()
	                                + "|" + source.getName()
	                                + "|" + entry.getKey().getProductCode()
	                                + "|" + entry.getValue();

	                        writer.write(data);
	                        writer.newLine();
	                    }
	                }
	            }

	            return true;

	        } catch (IOException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

	    public boolean loadSources() {
        File file = new File("src/SaveLoad/Sources2.txt");

        HashMap<String, Source> restoredSources = new HashMap<String, Source>();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {

            String line = reader.readLine();

            while (line != null) {

                if (line.trim().isEmpty()) {
                    line = reader.readLine();
                    continue;
                }

                String[] extractedData = line.split("\\|", -1);

                String username = extractedData[0];
                String password = extractedData[1];
                String name = extractedData[2];
                String productCode = extractedData[3];
                String quantityText = extractedData[4];

                if (username.trim().isEmpty() || password.trim().isEmpty() || name.trim().isEmpty()) {
                    System.out.println("Source information is incomplete: " + line);
                    return false;
                }

                // Create each Source object only once.
                Source source = restoredSources.get(username);

                if (source == null) {
                    source = new Source(username, password, name);
                    restoredSources.put(username, source);
                }

                // An empty product code and quantity represent a source with no products.
                if (productCode.trim().isEmpty() && quantityText.trim().isEmpty()) {
                    line = reader.readLine();
                    continue;
                }

                // A product code and quantity must either both exist or both be empty.
                if (productCode.trim().isEmpty() || quantityText.trim().isEmpty()) {
                    System.out.println("Incomplete source product record: " + line);
                    return false;
                }

                Product product = products.searchProduct(productCode);

                if (product == null) {
                    System.out.println("Product not found for source record: " + productCode);
                    return false;
                }

                try {
                    double quantity = Double.parseDouble(quantityText);

                    if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity < 0) {
                        System.out.println("Invalid source quantity: " + quantityText);
                        return false;
                    }

                    source.addProductToSource(product, quantity);

                } catch (NumberFormatException e) {
                    System.out.println("Invalid source quantity: " + quantityText);
                    return false;
                }

                line = reader.readLine();
            }

            // Add the reconstructed sources to SourceDatabase after reading succeeds.
            for (Source source : restoredSources.values()) {
                sources.addSourceToDatabase(source);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


	public boolean saveAccounts() {
		File file = new File("src/SaveLoad/Accounts.txt");
		
		try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
			
			ArrayList<User> accountsToBeSaved = login.getAccounts();
			
			for (User account : accountsToBeSaved) {
				String data = account.getUserID()
						+ "|" + account.getUserName()
						+ "|" + account.getPassword()
						+ "|" + account.getName()
						+ "|" + account.getUserAccessLevel()
						+ "|" + account.getActivated();
				
				writer.write(data);
				writer.newLine();
			}
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean loadAccounts() {
		File file = new File("src/SaveLoad/Accounts.txt");
		
		try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
			
			String line = reader.readLine();
			
			while (line != null) {
				if (line.trim().isEmpty()) {
					line = reader.readLine();
					continue;
				}
				
				String[] extractedData = line.split("\\|", -1);
				
				
				String userID = extractedData[0];
				String username = extractedData[1];
				String password = extractedData[2];
				String name = extractedData[3];
				String accessLevel = extractedData[4];
				boolean activated = Boolean.parseBoolean(extractedData[5]);
				
				User account = new User(username, password, name);
				account.setUserID(userID);
				account.setUserAccessLevel(accessLevel);
				account.setActivated(activated);
				
				login.addAccount(account);
				
				line = reader.readLine();
			}
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean savePendingAccounts() {
		File file = new File("src/SaveLoad/PendingAccounts.txt");
		
		try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
			
			ArrayList<User> pendingToBeSaved = login.getPending();
			
			for (User account : pendingToBeSaved) {
				String userID = account.getUserID();
				if (userID == null) {
					userID = "";
				}
				
				String data = userID
						+ "|" + account.getUserName()
						+ "|" + account.getPassword()
						+ "|" + account.getName()
						+ "|" + account.getUserAccessLevel()
						+ "|" + account.getActivated();
				
				writer.write(data);
				writer.newLine();
			}
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean loadPendingAccounts() {
		File file = new File("src/SaveLoad/PendingAccounts.txt");
		ArrayList<User> restoredPending = new ArrayList<User>();
		
		try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
			
			String line = reader.readLine();
			
			while (line != null) {
				if (line.trim().isEmpty()) {
					line = reader.readLine();
					continue;
				}
				
				String[] extractedData = line.split("\\|", -1);
				
				String userID = extractedData[0];
				String username = extractedData[1];
				String password = extractedData[2];
				String name = extractedData[3];
				String accessLevel = extractedData[4];
				boolean activated = Boolean.parseBoolean(extractedData[5]);
				
				User account = new User(username, password, name);
				account.setUserID(userID);
				account.setUserAccessLevel(accessLevel);
				account.setActivated(activated);
				
				restoredPending.add(account);
				
				line = reader.readLine();
			}
			
			login.setPending(restoredPending);
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean saveSalesReceipts() {
		File receiptFile = new File("src/SaveLoad/Receipt.txt");
		File itemFile = new File("src/SaveLoad/ReceiptItem.txt");
		
		try (BufferedWriter receiptWriter = Files.newBufferedWriter(receiptFile.toPath());
				BufferedWriter itemWriter = Files.newBufferedWriter(itemFile.toPath())) {
			
			Collection<Receipt> receiptsToBeSaved = sale.getAllReceipt();
			
			for (Receipt receipt : receiptsToBeSaved) {
				String saleID = "";
				
				if (receipt.getCart() != null) {
					saleID = receipt.getCart().getSaleID();
				}
				
				User employee = receipt.getEmployee();
				String employeeID = "";
				String employeeUsername = "";
				String employeeName = "";
				
				if (employee != null) {
					employeeID = employee.getUserID();
					employeeUsername = employee.getUserName();
					employeeName = employee.getName();
				}
				
				String data = receipt.getTransactionID()
						+ "|" + saleID
						+ "|" + receipt.getBuyerName()
						+ "|" + receipt.getTotalAmount()
						+ "|" + receipt.getAmountPaid()
						+ "|" + receipt.getCashReceived()
						+ "|" + receipt.getPaymentMethod()
						+ "|" + receipt.getChange()
						+ "|" + receipt.getTransacTime().toString()
						+ "|" + employeeID
						+ "|" + employeeUsername
						+ "|" + employeeName
						+ "|" + receipt.getEmployeeLevel()
						+ "|" + receipt.getPurchaseStatus();
				
				receiptWriter.write(data);
				receiptWriter.newLine();
				
				for (ReceiptItem item : receipt.getItems()) {
					String itemData = receipt.getTransactionID()
							+ "|" + item.getProductCode()
							+ "|" + item.getProductName()
							+ "|" + item.getQuantityInKG()
							+ "|" + item.getUnitPrice();
					
					itemWriter.write(itemData);
					itemWriter.newLine();
				}
			}
			
			return true;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean loadSalesReceipts() {
		File receiptFile = new File("src/SaveLoad/Receipt.txt");
		File itemFile = new File("src/SaveLoad/ReceiptItem.txt");
		
		HashMap<String, ArrayList<ReceiptItem>> restoredItems =
				new HashMap<String, ArrayList<ReceiptItem>>();
		
		try (BufferedReader itemReader = Files.newBufferedReader(itemFile.toPath())) {
			
			String line = itemReader.readLine();
			
			while (line != null) {
				if (line.trim().isEmpty()) {
					line = itemReader.readLine();
					continue;
				}
				
				String[] extractedData = line.split("\\|", -1);
				
				String receiptID = extractedData[0];
				String productCode = extractedData[1];
				String productName = extractedData[2];
				double quantity = Double.parseDouble(extractedData[3]);
				double unitPrice = Double.parseDouble(extractedData[4]);
				
				ReceiptItem item = new ReceiptItem(productCode, productName, quantity, unitPrice);
				ArrayList<ReceiptItem> items = restoredItems.get(receiptID);
				
				if (items == null) {
					items = new ArrayList<ReceiptItem>();
					restoredItems.put(receiptID, items);
				}
				
				items.add(item);
				line = itemReader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		}
		
		try (BufferedReader receiptReader = Files.newBufferedReader(receiptFile.toPath())) {
			
			String line = receiptReader.readLine();
			
			while (line != null) {
				if (line.trim().isEmpty()) {
					line = receiptReader.readLine();
					continue;
				}
				
				String[] extractedData = line.split("\\|", -1);
				
				String receiptID = extractedData[0];
				String saleID = extractedData[1];
				String buyerName = extractedData[2];
				double totalAmount = Double.parseDouble(extractedData[3]);
				double amountPaid = Double.parseDouble(extractedData[4]);
				double cashReceived = Double.parseDouble(extractedData[5]);
				String paymentMethod = extractedData[6];
				double change = Double.parseDouble(extractedData[7]);
				LocalDateTime transactionTime = LocalDateTime.parse(extractedData[8]);
				String employeeID = extractedData[9];
				String employeeUsername = extractedData[10];
				String employeeName = extractedData[11];
				String employeeLevel = extractedData[12];
				String purchaseStatus = extractedData[13];
				
				User employee = null;
				
				if (!employeeUsername.trim().isEmpty()) {
					employee = new User(employeeUsername, "", employeeName);
					employee.setUserID(employeeID);
					employee.setUserAccessLevel(employeeLevel);
					employee.setActivated(true);
				}
				
				ArrayList<ReceiptItem> items = restoredItems.get(receiptID);
				
				if (items == null) {
					items = new ArrayList<ReceiptItem>();
				}
				
				boolean restored = sale.restoreReceipt(receiptID, saleID, buyerName, totalAmount, amountPaid, cashReceived, paymentMethod, change, transactionTime, employee, employeeLevel, purchaseStatus, items);
				
				if (!restored) {
					System.out.println("Unable to restore receipt: " + receiptID);
					return false;
				}
				
				line = receiptReader.readLine();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		} catch (java.time.format.DateTimeParseException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
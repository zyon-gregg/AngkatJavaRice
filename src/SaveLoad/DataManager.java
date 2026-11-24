package SaveLoad;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
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
import users.Buyer;
import users.BuyerDatabase;
import users.Source;
import users.SourceDatabase;

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
	        
	        boolean loadSuccessful = true;

	        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {

	            String line = reader.readLine();

	            while (line != null) {
	            	String[] extractedData = line.split("\\|", -1);
	            	
	            	String username = extractedData[0];
                    String password = extractedData[1];
                    String name = extractedData[2];
                    String productCode = extractedData[3];
                    double quantity = Double.parseDouble(extractedData[4]);
                    Product product = products.searchProduct(productCode);
                    Source source = restoredSources.get(username);
                    
                    source.addProductToSource(product, quantity);

	                line = reader.readLine();
	            }

	            for (Source source : restoredSources.values()) {
	                sources.addSourceToDatabase(source);
	            }

	            return loadSuccessful;

	        } catch (IOException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }

}

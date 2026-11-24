package SaveLoad;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import Products.Product;
import Products.ProductDatabase;
import inventoryManagementSystem.InventorySystem;
import login.LoginManager;
import transaction.SalesTransactionSystem;
import users.BuyerDatabase;
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
				String name = extractedData[1];
				String description = extractedData[2];
				String source = extractedData[3];
				double cost = Double.parseDouble(extractedData[4]);
				double rawPrice = Double.parseDouble(extractedData[5]);
				double marketPrice = Double.parseDouble(extractedData[6]);
				
				double ProductStockInKG = Double.parseDouble(extractedData[7]);
				LocalDateTime lastUpdated = LocalDateTime.parse(extractedData[8]);
				
				
				
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

}

package SaveLoad;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;

import Products.ProductDatabase;
import inventoryManagementSystem.InventorySystem;
import login.LoginManager;
import transaction.SalesTransactionSystem;
import users.BuyerDatabase;
import users.SourceDatabase;

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
	
	public static void readProductFromFile() { 
		
		/**
		
		try(){
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		**/
		
		
	}
	
	public static void saveProductToFile() {
		
	}

}

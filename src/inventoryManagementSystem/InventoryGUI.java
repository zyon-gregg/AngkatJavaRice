package inventoryManagementSystem;

public class InventoryGUI {
	
	//Igii, pwede mo na dito icode yung GUI
	
	//If I were to recommend the GUI layout
	
	//I think there should be a way to check sinong nagaaaccess ng inventory gui. This needs to know kung manager ba or clerk ang nagaccess para may limitations tayo.
	
	//Display ng total inventory records
	//Display ng total number of products that are low on stock
	// Display ng number of products that are out of stock.
	
	//Meron ako netong 3 ^  na helper functions. You may inspect the code sa InventorySystem.java
	
	//Then pwede ka magadd ng table view. Columns may include yung product code, name, current stock (in kg), last updated
	
	//just call lang getAllinventory(). This returns an array list for your table view hehe.
	
	//I suggest may parang field or window for restock. Pwede ka mag use ng text field to get text inputs from user
	//selects a product (by product Id), enter niya quantity, username ni employee, explanation.
	//then call lang InventorySystem.restockProduct(insert parameters)
	//restocking can only be done by managers
	
	//you can also add a field or window for adjust stock
	//bale select siya ng product, enter the stock quantity na gustong ipaedit, username ng user, explanation
	//then just call InventorySyste.editStock(insert parameters)
	
	//So either manager and clerk ang magaccess nitong inventory GUI. Make sure na bawal mag restock or adjust stock si clerk. Si manager lang pwede.
	//Thus you may need to find a way how our gui can know if manager or clerk type ang user.
	
	//Pwede mo rin idisplay products that are low on stock or out of stock. May helper functions ako para dito that returns an array list. Decide how will you display it (could be table view or other)
	//InventorySystem.getProductsLowOnStock() and InventorySystem.getProductsOutOfStock();
	
	//Oh, also display rin yung stock history. Our stock history shows kung anong nangyari in our inventory
	//Parang audit window ganun.
	//Meron na rin akong helpers for that para madisplay mo sila
	//InventorySystem.getAllHistory and InventorySystem.getHistoryByProduct();
	
	//ADDITIONAL NOTES:
	//No modifying ng stock quantities through the setter ng inventoryOfProduct. I already provided functions sa InventorySystem. Pareview nalang.
	//Refresh the window after a succesful stock operation
	//pwede ka magdisplay ng alert window na nagiindicate na success or error.



}

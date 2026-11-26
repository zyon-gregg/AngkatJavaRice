package transaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import users.User;

public class Receipt {
	//Receipt attributes
    private String transactionID;
    private Cart cart;
    private List<ReceiptItem> items = Collections.emptyList();
    private Double totalAmount = 0.0;
    private Double amountPaid;
    private Double cashReceived = 0.0;
    private String paymentMethod;
    private Double change = 0.0;
    private LocalDateTime transacTime;
    private User employee;
    private String buyerName;
    private String employeeLevel;
    private String purchaseStatus;

    Receipt() {
    }
    //getters and setters
    public String getTransactionID() { 
    	return transactionID; 	
    }
    
    public void setTransactionID(String transactionID) { 
    	this.transactionID = transactionID; 
    }

    public Cart getCart() { 
    	return cart; 	
    }
    
    public void setCart(Cart cart) { 
    	this.cart = cart;  	
    }

    
    public List<ReceiptItem> getItems() { 
    	return items; 
    }

    void setItems(List<ReceiptItem> items) {
        if (items == null) {
            this.items = Collections.emptyList();
        } else {
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
        }
    }

    public Double getTotalAmount() { 
    	return totalAmount;   	
    }
    void setTotalAmount(Double totalAmount) { 
    	this.totalAmount = totalAmount;   	
    }

    //Amount applied to settle the sale. This equals the sale total.
    public Double getAmountPaid() { 
    	return amountPaid; 
    }
    public void setAmountPaid(Double amountPaid) { 
    	this.amountPaid = amountPaid;  	
    }

    // Cash tendered by the customer. Zero for simulated cashless payments.
    public Double getCashReceived() { 
    	return cashReceived;    	
    }
    public void setCashReceived(Double cashReceived) { 
    	this.cashReceived = cashReceived;   	
    }

    public String getPaymentMethod() { 
    	return paymentMethod;   	
    }
    public void setPaymentMethod(String paymentMethod) { 
    	this.paymentMethod = paymentMethod; 	
    }

    public Double getChange() { 
    	return change;   	
    }
    public void setChange(Double change) {
    	this.change = change; 	
    }

    public LocalDateTime getTransacTime() { 
    	return transacTime; 	
    }
    public void setTransacTime(LocalDateTime transacTime) { 
    	this.transacTime = transacTime;  	
    }

    public String getBuyerName() { 
    	return buyerName;  	
    }
    void setBuyerName(String buyerName) { 
    	this.buyerName = buyerName; 
    }

    public User getEmployee() { 
    	return employee;
    }
    public void setEmployee(User employee) { 
    	this.employee = employee;    	
    }

    public String getEmployeeLevel() { 
    	return employeeLevel; 
    }
    public void setEmployeeLevel(String employeeLevel) { 
    	this.employeeLevel = employeeLevel; 
    }

    public String getPurchaseStatus() { 
    	return purchaseStatus; 
    }
    public void setPurchaseStatus(String purchaseStatus) { 
    	this.purchaseStatus = purchaseStatus; 
    }
}
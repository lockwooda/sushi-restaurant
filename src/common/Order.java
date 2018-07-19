package common;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Order class that models an order for a particular client, including the contents of the order
 * Made by Alex Lockwood
 */

public class Order extends Model implements Serializable {

    private boolean bOrderComplete;
    private User uCustomer;
    private HashMap<Dish, Number> hmClientOrder;
    private String szStatus;

    //constructor
    public Order(User uCustomer) {
        this.setOrderCompletion(false);
        this.setCustomer(uCustomer);
        this.setStatus("Waiting on Delivery");
        this.hmClientOrder = new HashMap<>();
    }

    //getters
    @Override
    public String getName() {
        return this.uCustomer.getName();
    }

    public boolean getOrderCompletion() {
        return this.bOrderComplete;
    }

    public User getCustomer() {
        return this.uCustomer;
    }

    public HashMap<Dish, Number> getClientOrder() {
        return this.hmClientOrder;
    }

    public String getStatus() {
        return this.szStatus;
    }

    //setters
    public void setOrderCompletion(boolean bOrderComplete) {
        this.bOrderComplete = bOrderComplete;
    }

    public void setCustomer(User uCustomer) {
        this.notifyUpdate("order-user", this.uCustomer, uCustomer);
        this.uCustomer = uCustomer;
    }

    public void addToOrder(Dish dDish, int iAmount) {
        this.notifyUpdate("order-dish", dDish, iAmount);
        if(!this.getClientOrder().containsKey(dDish)) {
            this.hmClientOrder.put(dDish, iAmount);
        }
        else {
            this.hmClientOrder.replace(dDish, iAmount);
        }
    }

    public void setStatus(String szStatus) {
        this.notifyUpdate("order-status", this.szStatus, szStatus);
        this.szStatus = szStatus;
    }

    public void setOrder(HashMap<Dish, Number> hmClientOrder) {
        this.hmClientOrder = hmClientOrder;
    }
}

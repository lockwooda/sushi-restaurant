package server;

import common.Dish;
import common.Ingredient;
import common.Model;
import common.StockManagement;

import java.util.Random;

/**
 * Staff class that works in the kitchen and creates dishes when instructed by the stock-management class
 * Holds the staff's name and status. Has the stock-management class for ease of access
 * Made by Alex Lockwood
 */
public class Staff extends Model implements Runnable {

    private String szName;
    private String szStatus;
    private StockManagement smStock;
    private final int iUpperBound = 60;
    private final int iLowerBound = 20;

    //constructors
    public Staff(String szName, StockManagement smStock) {
        this.setName(szName);
        this.setStockManagement(smStock);
        this.setStatus("Idle");
    }

    //getters
    @Override
    public String getName() {
        return this.szName;
    }

    public String getStatus() {
        return this.szStatus;
    }

    public StockManagement getStockManagement() {
        return this.smStock;
    }

    //setters
    public void setName(String szName) {
        this.notifyUpdate("name", this.szName, szName);
        this.szName = szName;
    }

    public void setStatus(String szStatus) {
        this.notifyUpdate("status", this.szStatus, szStatus);
        this.szStatus = szStatus;
    }

    private void setStockManagement(StockManagement smStock) {
        this.notifyUpdate("stockmanagement", this.smStock, smStock);
        this.smStock = smStock;
    }

    //main run code
    @Override
    public void run() {
        Random r = new Random();
        //infinitely loop
        while(true) {
            //if no dishes left to make, return to a wait state
            if(this.getStockManagement().getDishesQueue().isEmpty()) {
                synchronized (this.getStockManagement().getDishesList()) {
                    try {
                        this.getStockManagement().getDishesList().wait();
                    }
                    catch (InterruptedException ie) {
                        //if interrupted, kill the thread
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            //whilst there are dishes to make, make dishes
            while(!this.getStockManagement().getDishesQueue().isEmpty()) {
                Dish dCurrentDish = null;
                boolean ingredientsPresent = true;
                //check if there is enough ingredients for the dish, if not then do not make dish
                synchronized(this.getStockManagement().getDishesQueue()) {
                    dCurrentDish = this.getStockManagement().getDishesQueue().peek();
                    for(Ingredient i : dCurrentDish.getRecipe().keySet()) {
                        if(dCurrentDish.getRecipe().get(i).intValue() > this.getStockManagement().getStockLevel(i).intValue()) {
                            ingredientsPresent = false;
                        }
                    }
                    if(ingredientsPresent) {
                        try {
                            dCurrentDish = this.getStockManagement().getDishesQueue().take();
                        }
                        catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }

                //if enough ingredients
                if(ingredientsPresent) {
                    for(Ingredient i : dCurrentDish.getRecipe().keySet()) {
                        this.getStockManagement().setStockLevel(i
                                , this.getStockManagement().getStockLevel(i).intValue() - dCurrentDish.getRecipe().get(i).intValue());
                    }
                    try {
                        this.setStatus("Making Dish: " + dCurrentDish.getName());
                        Thread.currentThread().sleep(1000 * (r.nextInt((iUpperBound - iLowerBound) + 1) + iLowerBound));
                        this.getStockManagement().addToStock(dCurrentDish);
                        this.setStatus("Idle");
                    }
                    catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        }
    }
}

package server;

import common.Ingredient;
import common.Model;
import common.Order;
import common.StockManagement;

public class Drone extends Model implements Runnable {

    private String szStatus;
    private StockManagement smStock;
    private Number iSpeed;

    public Drone(StockManagement smStock, Number iSpeed) {
        this.setStockManagement(smStock);
        this.setStatus("Idle");
        this.setSpeed(iSpeed);
    }

    @Override
    public String getName() {
        return null;
    }

    public StockManagement getStockManagement() {
        return this.smStock;
    }

    public String getStatus() {
        return this.szStatus;
    }

    public Number getSpeed() {
        return this.iSpeed;
    }

    private void setStockManagement(StockManagement smStock) {
        this.notifyUpdate("stockmanagement", this.smStock, smStock);
        this.smStock = smStock;
    }

    private void setStatus(String szStatus) {
        this.notifyUpdate("status", this.getStatus(), szStatus);
        this.szStatus = szStatus;
    }

    private void setSpeed(Number iSpeed) {
        this.iSpeed = iSpeed;
    }

    @Override
    public void run() {
        while(true) {
            //if both queues empty then wait until notified
            if(this.getStockManagement().getIngredientsQueue().isEmpty()
                    && this.getStockManagement().getOrderQueue().isEmpty()) {
                synchronized (this.getStockManagement().getIngredientsList()) {
                    try {
                        this.getStockManagement().getIngredientsList().wait();
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            while(!this.getStockManagement().getIngredientsQueue().isEmpty()
                    || !this.getStockManagement().getOrderQueue().isEmpty()) {
                if(this.getStockManagement().getOrderQueue().size() > 0) {
                    Order oOrder = null;
                    System.out.println("Order Drone Test");
                    synchronized (this.getStockManagement().getOrderQueue()) {
                        try {
                            oOrder = this.getStockManagement().getOrderQueue().take();
                            oOrder.setStatus("Being Delivered");
                            this.setStatus("Delivering Order");
                            this.getStockManagement().getOrderQueue().wait((long)((oOrder.getCustomer().getPostcode().getDistance().doubleValue() / this.getSpeed().doubleValue()) * 60000));
                        }
                        catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        oOrder.setOrderCompletion(true);
                        oOrder.setStatus("Delivered");
                        this.setStatus("Idle");
                    }
                }
                if(this.getStockManagement().getIngredientsQueue().size() > 0){
                    Ingredient inIngredient = null;
                    synchronized (this.getStockManagement().getIngredientsQueue()) {
                        try {
                            inIngredient = this.getStockManagement().getIngredientsQueue().take();
                            this.setStatus("Getting Ingredients: " + inIngredient.getName());
                            this.getStockManagement().getIngredientsQueue().wait((long)((inIngredient.getSupplier().getDistance().doubleValue() / this.getSpeed().doubleValue()) * 60000));
                        }
                        catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    this.getStockManagement().addToStock(inIngredient);
                    this.setStatus("Idle");
                }
            }
        }
    }
}

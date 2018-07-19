package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The stock-management class that handles all the processing of stock for the business
 * Handles what dishes and ingredients are used the sushi business, their stock levels, restock thresholds, restock amounts
 * Also handles queues for dishes which the staff will make and ingredients and orders that drones will collect or deliver
 * Made by Alex Lockwood
 */
public class StockManagement {
    private boolean bRestockDishes;
    private List<Dish> lDishesList;
    private ConcurrentHashMap<Dish, Number> hmDishStocks;
    private ConcurrentHashMap<Dish, Number> hmDishRestockThreshold;
    private ConcurrentHashMap<Dish, Number> hmDishRestockAmount;

    private boolean bRestockIng;
    private List<Ingredient> lIngredientList;
    private ConcurrentHashMap<Ingredient, Number> hmIngredientStocks;
    private ConcurrentHashMap<Ingredient, Number> hmIngredientRestockThreshold;
    private ConcurrentHashMap<Ingredient, Number> hmIngredientRestockAmount;

    private LinkedBlockingQueue<Dish> qDishesToMake;
    private LinkedBlockingQueue<Ingredient> qIngsToCollect;
    private LinkedBlockingQueue<Order> qOrderToDeliver;

    //constructor
    public StockManagement() {
        this.setDishesList(new ArrayList<>());
        this.setIfRestockingDish(true);
        this.setDishStocks(new ConcurrentHashMap<>());
        this.setDishRestockThreshold(new ConcurrentHashMap<>());
        this.setDishRestockAmount(new ConcurrentHashMap<>());

        this.setIngredientsList(new ArrayList<>());
        this.setIfRestockingIng(true);
        this.setIngredientStocks(new ConcurrentHashMap<>());
        this.setIngredientRestockThreshold(new ConcurrentHashMap<>());
        this.setIngredientRestockAmount(new ConcurrentHashMap<>());

        qDishesToMake = new LinkedBlockingQueue<>();
        qIngsToCollect = new LinkedBlockingQueue<>();
        qOrderToDeliver = new LinkedBlockingQueue<>();
    }

    /*
    DISHES GETTERS AND SETTERS
     */

    public boolean getIfRestockingDish() {
        return this.bRestockDishes;
    }

    public List<Dish> getDishesList() {
        return this.lDishesList;
    }

    public ConcurrentHashMap<Dish, Number> getAllDishStockLevels() {
        return this.hmDishStocks;
    }

    public Number getStockLevel(Dish dDish) {
        return this.hmDishStocks.get(dDish);
    }

    public ConcurrentHashMap<Dish, Number> getAllDishRestockThresholds() {
        return this.hmDishRestockThreshold;
    }

    public Number getRestockThreshold(Dish dDish) {
        return this.getAllDishRestockThresholds().get(dDish);
    }

    public ConcurrentHashMap<Dish, Number> getAllDishRestockAmounts() {
        return this.hmDishRestockAmount;
    }

    public Number getRestockAmount(Dish dDish) {
        return this.getAllDishRestockAmounts().get(dDish);
    }

    private void setIfRestockingDish(boolean bRestockDishes) {
        this.bRestockDishes = bRestockDishes;
    }

    private synchronized void setDishesList(List<Dish> lDishesList) {
        this.lDishesList = lDishesList;
    }

    public synchronized Dish addToList(Dish dDish) {
        if(!this.getDishesList().contains(dDish)) {
            this.getDishesList().add(dDish);
            return dDish;
        }
        else return null;
    }

    private synchronized void setDishStocks(ConcurrentHashMap<Dish, Number> hmDishStocks) {
        this.hmDishStocks = hmDishStocks;
    }

    public void setStockLevel(Dish dDish, Number iStock) {
        this.getAllDishStockLevels().put(dDish, iStock);

        //if the stock level is less than the restock level and allowed to restock, add enough dishes to the queue to properly restock
        if(this.getStockLevel(dDish).intValue() < this.getRestockThreshold(dDish).intValue() && this.getIfRestockingDish()) {
            int iCurrentStock = this.getStockLevel(dDish).intValue();
            int iLimit = this.getRestockThreshold(dDish).intValue() + this.getRestockAmount(dDish).intValue();
            for(int i = 0; i + iCurrentStock < iLimit ; i++) {
                this.addToDishesQueue(dDish);
            }
            //notify all staff of new dishes that have arrived
            synchronized (this.getDishesList()) {
                this.getDishesList().notifyAll();
            }
        }
    }

    public synchronized void addToStock(Dish dDish) {
        this.getAllDishStockLevels().put(dDish, this.getStockLevel(dDish).intValue() + 1);
    }

    private synchronized void setDishRestockThreshold(ConcurrentHashMap<Dish, Number> hmDishRestockThreshold) {
        this.hmDishRestockThreshold = hmDishRestockThreshold;
    }

    public synchronized void setRestockThreshold(Dish dDish, Number iThreshold) {
        this.getAllDishRestockThresholds().put(dDish, iThreshold);
    }

    private synchronized void setDishRestockAmount(ConcurrentHashMap<Dish, Number> hmDishRestockAmount) {
        this.hmDishRestockAmount = hmDishRestockAmount;
    }

    public synchronized void setRestockAmount(Dish dDish, Number iThreshold) {
        this.getAllDishRestockAmounts().put(dDish, iThreshold);
    }

    /*
    INGREDIENTS GETTERS AND SETTERS
     */
    private boolean getIfRestockingIng() {
        return this.bRestockIng;
    }

    public List<Ingredient> getIngredientsList() {
        return lIngredientList;
    }

    public ConcurrentHashMap<Ingredient, Number> getAllIngStockLevels() {
        return hmIngredientStocks;
    }

    public Number getStockLevel(Ingredient inFood) {
        return this.getAllIngStockLevels().get(inFood);
    }

    public ConcurrentHashMap<Ingredient, Number> getAllIngRestockThresholds() {
        return hmIngredientRestockThreshold;
    }

    public Number getRestockThreshold(Ingredient inFood) {
        return this.getAllIngRestockThresholds().get(inFood);
    }

    public ConcurrentHashMap<Ingredient, Number> getAllIngRestockAmounts() {
        return hmIngredientRestockAmount;
    }

    public Number getRestockAmount(Ingredient inFood) {
        return this.getAllIngRestockAmounts().get(inFood);
    }

    public void setIfRestockingIng(boolean bRestockIng) {
        this.bRestockIng = bRestockIng;
    }

    private void setIngredientsList(List<Ingredient> lIngredientsList) {
        this.lIngredientList = lIngredientsList;
    }

    public void addToList(Ingredient inFood)  {
        if(!this.getIngredientsList().contains(inFood)) {
            this.getIngredientsList().add(inFood);
        }
    }

    private void setIngredientStocks(ConcurrentHashMap<Ingredient, Number> hmIngredientStocks) {
        this.hmIngredientStocks = hmIngredientStocks;
    }

    public void setStockLevel(Ingredient inFood, Number iStock) {
        this.getAllIngStockLevels().put(inFood, iStock);

        //if the stock level is less than the threshold and allowed to restock, add the ingredient to the queue for collection
        if(this.getStockLevel(inFood).intValue() < this.getRestockThreshold(inFood).intValue() && this.getIfRestockingIng()) {
            synchronized (this.getIngredientsList()) {
                this.addToIngredientsQueue(inFood);
                //notify one drone
                this.getIngredientsList().notify();
            }
        }
    }

    public synchronized void addToStock(Ingredient inFood) {
        this.getAllIngStockLevels().put(inFood, this.getRestockThreshold(inFood).intValue() + this.getRestockAmount(inFood).intValue());
    }

    private void setIngredientRestockThreshold(ConcurrentHashMap<Ingredient, Number> hmIngredientRestockThreshold) {
        this.hmIngredientRestockThreshold = hmIngredientRestockThreshold;
    }

    public void setRestockThreshold(Ingredient inFood, Number iThreshold) {
        this.getAllIngRestockThresholds().put(inFood, iThreshold);
    }

    private void setIngredientRestockAmount(ConcurrentHashMap<Ingredient, Number> hmIngredientRestockAmount) {
        this.hmIngredientRestockAmount = hmIngredientRestockAmount;
    }

    public void setRestockAmount(Ingredient inFood, Number iAmount) {
        this.getAllIngRestockAmounts().put(inFood, iAmount);
    }

    //QUEUES
    public LinkedBlockingQueue<Dish> getDishesQueue() {
        return this.qDishesToMake;
    }

    public LinkedBlockingQueue<Ingredient> getIngredientsQueue() {
        return this.qIngsToCollect;
    }

    public LinkedBlockingQueue<Order> getOrderQueue() {
        return this.qOrderToDeliver;
    }

    public void addToDishesQueue(Dish dDish) {
        this.getDishesQueue().add(dDish);
    }

    public void addToIngredientsQueue(Ingredient inIngredient) {
        this.getIngredientsQueue().add(inIngredient);
    }

    public void addToOrderQueue(Order oOrder) {
        this.getOrderQueue().add(oOrder);
    }
}

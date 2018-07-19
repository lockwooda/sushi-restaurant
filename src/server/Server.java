package server;

import common.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server class that handles all the server-side processing of data, and sends along data to clients
 * Holds all the orders, postcodes, stocks, threads.
 * Made by Alex Lockwood
 */

public class Server implements ServerInterface {

    private CommsServer csComms;
    private StockManagement smStock;
    private List<Postcode> postcodeList;
    private List<Supplier> supplierList;
    private List<User> userList;
    private Map<Staff, Thread> staffThreadMap;
    private Map<Drone, Thread> droneThreadMap;
    private List<Order> ordersList;
    private List<UpdateListener> updateListeners = new ArrayList<>();

    //constructor
    public Server(StockManagement smStock) {
        //if the persistence file exists, use it as a configuration file and load it into the server
        if(new File("persistence.cfg").exists()) {
            try {
                this.loadConfiguration("persistence.cfg");
            }
            catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            }
        }
        else {
            this.smStock = smStock;
            this.setRestockingDishesEnabled(true);
            this.setRestockingIngredientsEnabled(true);

            this.postcodeList = new ArrayList<>();
            this.supplierList = new ArrayList<>();
            this.userList = new ArrayList<>();
            this.staffThreadMap = new HashMap<>();
            this.droneThreadMap = new HashMap<>();
            this.ordersList = new ArrayList<>();
        }

        try {
            csComms = new CommsServer();
        }
        catch (IOException io) {
            io.printStackTrace();
        }

        //wait on a new message, once received process it
        Runnable rMessageListener = () -> {
            while(true) {
                synchronized (csComms) {
                    try {
                        csComms.wait();
                    }
                    catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    Object[] oMessage = csComms.getMessage();
                    Server.this.processMessage(oMessage);
                }
            }
        };

        Thread tMessageListener = new Thread(rMessageListener);
        tMessageListener.start();

        //on shutdown, save all the data to the persistence file
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DataPersistence da = new DataPersistence();
            da.processData(this);
        }));
    }

    //process a configuration file, such that it loads all the items in to the server
    @Override
    public void loadConfiguration(String filename) throws FileNotFoundException {
        Configuration c = new Configuration(filename);
        try {
            c.processFile();
        }
        catch (IOException io) {
            io.printStackTrace();
        }

        this.supplierList = c.getSupplierList();
        this.postcodeList = c.getPostcodeList();
        this.smStock = c.getSmStock();
        this.userList = c.getUserList();

        this.userList = new ArrayList<>();
        for(User u : c.getUserList()) {
            this.userList.add(u);
        }

        //for threaded applications, add the staff to a mapping and start their respective threads
        this.staffThreadMap = new HashMap<>();
        for(Staff s : c.getStaffList()) {
            Thread tNewStaff = new Thread(s);
            tNewStaff.start();
            this.staffThreadMap.put(s, tNewStaff);
        }

        this.droneThreadMap = new HashMap<>();
        for(Drone d : c.getDroneList()) {
            Thread tNewDrone = new Thread(d);
            tNewDrone.start();
            this.droneThreadMap.put(d, tNewDrone);
        }

        for(Ingredient i : this.getStock().getIngredientsList()) {
            if(c.getIngredientStockMap().keySet().contains(i)) {
                this.getStock().setStockLevel(i, c.getIngredientStockMap().get(i));
            }
            else this.getStock().setStockLevel(i, 10);
        }
        for(Dish d : c.getDishStockMap().keySet()) {
            if(c.getDishStockMap().keySet().contains(d)) {
                this.getStock().setStockLevel(d, c.getDishStockMap().get(d));
            }
            else this.getStock().setStockLevel(d, 10);
        }

        this.ordersList = new ArrayList<>();
        for(Order o : c.getOrderList()) {
            this.ordersList.add(o);
        }
    }

    @Override
    public void setRestockingIngredientsEnabled(boolean enabled) {
        smStock.setIfRestockingIng(enabled);
    }

    @Override
    public void setRestockingDishesEnabled(boolean enabled) {
        smStock.setIfRestockingIng(enabled);
    }

    @Override
    public void setStock(Dish dish, Number stock) {
        smStock.setStockLevel(dish, stock);
    }

    @Override
    public void setStock(Ingredient ingredient, Number stock) {
        smStock.setStockLevel(ingredient, stock);
    }

    @Override
    public List<Dish> getDishes() {
        return smStock.getDishesList();
    }

    @Override
    public Dish addDish(String name, String description, Number price, Number restockThreshold, Number restockAmount) {
        Dish dNewDish = new Dish(name, description, (int) price);
        if(smStock.addToList(dNewDish) != null) {
            smStock.setRestockThreshold(dNewDish, restockThreshold);
            smStock.setRestockAmount(dNewDish, restockAmount);
            smStock.setStockLevel(dNewDish, 0);
            return dNewDish;
        }
        else return null;
    }

    @Override
    public void removeDish(Dish dish) throws UnableToDeleteException {
        try {
            if(!smStock.getDishesList().contains(dish)
                    || !smStock.getAllDishStockLevels().containsKey(dish)
                    || !smStock.getAllDishRestockThresholds().containsKey(dish)
                    || !smStock.getAllDishRestockAmounts().containsKey(dish)) {
                throw new UnableToDeleteException("Dish not present");
            }
            else {
                smStock.getDishesList().remove(dish);
                smStock.getAllDishStockLevels().remove(dish);
                smStock.getAllDishRestockThresholds().remove(dish);
                smStock.getAllDishRestockAmounts().remove(dish);
            }
        }
        catch (UnableToDeleteException utde) {
            throw utde;
        }
    }

    @Override
    public void addIngredientToDish(Dish dish, Ingredient ingredient, Number quantity) {
        dish.addIngredient(ingredient, (int) quantity);
    }

    @Override
    public void removeIngredientFromDish(Dish dish, Ingredient ingredient) {
        dish.removeIngredient(ingredient);
    }

    @Override
    public void setRecipe(Dish dish, Map<Ingredient, Number> recipe) {
        dish.setRecipe((HashMap<Ingredient, Number>) recipe);
    }

    @Override
    public void setRestockLevels(Dish dish, Number restockThreshold, Number restockAmount) {
        smStock.setRestockThreshold(dish, restockThreshold);
        smStock.setRestockAmount(dish, restockAmount);
    }

    @Override
    public Number getRestockThreshold(Dish dish) {
        return smStock.getRestockThreshold(dish);
    }

    @Override
    public Number getRestockAmount(Dish dish) {
        return smStock.getRestockAmount(dish);
    }

    @Override
    public Map<Ingredient, Number> getRecipe(Dish dish) {
        return dish.getRecipe();
    }

    @Override
    public Map<Dish, Number> getDishStockLevels() {
        return smStock.getAllDishStockLevels();
    }

    @Override
    public List<Ingredient> getIngredients() {
        return smStock.getIngredientsList();
    }

    @Override
    public Ingredient addIngredient(String name, String unit, Supplier supplier, Number restockThreshold, Number restockAmount) {
        Ingredient inNewIng = new Ingredient(name, unit, supplier);
        smStock.addToList(inNewIng);
        smStock.setRestockThreshold(inNewIng, restockThreshold);
        smStock.setRestockAmount(inNewIng, restockAmount);
        smStock.setStockLevel(inNewIng, 0);

        return inNewIng;
    }

    @Override
    public void removeIngredient(Ingredient ingredient) throws UnableToDeleteException {
        try {
            if(!smStock.getIngredientsList().contains(ingredient)
                    || !smStock.getAllIngStockLevels().containsKey(ingredient)
                    || !smStock.getAllIngRestockThresholds().containsKey(ingredient)
                    || !smStock.getAllIngRestockAmounts().containsKey(ingredient)) {
                throw new UnableToDeleteException("Dish not present in a data structure");
            }
            else {
                smStock.getIngredientsList().remove(ingredient);
                smStock.getAllIngStockLevels().remove(ingredient);
                smStock.getAllIngRestockThresholds().remove(ingredient);
                smStock.getAllIngRestockAmounts().remove(ingredient);
            }
        }
        catch (UnableToDeleteException utde) {
            throw utde;
        }
    }

    @Override
    public void setRestockLevels(Ingredient ingredient, Number restockThreshold, Number restockAmount) {
        smStock.setRestockThreshold(ingredient, restockThreshold);
        smStock.setRestockAmount(ingredient, restockAmount);
    }

    @Override
    public Number getRestockThreshold(Ingredient ingredient) {
        return smStock.getRestockThreshold(ingredient);
    }

    @Override
    public Number getRestockAmount(Ingredient ingredient) {
        return smStock.getRestockAmount(ingredient);
    }

    @Override
    public Map<Ingredient, Number> getIngredientStockLevels() {
        return smStock.getAllIngStockLevels();
    }

    @Override
    public List<Supplier> getSuppliers() {
        return this.supplierList;
    }

    @Override
    public Supplier addSupplier(String name, Number distance) {
        Supplier sNewSupplier = new Supplier(name, distance);
        this.getSuppliers().add(sNewSupplier);
        return sNewSupplier;
    }

    @Override
    public void removeSupplier(Supplier supplier) throws UnableToDeleteException {
        if(this.getSuppliers().contains(supplier)) {
            this.getSuppliers().remove(supplier);
        }
        else throw new UnableToDeleteException("Supplier not present");
    }

    @Override
    public Number getSupplierDistance(Supplier supplier) {
        return supplier.getDistance();
    }

    @Override
    public List<Drone> getDrones() {
        ArrayList<Drone> droneList = new ArrayList<>();
        for(Drone d : this.droneThreadMap.keySet()) {
            droneList.add(d);
        }

        return droneList;
    }

    @Override
    public Drone addDrone(Number speed) {
        Drone dNewDrone = new Drone(this.getStock(), speed);
        Thread tNewDrone = new Thread(dNewDrone);
        tNewDrone.start();

        this.droneThreadMap.put(dNewDrone, tNewDrone);
        return dNewDrone;
    }

    @Override
    public void removeDrone(Drone drone) throws UnableToDeleteException {
        if(this.getDrones().contains(drone)) {
            this.droneThreadMap.get(drone).interrupt();
            this.droneThreadMap.remove(drone);
        }
        else throw new UnableToDeleteException("Drone not present");
    }

    @Override
    public Number getDroneSpeed(Drone drone) {
        return drone.getSpeed();
    }

    @Override
    public String getDroneStatus(Drone drone) {
        return drone.getStatus();
    }

    @Override
    public List<Staff> getStaff() {
        ArrayList<Staff> staffList = new ArrayList<>();
        for(Staff s : this.staffThreadMap.keySet()) {
            staffList.add(s);
        }

        return staffList;
    }

    @Override
    public Staff addStaff(String name) {
        Staff dNewStaff = new Staff(name, this.getStock());
        Thread tNewStaff = new Thread(dNewStaff);
        tNewStaff.start();

        this.staffThreadMap.put(dNewStaff, tNewStaff);
        return dNewStaff;
    }

    @Override
    public void removeStaff(Staff staff) throws UnableToDeleteException {
        if(this.getStaff().contains(staff)) {
            this.droneThreadMap.get(staff).interrupt();
            this.droneThreadMap.remove(staff);
        }
        else throw new UnableToDeleteException("Drone not present");
    }

    @Override
    public String getStaffStatus(Staff staff) {
        return staff.getStatus();
    }

    @Override
    public List<Order> getOrders() {
        return this.ordersList;
    }

    @Override
    public void removeOrder(Order order) throws UnableToDeleteException {
        if(this.getOrders().contains(order)) {
            this.getOrders().remove(order);
        }
        else throw new UnableToDeleteException("Order not present");
    }

    @Override
    public Number getOrderDistance(Order order) {
        return order.getCustomer().getPostcode().getDistance();
    }

    @Override
    public boolean isOrderComplete(Order order) {
        return order.getOrderCompletion();
    }

    @Override
    public String getOrderStatus(Order order) {
        return order.getStatus();
    }

    @Override
    public Number getOrderCost(Order order) {
        double dCost = 0;
        for(Dish dDish : order.getClientOrder().keySet()) {
            dCost += dDish.getPrice() * order.getClientOrder().get(dDish).intValue();
        }
        return dCost;
    }

    @Override
    public List<Postcode> getPostcodes() {
        return this.postcodeList;
    }

    @Override
    public void addPostcode(String code, Number distance) {
        Postcode pNewPostcode = new Postcode(code, distance);
        this.postcodeList.add(pNewPostcode);
    }

    @Override
    public void removePostcode(Postcode postcode) throws UnableToDeleteException {
        if(this.postcodeList.contains(postcode)) {
            this.postcodeList.remove(postcode);
        }
        else {
            throw new UnableToDeleteException("Postcode not present");
        }
    }

    @Override
    public List<User> getUsers() {
        return this.userList;
    }

    @Override
    public void removeUser(User user) throws UnableToDeleteException {
        if(this.getUsers().contains(user)) {
            this.getUsers().remove(user);
        }
        else throw new UnableToDeleteException("User not present");
    }

    //register a user into the system
    private void registerUser(Object[] oMessage) {
        String[] szSplitMessage = ((String) oMessage[0]).split(":");
        for(User u : this.getUsers()) {
            if(u.getName().equals(szSplitMessage[2])) {
                this.csComms.sendMessage(Integer.parseInt(szSplitMessage[1]), null);
                return;
            }
        }

        //if the username doesn't exist, add the user to the list
        User uNewUser = new User(szSplitMessage[2], szSplitMessage[3], szSplitMessage[4], (Postcode) oMessage[1]);
        this.getUsers().add(uNewUser);
        this.csComms.sendMessage(Integer.parseInt(szSplitMessage[1]), uNewUser);
        this.notifyUpdate();
    }

    //login a user, based upon their password
    private void loginUser(String[] szMessage) {
        for(User u : this.getUsers()) {
            //if the username and password match, sign them in
            if(u.getName().equals(szMessage[2])
                    && u.getPassword().equals(szMessage[3])) {
                this.csComms.sendMessage(Integer.parseInt(szMessage[1]), u);
                return;
            }
        }

        this.csComms.sendMessage(Integer.parseInt(szMessage[1]), null);
    }

    //given a user's basket, send it into an order to be delivered
    private void checkoutBasket(Object[] oMessage) {
        Order newOrder = new Order((User) oMessage[1]);
        newOrder.setOrder((HashMap<Dish, Number>) oMessage[2]);
        for(Dish dDish : ((HashMap<Dish, Number>) oMessage[2]).keySet()) {
            for(Dish dDishName : this.getStock().getDishesList()) {
                if(dDish.getName().equals(dDishName.getName())) {
                    this.getStock().setStockLevel(dDishName
                            , this.getStock().getStockLevel(dDishName).intValue() - newOrder.getClientOrder().get(dDish).intValue());
                }
            }
        }
        this.getOrders().add(newOrder);
        synchronized (this.getStock().getIngredientsList()) {
            this.getStock().addToOrderQueue(newOrder);
            this.getStock().getIngredientsList().notify();
        }

        this.csComms.sendMessage(Integer.parseInt(((String) oMessage[0]).split(":")[1]), newOrder);
        this.notifyUpdate();
    }

    //get a set of users created by a user
    private void getUserOrders(Object[] oMessage) {
        List<Order> userOrders = new ArrayList<>();
        for(Order o : this.getOrders()) {
            if(o.getCustomer() == oMessage[1]) {
                userOrders.add(o);
            }
        }
        this.csComms.sendMessage(Integer.parseInt(((String) oMessage[0]).split(":")[1]), userOrders);
    }

    //method that processes the mesages given to it by the users
    private void processMessage(Object[] oMessage) {
        String[] szSplitMessage = ((String) oMessage[0]).split(":");

        //change how the data is processed based upon the identifier in the first part of the message
        switch(szSplitMessage[0]) {
            case "REGISTER":
                this.registerUser(oMessage);
                break;
            case "LOGIN":
                this.loginUser(szSplitMessage);
                break;
            case "GETPOSTCODES":
                this.csComms.sendMessage(Integer.parseInt(szSplitMessage[1])
                        , this.getPostcodes());
                break;
            case "GETDISHES":
                this.csComms.sendMessage(Integer.parseInt(szSplitMessage[1])
                        , this.getDishes());
                break;
            case "GETORDERS":
                this.getUserOrders(oMessage);
                break;
            case "CHECKOUT":
                this.checkoutBasket(oMessage);
                break;
            case "CANCELORDER":
                this.getOrders().remove(oMessage[1]);
                break;
        }
        this.notifyUpdate();
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        updateListeners.add(listener);
    }

    @Override
    public void notifyUpdate() {
        for(UpdateListener u : updateListeners) {
            u.updated(new UpdateEvent());
        }
    }

    public StockManagement getStock() {
        return this.smStock;
    }
}
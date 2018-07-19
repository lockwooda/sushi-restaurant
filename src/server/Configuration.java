package server;

import common.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration class to process a configuration file and return a list of models to use in the server
 * Made by Alex Lockwood
 */

public class Configuration {
    private String szPath;

    private StockManagement smStock;
    private List<Supplier> supplierList;
    private List<User> userList;
    private List<Postcode> postcodeList;
    private List<Staff> staffList;
    private List<Drone> droneList;
    private List<Order> orderList;
    private Map<Dish, Integer> dishStockMap;
    private Map<Ingredient, Integer> ingredientStockMap;

    //constructor
    public Configuration(String szFileName) throws FileNotFoundException {
         if(!new File(szFileName).exists()) throw new FileNotFoundException();

         szPath = szFileName;
         smStock = new StockManagement();
         supplierList = new ArrayList<>();
         userList = new ArrayList<>();
         postcodeList = new ArrayList<>();
         staffList = new ArrayList<>();
         droneList = new ArrayList<>();
         orderList = new ArrayList<>();
         dishStockMap = new HashMap<>();
         ingredientStockMap = new HashMap<>();
    }

    //method to process the file parsed through the constructor
    public void processFile() throws IOException {
        //read each line and process it
        try (BufferedReader br = new BufferedReader(new FileReader(new File(szPath)))) {
            String szInput;
            //while there is a line to process, check it
            while((szInput = br.readLine()) != null) {
                if(!szInput.equals("")) {
                    String[] szSplit = szInput.split(":");
                    //change the processing based upon identifier
                    switch(szSplit[0]) {
                        case "SUPPLIER":
                            this.getSupplierList().add(new Supplier(szSplit[1], Integer.parseInt(szSplit[2])));
                            break;
                        case "INGREDIENT":
                            this.processIngredient(szSplit);
                            break;
                        case "DISH":
                            this.processDish(szSplit);
                            break;
                        case "USER":
                            this.processUser(szSplit);
                            break;
                        case "POSTCODE":
                            this.processPostcode(szSplit);
                            break;
                        case "STAFF":
                            this.staffList.add(new Staff(szSplit[1], smStock));
                            break;
                        case "DRONE":
                            this.droneList.add(new Drone(smStock, Integer.parseInt(szSplit[1])));
                            break;
                        case "ORDER":
                            this.processOrder(szSplit);
                            break;
                        case "STOCK":
                            this.processStock(szSplit);
                            break;
                    }
                }
            }
        }
    }

    //add an ingredient with its corresponding supplier
    public void processIngredient(String[] szSplit) {
        for(Supplier s : this.getSupplierList()) {
            if(s.getName().equals(szSplit[3])) {
                this.getSmStock().getIngredientsList().add(new Ingredient(szSplit[1], szSplit[2], s));
                this.getSmStock().addToList(this.getSmStock().getIngredientsList().get(this.getSmStock().getIngredientsList().size() - 1));
                this.getSmStock().setRestockThreshold(this.getSmStock().getIngredientsList().get(this.getSmStock().getIngredientsList().size() - 1)
                        , Integer.parseInt(szSplit[4]));
                this.getSmStock().setRestockAmount(this.getSmStock().getIngredientsList().get(this.getSmStock().getIngredientsList().size() - 1)
                        , Integer.parseInt(szSplit[5]));
                return;
            }
        }
    }

    //process a dish with its corresponding ingredients in the
    public void processDish(String[] szSplit) {
        Dish dNewDish = new Dish(szSplit[1], szSplit[2], Double.valueOf(szSplit[3]));
        this.getSmStock().addToList(dNewDish);
        this.getSmStock().setRestockThreshold(dNewDish, Integer.parseInt(szSplit[4]));
        this.getSmStock().setRestockAmount(dNewDish, Integer.parseInt(szSplit[5]));

        //process the ingredients list
        String[] szRecipe = szSplit[6].split(",");
        for(String szIngredient : szRecipe) {
            if(szIngredient.matches("[0-9]* \\* [A-Za-z ]*")) {
                Ingredient inNewIngd = null;

                //patterns to check the ingredients quantity and actual ingredient
                Matcher mMatcher;
                Pattern pQuantity = Pattern.compile("[0-9]* \\*");
                Pattern pIngredient = Pattern.compile("\\* [A-Za-z ]*");
                mMatcher = pIngredient.matcher(szIngredient);
                if(mMatcher.find()) {
                    String szName = mMatcher.group(0).substring(2);
                    boolean bActualIng = false;
                    for(Ingredient i : this.getSmStock().getIngredientsList()) {
                        if(i.getName().equals(szName)) {
                            inNewIngd = i;
                            bActualIng = true;
                        }
                    }

                    if(!bActualIng) return;
                }

                //add the ingredient to the recipe of the dish
                mMatcher = pQuantity.matcher(szIngredient);
                if(mMatcher.find()) {
                    dNewDish.addIngredient(inNewIngd
                            , Integer.parseInt(mMatcher.group(0).substring(0, mMatcher.group(0).length() - 2)));
                }
            }
        }
    }

    //if there is a matching postcode, add them to the list
    public void processUser(String[] szSplit) {
        for(Postcode p : this.getPostcodeList()) {
            if(p.getPostcode().matches(szSplit[4])) {
                this.getUserList().add(new User(szSplit[1], szSplit[2], szSplit[3], p));
                return;
            }
        }
    }

    //add a postcode to the list
    public void processPostcode(String[] szSplit) {
        //if the postcode already exists, return
        for(Postcode p : this.getPostcodeList()) {
            if(p.getPostcode().matches(szSplit[4])) {
                return;
            }
        }

        this.getPostcodeList().add(new Postcode(szSplit[1], Integer.parseInt(szSplit[2])));
    }

    //process an order in the configuration file
    public void processOrder(String[] szSplit) {
        Order newOrder = null;
        for(User u : this.getUserList()) {
            if(u.getName().equals(szSplit[1])) {
                newOrder = new Order(u);
                break;
            }
        }

        String[] szOrder = szSplit[2].split(",");
        for(String szIngredient : szOrder) {
            if(szIngredient.matches("[0-9]* \\* [A-Za-z ]*")) {
                Dish dOrderComponent = null;

                //patterns to match quantities of dishes and actual dishes
                Matcher mMatcher;
                Pattern pQuantity = Pattern.compile("[0-9]* \\*");
                Pattern pIngredient = Pattern.compile("\\* [A-Za-z ]*");
                mMatcher = pIngredient.matcher(szIngredient);
                if(mMatcher.find()) {
                    String szName = mMatcher.group(0).substring(2);
                    boolean bActualIng = false;
                    for(Dish d : this.getSmStock().getDishesList()) {
                        if(d.getName().equals(szName)) {
                            dOrderComponent = d;
                            bActualIng = true;
                        }
                    }

                    if(!bActualIng) return;
                }

                //if a quantity and dish is found, add to the order
                mMatcher = pQuantity.matcher(szIngredient);
                if(mMatcher.find()) {
                    newOrder.addToOrder(dOrderComponent
                            , Integer.parseInt(mMatcher.group(0).substring(0, mMatcher.group(0).length() - 2)));
                }
            }
        }

        this.getOrderList().add(newOrder);
    }

    //add ingredients and dishes to the stock
    public void processStock(String[] szSplit) {
        for(Ingredient i : this.getSmStock().getIngredientsList()) {
            if(i.getName().equals(szSplit[1])) {
                this.ingredientStockMap.put(i, Integer.parseInt(szSplit[2]));
                return;
            }
        }

        for(Dish d : this.getSmStock().getDishesList()) {
            if(d.getName().equals(szSplit[1])) {
                this.dishStockMap.put(d, Integer.parseInt(szSplit[2]));
                return;
            }
        }
    }

    //getters
    public StockManagement getSmStock() {
        return smStock;
    }

    public List<Supplier> getSupplierList() {
        return supplierList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public List<Postcode> getPostcodeList() {
        return postcodeList;
    }

    public List<Staff> getStaffList() {
        return staffList;
    }

    public List<Drone> getDroneList() {
        return droneList;
    }

    public List<Order> getOrderList() {
        return orderList;
    }

    public Map<Dish, Integer> getDishStockMap() {
        return dishStockMap;
    }

    public Map<Ingredient, Integer> getIngredientStockMap() {
        return ingredientStockMap;
    }
}
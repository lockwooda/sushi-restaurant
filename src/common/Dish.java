package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Dish class that handles the dishes for both client and server
 * Stores the name, description, price and the recipe for the dish
 * Made by Alex Lockwood
 */
public class Dish extends Model implements Serializable {

    private String szName;
    private String szDescription;
    private double dPrice;
    private Map<Ingredient, Number> hRecipe;

    //constructor without a recipe given
    public Dish(String szName, String szDescription, double dPrice) {
        this.setName(szName);
        this.setDescription(szDescription);
        this.setPrice(dPrice);
        this.setRecipe(new HashMap<>());
    }

    //constructor with a recipe given
    public Dish(String szName, String szDescription, double iPrice, HashMap<Ingredient, Number> hRecipe) {
        this(szName, szDescription, iPrice);
        this.setRecipe(hRecipe);
    }

    //getters
    @Override
    public String getName() {
        return this.szName;
    }

    public String getDescription() {
        return this.szDescription;
    }

    public double getPrice() {
        return this.dPrice;
    }

    public Map<Ingredient, Number> getRecipe() {
        return this.hRecipe;
    }

    //setters
    public void setName(String szName) {
        this.notifyUpdate("name", this.getName(), szName);
        this.szName = szName;
    }

    public void setDescription(String szDescription) {
        this.notifyUpdate("desc", this.getDescription(), szDescription);
        this.szDescription = szDescription;
    }

    public void setPrice(double dPrice) {
        this.notifyUpdate("price", this.getPrice(), dPrice);
        this.dPrice = dPrice;
    }

    public void setRecipe(HashMap<Ingredient, Number> hRecipe) {
        this.notifyUpdate("recipe", this.getRecipe(), hRecipe);
        this.hRecipe = hRecipe;
    }

    //add ingredient to the recipe
    public void addIngredient(Ingredient iFood, int iAmount) {
        HashMap<Ingredient, Number> hTempRecipe = new HashMap<>(this.getRecipe());
        this.getRecipe().put(iFood, iAmount);
        this.notifyUpdate("recipe", hTempRecipe, this.getRecipe());
    }

    //remove ingredient from the recipe
    public void removeIngredient(Ingredient iFood) {
        this.getRecipe().remove(iFood);
    }
}

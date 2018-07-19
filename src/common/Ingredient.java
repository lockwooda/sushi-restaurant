package common;

import java.io.Serializable;

/**
 * Ingredient class that handles the ingredients used by the sushi restaurant
 * Stores the name of the ingredient, what units it is measured in and the supplier of said ingredient
 * Made by Alex Lockwood
 */
public class Ingredient extends Model implements Serializable {

    private String szName;
    private String szUnit;
    private Supplier sSupplier;

    //constructor
    public Ingredient(String szName, String szUnit, Supplier sSupplier) {
        this.setName(szName);
        this.setUnit(szUnit);
        this.setSupplier(sSupplier);
    }

    //getters
    @Override
    public String getName() {
        return this.szName;
    }

    public String getUnit() {
        return this.szUnit;
    }

    public Supplier getSupplier() {
        return this.sSupplier;
    }

    //setters
    @Override
    public void setName(String szName) {
        this.notifyUpdate("name", this.getName(), szName);
        this.szName = szName;
    }

    public void setUnit(String szUnit) {
        this.notifyUpdate("unit", this.getUnit(), szUnit);
        this.szUnit = szUnit;
    }

    public void setSupplier(Supplier sSupplier) {
        this.notifyUpdate("supplier", this.getSupplier(), sSupplier);
        this.sSupplier = sSupplier;
    }

}

package common;

import java.io.Serializable;

/**
 * Supplier class that handles the suppliers for the sushi restaurant
 * Stores the name of the supplier, and its distance from the business
 * Made by Alex Lockwood
 */
public class Supplier extends Model implements Serializable {

    private String szName;
    private Number iDistance;

    //constructor
    public Supplier(String szName, Number iDistance) {
        this.setName(szName);
        this.setDistance(iDistance);
    }

    @Override
    //getters
    public String getName() {
        return this.szName;
    }

    public Number getDistance() {
        return this.iDistance;
    }

    //setters
    public void setName(String szName) {
        this.notifyUpdate("name", this.getName(), szName);
        this.szName = szName;
    }

    public void setDistance(Number iDistance) {
        this.notifyUpdate("distance", this.getDistance(), iDistance);
        this.iDistance = iDistance;
    }

}

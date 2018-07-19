package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User class that models the user object for logging in, storing their location, and their basket
 * Made by Alex Lockwood
 */

public class User extends Model implements Serializable {

    private String szName;
    private String szPassword;
    private String szLocation;
    private Postcode pPostcode;
    private Map<Dish, Number> mBasket;

    //constructor
    public User(String szName, String szPassword, String szLocation, Postcode pPostcode) {
        this.setName(szName);
        this.setPassword(szPassword);
        this.setLocation(szLocation);
        this.setPostcode(pPostcode);
        mBasket = new HashMap<>();
    }

    //getters
    @Override
    public String getName() {
        return this.szName;
    }

    public String getPassword() {
        return this.szPassword;
    }

    public String getLocation() {
        return this.szLocation;
    }

    public Postcode getPostcode() {
        return this.pPostcode;
    }

    public Map<Dish, Number> getBasket() {
        return this.mBasket;
    }

    //setters
    public void setName(String szName) {
        this.notifyUpdate("name", this.getName(), szName);
        this.szName = szName;
    }

    public void setPassword(String szPassword) {
        this.szPassword = szPassword;
    }

    public void setLocation(String szLocation) {
        this.notifyUpdate("location", this.getLocation(), szLocation);
        this.szLocation = szLocation;
    }

    public void setPostcode(Postcode pPostcode) {
        this.notifyUpdate("postcode", this.getPostcode(), pPostcode);
        this.pPostcode = pPostcode;
    }

    public void addToBasket(Dish dDish, Integer iAmount) {
        this.notifyUpdate();
        if(this.mBasket.get(dDish) != null) {
            this.mBasket.put(dDish, iAmount + this.mBasket.get(dDish).intValue());
        }
        else this.mBasket.put(dDish, iAmount);
    }

    public void updateBasket(Dish dDish, Integer iAmount) {
        if(this.mBasket.get(dDish) != null) {
            this.notifyUpdate();
            this.mBasket.put(dDish, iAmount);

            if(this.mBasket.get(dDish).intValue() == 0) {
                this.mBasket.remove(dDish);
            }
        }
    }
}

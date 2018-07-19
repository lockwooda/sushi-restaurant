package common;

import java.io.Serializable;

/**
 * Postcode class that models a postcode and its distance from the business
 * Made by Alex Lockwood
 */
public class Postcode extends Model implements Serializable {

    private String szPostcode;
    private Number iDistance;

    //constructor
    public Postcode(String szPostcode, Number iDistance) {
        this.setPostcode(szPostcode);
        this.setDistance(iDistance);
    }

    //getters
    @Override
    public String getName() {
        return this.szPostcode;
    }

    public String getPostcode() {
        return this.szPostcode;
    }

    public Number getDistance() {
        return this.iDistance;
    }

    //setters
    public void setPostcode(String szPostcode) {
        this.notifyUpdate("postcode", this.szPostcode, szPostcode);
        this.szPostcode = szPostcode;
    }

    public void setDistance(Number iDistance) {
        this.notifyUpdate("postcode-dist", this.iDistance, iDistance);
        this.iDistance = iDistance;
    }
}

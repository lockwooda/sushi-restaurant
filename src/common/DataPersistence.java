package common;

import server.Drone;
import server.Server;
import server.Staff;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 * Data Persistence class that will output the data in the Server into a format readable by the configuration class
 * The Server class is modified in its constructor, such that it will automatically read the persistence.cfg file if present
 * Made by Alex Lockwood
 */

public class DataPersistence {

    public void processData(Server s) {
        try (PrintWriter pw = new PrintWriter("persistence.cfg")) {
            for(Supplier sSupplier : s.getSuppliers()) {
                pw.println("SUPPLIER:"
                        + sSupplier.getName() + ":"
                        + sSupplier.getDistance());
            }

            for(Postcode pPostcode : s.getPostcodes()) {
                pw.println("POSTCODE:"
                        + pPostcode.getPostcode() + ":"
                        + pPostcode.getDistance());
            }

            for(Ingredient inIngredient : s.getStock().getIngredientsList()) {
                pw.println("INGREDIENT:"
                        + inIngredient.getName() + ":"
                        + inIngredient.getUnit() + ":"
                        + inIngredient.getSupplier().getName() + ":"
                        + s.getStock().getRestockThreshold(inIngredient) + ":"
                        + s.getStock().getRestockAmount(inIngredient));
            }

            for(Dish dDish : s.getStock().getDishesList()) {
                pw.print("DISH:"
                        + dDish.getName() + ":"
                        + dDish.getDescription() + ":"
                        + dDish.getPrice() + ":"
                        + s.getStock().getRestockThreshold(dDish) + ":"
                        + s.getStock().getRestockAmount(dDish) + ":");

                Iterator<Ingredient> itIngIterator = dDish.getRecipe().keySet().iterator();
                Ingredient inIngredient;
                while(itIngIterator.hasNext()) {
                    inIngredient = itIngIterator.next();
                    pw.print(dDish.getRecipe().get(inIngredient) + " * " + inIngredient.getName());
                    if(itIngIterator.hasNext()) pw.print(",");
                }
                pw.println();
            }

            for(User uUser : s.getUsers()) {
                pw.println("USER:"
                        + uUser.getName() + ":"
                        + uUser.getPassword() + ":"
                        + uUser.getLocation() + ":"
                        + uUser.getPostcode().getPostcode());
            }

            for(Staff sStaff : s.getStaff()) {
                pw.println("STAFF:" + sStaff.getName());
            }

            for(Drone drDrone : s.getDrones()) {
                pw.println("DRONE:" + drDrone.getSpeed());
            }

            for(Ingredient inIngredient : s.getStock().getIngredientsList()) {
                pw.println("STOCK:" + inIngredient.getName() + ":"
                        + s.getStock().getStockLevel(inIngredient));
            }

            for(Dish dDish : s.getStock().getDishesList()) {
                pw.println("STOCK:" + dDish.getName() + ":"
                        + s.getStock().getStockLevel(dDish));
            }

            for(Order oOrder : s.getOrders()) {
                pw.print("ORDER:" + oOrder.getCustomer().getName() + ":");
                Iterator<Dish> itDishIterator = oOrder.getClientOrder().keySet().iterator();
                Dish dDish;
                while(itDishIterator.hasNext()) {
                    dDish = itDishIterator.next();
                    pw.print(oOrder.getClientOrder().get(dDish) + " * " + dDish.getName());
                    if(itDishIterator.hasNext()) pw.print(",");
                }
                pw.println();
            }
        }
        catch (IOException io) {
            io.printStackTrace();
        }

    }
}

package client;

import common.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client class
 * Handles all client side processing of data and communication to the business server
 */
public class Client implements ClientInterface {

    private CommsClient comms;
    private int iConnectionIndex;
    private List<UpdateListener> updateListeners = new ArrayList<>();

    //constructor
    public Client() {
        try {
            comms = new CommsClient();
            Object connectedMessage = this.getComms().getMessage();
            iConnectionIndex = Integer.parseInt(((String) connectedMessage ).split(":")[1]);
        }
        catch (IOException io) {
            io.printStackTrace();
        }
    }

    @Override
    public User register(String username, String password, String address, Postcode postcode) {
        Object[] oMessage = new Object[2];
        oMessage[0] = "REGISTER:"
                + iConnectionIndex + ":"
                + username + ":"
                + password + ":"
                + address;
        oMessage[1] = postcode;

        this.getComms().sendMessage(oMessage);
        User uNewUser = (User) this.getComms().getMessage();
        return uNewUser;
    }

    @Override
    public User login(String username, String password) {
        Object[] oMessage = new Object[]{"LOGIN:"
                + iConnectionIndex + ":"
                + username + ":"
                + password};
        this.getComms().sendMessage(oMessage);
        User uLogin = (User) this.getComms().getMessage();
        return uLogin;
    }

    @Override
    public List<Postcode> getPostcodes() {
        Object[] oMessage = new Object[]{"GETPOSTCODES:" + iConnectionIndex};
        this.getComms().sendMessage(oMessage);
        try {
            Thread.currentThread().sleep(100);
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return (List<Postcode>) this.getComms().getMessage();
    }

    @Override
    public List<Dish> getDishes() {
        Object[] oMessage = new Object[]{"GETDISHES:" + iConnectionIndex};
        this.getComms().sendMessage(oMessage);
        try {
            Thread.currentThread().sleep(100);
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return (List<Dish>) this.getComms().getMessage();
    }

    @Override
    public String getDishDescription(Dish dish) {
        return dish.getDescription();
    }

    @Override
    public Number getDishPrice(Dish dish) {
        return dish.getPrice();
    }

    @Override
    public Map<Dish, Number> getBasket(User user) {
        return user.getBasket();
    }

    @Override
    public Number getBasketCost(User user) {
        double dCost = 0;
        for(Dish d : user.getBasket().keySet()) {
            dCost += d.getPrice() * user.getBasket().get(d).intValue();
        }

        return dCost;
    }

    @Override
    public void addDishToBasket(User user, Dish dish, Number quantity) {
        user.addToBasket(dish, quantity.intValue());
        this.notifyUpdate();
    }

    @Override
    public void updateDishInBasket(User user, Dish dish, Number quantity) {
        user.updateBasket(dish, quantity.intValue());
        this.notifyUpdate();
    }

    @Override
    public Order checkoutBasket(User user) {
        Object[] oMessage = new Object[3];
        oMessage[0] = "CHECKOUT:"
                + iConnectionIndex;
        oMessage[1] = user;
        oMessage[2] = new HashMap<>(user.getBasket());

        this.getComms().sendMessage(oMessage);
        this.clearBasket(user);
        return (Order) this.getComms().getMessage();
    }

    @Override
    public void clearBasket(User user) {
        user.getBasket().clear();
    }

    @Override
    public List<Order> getOrders(User user) {
        Object[] oMessage = new Object[2];
        oMessage[0] = "GETORDERS:"
                + iConnectionIndex;
        oMessage[1] = user;
        this.getComms().sendMessage(oMessage);
        try {
            Thread.currentThread().sleep(100);
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return (List<Order>) this.getComms().getMessage();
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
    public void cancelOrder(Order order) {
        Object[] oMessage = new Object[2];
        oMessage[0] = "CANCELORDER:" + iConnectionIndex;
        oMessage[1] = order;

        this.getComms().sendMessage(oMessage);
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

    private CommsClient getComms() {
        return this.comms;
    }

}

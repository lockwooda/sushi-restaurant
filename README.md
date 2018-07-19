# Sushi Restaurant
For our second [Programming II](https://www.southampton.ac.uk/courses/modules/comp1206.page) coursework, our task was to adapt plans for a sushi restaurant using multithreading programming within Java.

## Core Ideas
There can be only one restaurant running the program at a time, but as many clients as you wish may run the program and connect to the restaurant. They either need to log in with pre-existing details or sign-up to access the menu to order. The client needs to be able to communicate with the restaurant and the restaurant needs to be able to communicate with specific clients.

A restaurant needs to keep in stock Dishes and Ingredients. Dishes are restocked by Staff, who wait until a Dish needs to be restocked. Staff members use Ingredients specified within a recipe that is associated with the Dish. Ingredients are restocked by Drones, which go to Suppliers linked to the Ingredient.

Each Ingredient and Dish have a restock threshold and restock amount. Once one goes below, the item is restocked to the restock threshold and restock amount.

Drones also have a double function of delivering Orders, which are placed by Clients.

### Additional Points
A restock amount and/or threshold that does not correctly restock the items needed by the restaurant is the fault of the restaurant and not of the code to recognise that an error has been made.

The amount of time taken to restock Dishes is arbitrarily decided between an upper and lower bound which can be changed by `iUpperBound` and `iLowerBound` variables within the Staff class. 

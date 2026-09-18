package model;

/**
 * Abstract base class representing a generic inventory item.
 * Demonstrates Object-Oriented Inheritance, Encapsulation, and Polymorphism.
 */
public abstract class Item {
    private String id;
    private String name;
    private double price;
    private int quantity;

    /**
     * Default constructor
     */
    public Item() {
    }

    /**
     * Parameterized constructor
     *
     * @param id       Unique identifier
     * @param name     Item name
     * @param price    Unit price
     * @param quantity Available stock quantity
     */
    public Item(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Encapsulation - Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    /**
     * Calculates total inventory value for this item.
     * Polymorphic method that can be used across subclasses.
     */
    public double getTotalValue() {
        return this.price * this.quantity;
    }

    /**
     * Checks whether the item is physically available in stock.
     */
    public boolean isAvailable() {
        return this.quantity > 0;
    }

    /**
     * Abstract method to provide specific display description.
     * Enforces polymorphic behavior in subclasses.
     */
    public abstract String getDisplayText();

    @Override
    public String toString() {
        return String.format("[%s] %s - $%.2f (Qty: %d)", id, name, price, quantity);
    }
}

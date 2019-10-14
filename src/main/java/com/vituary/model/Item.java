package com.vituary.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Item {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private UUID id;
    private String name;
    private String description;
    private int price;
    private int quantity;

    protected Item() {
    }

    public Item(String name, String description, int price, int quantity) {
        this(name, description, price, quantity, null);
    }

    public Item(String name, String description, int price, int quantity, UUID id) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    // Often I would consider using the @Getter annotation from lombok, but requires annotation
    // pre-processing to be enabled on the project, so sometimes causes issues for developers.
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void decrementQuantity() {
        quantity--;
    }
}

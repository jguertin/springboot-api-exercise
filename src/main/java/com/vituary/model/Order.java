package com.vituary.model;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn
    private Item item;
    private String username;
    private int price;

    protected Order() {
    }

    public Order(Item itemId, String username, int price) {
        this(itemId, username, price, null);
    }

    public Order(Item itemId, String username, int price, UUID id) {
        this.id = id;
        this.item = itemId;
        this.username = username;
        this.price = price;
    }

    public UUID getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public String getUsername() {
        return username;
    }

    public int getPrice() {
        return price;
    }
}

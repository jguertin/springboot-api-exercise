package com.vituary.dto;

import java.util.UUID;

public class OrderDTO {
    private UUID id;
    private UUID itemId;
    private String username;
    private Integer price;

    private OrderDTO() {
    }

    public OrderDTO(UUID id, UUID itemId, String username, Integer price) {
        this.id = id;
        this.itemId = itemId;
        this.username = username;
        this.price = price;
    }

    public UUID getId() {
        return id;
    }

    public UUID getItemId() {
        return itemId;
    }

    public String getUsername() {
        return username;
    }

    public Integer getPrice() {
        return price;
    }
}

package com.vituary.dto;

import java.util.UUID;

public class ItemDTO {
    private UUID id;
    private String name;
    private String description;
    private int price;

    private ItemDTO() {
    }

    public ItemDTO(UUID id, String name, String description, int price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
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
}

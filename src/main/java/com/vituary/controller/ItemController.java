package com.vituary.controller;

import com.vituary.dto.ItemDTO;
import com.vituary.model.Item;
import com.vituary.model.ItemPriceAdjuster;
import com.vituary.repository.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/items")
public class ItemController {
    private ItemRepository itemRepository;
    private ItemPriceAdjuster priceAdjuster;

    public ItemController(ItemRepository itemRepository, ItemPriceAdjuster priceAdjuster) {
        this.itemRepository = itemRepository;
        this.priceAdjuster = priceAdjuster;
    }

    @RequestMapping
    public List<ItemDTO> list() {
        Iterable<Item> items = itemRepository.findByQuantityIsGreaterThan(0);
        return StreamSupport.stream(items.spliterator(), false)
                .map(this::mapModelToDTO)
                .collect(Collectors.toList());
    }

    @RequestMapping("/{id}")
    public ItemDTO read(@PathVariable("id") UUID id) {
        return itemRepository.findById(id)
                .map(this::mapModelToDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "item not found: " + id));
    }

    private ItemDTO mapModelToDTO(Item item) {
        return new ItemDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                priceAdjuster.getAdjustedPriceForView(item)
        );
    }
}

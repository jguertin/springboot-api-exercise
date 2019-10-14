package com.vituary.repository;

import com.vituary.model.Item;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ItemRepository extends CrudRepository<Item, UUID> {
    Iterable<Item> findByQuantityIsGreaterThan(int greaterThan);
}

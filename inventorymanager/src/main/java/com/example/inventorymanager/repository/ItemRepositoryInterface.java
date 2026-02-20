package com.example.inventorymanager.repository;

import com.example.inventorymanager.model.Item;
import java.util.List;
import java.util.Optional;

public interface ItemRepositoryInterface {
    void save(Item item);
    List<Item> findAll();
    Optional<Item> findById(String id);
    void update(Item item);
    void delete(String id);
}

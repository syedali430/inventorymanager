package com.example.inventorymanager.repository;

import java.util.List;
import java.util.Optional;

import com.example.inventorymanager.model.Item;

public interface ItemRepositoryInterface {

    void save(Item item);

    List<Item> findAll();

    Optional<Item> findById(String id);

    void update(Item item);

    void delete(String id);
}

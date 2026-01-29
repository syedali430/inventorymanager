package com.example.inventorymanager.controller;

import com.example.inventorymanager.model.Item;

public interface ItemControllerInterface {
    void addItem(Item item);
    void getAllItems();
    void updateItem(Item item);
    void deleteItem(Item item);
}

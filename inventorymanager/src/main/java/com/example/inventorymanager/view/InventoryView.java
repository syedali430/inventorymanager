package com.example.inventorymanager.view;

import com.example.inventorymanager.model.Item;
import java.util.List;

public interface InventoryView {
    void displayItems(List<Item> items);
    void addItem(Item item);
    void updateItem(Item item);
    void deleteItem(Item item);
    void showErrorMessage(String message, Item item);
}

package com.example.inventorymanager.controller;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepository;
import com.example.inventorymanager.view.InventoryView;

public class ItemController {

    private ItemRepository itemRepository;
    private InventoryView inventoryView;

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ItemController(ItemRepository itemRepository, InventoryView inventoryView) {
        this.itemRepository = itemRepository;
        this.inventoryView = inventoryView;
    }

    public void setView(InventoryView inventoryView) {
        this.inventoryView = inventoryView;
    }

    public synchronized void addItem(Item item) {
        if (itemRepository.findById(item.getId()).isPresent()) {
            inventoryView.showErrorMessage("Already existing item with id " + item.getId(), item);
            return;
        }

        itemRepository.save(item);
        inventoryView.addItem(item);
    }

    public void getAllItems() {
        inventoryView.displayItems(itemRepository.findAll());
    }

    public synchronized void updateItem(Item item) {
        if (!itemRepository.findById(item.getId()).isPresent()) {
            inventoryView.showErrorMessage("No existing item with id " + item.getId(), item);
            return;
        }

        itemRepository.update(item);
        inventoryView.updateItem(item);
    }

    public synchronized void deleteItem(Item item) {
        if (!itemRepository.findById(item.getId()).isPresent()) {
            inventoryView.showErrorMessage("No existing item with id " + item.getId(), item);
            return;
        }

        itemRepository.delete(item.getId());
        inventoryView.deleteItem(item);
    }
}

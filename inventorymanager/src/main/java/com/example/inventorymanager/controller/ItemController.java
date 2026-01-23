package com.example.inventorymanager.controller;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepositoryInterface;
import com.example.inventorymanager.view.InventoryView;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class ItemController implements ItemControllerInterface {

    private final ItemRepositoryInterface itemRepository;
    private InventoryView inventoryView;

    @Inject
    public ItemController(ItemRepositoryInterface itemRepository, @Assisted InventoryView inventoryView) {
        this.itemRepository = itemRepository;
        this.inventoryView = inventoryView;
    }

    @Override
    public synchronized void addItem(Item item) {
        if (itemRepository.findById(item.getId()).isPresent()) {
            inventoryView.showErrorMessage("Already existing item with id " + item.getId(), item);
            return;
        }

        itemRepository.save(item);
        inventoryView.addItem(item);
    }

    @Override
    public void getAllItems() {
        inventoryView.displayItems(itemRepository.findAll());
    }

    @Override
    public synchronized void updateItem(Item item) {
        if (!itemRepository.findById(item.getId()).isPresent()) {
            inventoryView.showErrorMessage("No existing item with id " + item.getId(), item);
            return;
        }

        itemRepository.update(item);
        inventoryView.updateItem(item);
    }

    @Override
    public synchronized void deleteItem(Item item) {
        if (!itemRepository.findById(item.getId()).isPresent()) {
            inventoryView.showErrorMessage("No existing item with id " + item.getId(), item);
            return;
        }

        itemRepository.delete(item.getId());
        inventoryView.deleteItem(item);
    }
}

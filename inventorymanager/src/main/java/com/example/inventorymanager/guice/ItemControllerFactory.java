package com.example.inventorymanager.guice;

import com.example.inventorymanager.controller.ItemControllerInterface;
import com.example.inventorymanager.view.InventoryView;

public interface ItemControllerFactory {
    ItemControllerInterface create(InventoryView view);
}
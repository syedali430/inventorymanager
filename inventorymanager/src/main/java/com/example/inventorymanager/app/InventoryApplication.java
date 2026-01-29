package com.example.inventorymanager.app;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.view.swing.InventoryFrame;
import com.example.inventorymanager.repository.ItemRepository;
import com.example.inventorymanager.repository.ItemRepositoryInterface;
import com.example.inventorymanager.view.InventoryView;

import javax.swing.*;

public class InventoryApplication {

    public InventoryFrame createFrame() {
        ItemRepositoryInterface repository = ItemRepository.createDefault();
        InventoryFrame frame = new InventoryFrame();
        InventoryView view = frame;
        ItemController controller = new ItemController(repository, view);
        frame.setController(controller);
        controller.getAllItems();
        return frame;
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(
            () -> new InventoryApplication().createFrame().setVisible(true)
        );
    }
}

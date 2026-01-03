package com.example.inventorymanager.app;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.repository.ItemRepository;
import com.example.inventorymanager.view.swing.InventoryFrame;

import javax.swing.*;

public class InventoryApplication {

    public InventoryFrame createFrame() {
        ItemRepository repository = new ItemRepository();
        ItemController controller = new ItemController(repository);
        InventoryFrame frame = new InventoryFrame(controller);
        controller.setView(frame);
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

        SwingUtilities.invokeLater(() -> {
            InventoryApplication app = new InventoryApplication();
            InventoryFrame frame = app.createFrame();
            frame.setVisible(true);
        });
    }
}


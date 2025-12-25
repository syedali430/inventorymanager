package com.example.inventorymanager.app;

import com.example.inventorymanager.view.swing.InventoryFrame;
import com.example.inventorymanager.repository.ItemRepository;

import javax.swing.*;

public class InventoryApplication {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new InventoryFrame(new ItemRepository()).setVisible(true);
        });
    }
}


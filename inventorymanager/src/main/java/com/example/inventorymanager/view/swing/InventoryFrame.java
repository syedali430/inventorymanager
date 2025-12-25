package com.example.inventorymanager.view.swing;

import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.repository.ItemRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class InventoryFrame extends JFrame {

    private final ItemRepository repository;
    private DefaultListModel<Item> listModel;
    private JList<Item> itemList;

    private JTextField nameField;
    private JTextField quantityField;
    private JTextField priceField;
    private JTextField descField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;

    public InventoryFrame(ItemRepository repository) {
        this.repository = repository;

        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        itemList.setName("itemList");

        setTitle("Inventory Manager");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();

        List<Item> existingItems = repository.findAll();
        for (Item item : existingItems) {
            SwingUtilities.invokeLater(() -> listModel.addElement(item));
        }

        updateSelectionState();
    }

    private void initUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 6, 6));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Item Details"));
        nameField = new JTextField();
        nameField.setName("nameField");
        quantityField = new JTextField();
        quantityField.setName("quantityField");
        priceField = new JTextField();
        priceField.setName("priceField");
        descField = new JTextField();
        descField.setName("descField");

        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(quantityField);
        inputPanel.add(new JLabel("Price:"));
        inputPanel.add(priceField);
        inputPanel.add(new JLabel("Description:"));
        inputPanel.add(descField);

        panel.add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        addButton = new JButton("Add Item");
        addButton.setName("addButton");
        addButton.setEnabled(false); // initially disabled
        addButton.addActionListener(this::onAddItem);
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setPreferredSize(new Dimension(160, 38));

        updateButton = new JButton("Update Item");
        updateButton.setName("updateButton");
        updateButton.setEnabled(false);
        updateButton.addActionListener(this::onUpdateItem);
        updateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateButton.setPreferredSize(new Dimension(160, 38));

        deleteButton = new JButton("Delete Item");
        deleteButton.setName("deleteButton");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(this::onDeleteItem);
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.setPreferredSize(new Dimension(160, 38));

        buttonPanel.add(addButton);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(updateButton);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel, BorderLayout.EAST);

        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(itemList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Inventory Items"));
        panel.add(scrollPane, BorderLayout.CENTER);

        setContentPane(panel);

        // Enable addButton only if name, quantity, price fields are filled
        nameField.getDocument().addDocumentListener(new SimpleDocListener(this::updateAddButtonState));
        quantityField.getDocument().addDocumentListener(new SimpleDocListener(this::updateAddButtonState));
        priceField.getDocument().addDocumentListener(new SimpleDocListener(this::updateAddButtonState));

        // Sync detail fields + buttons on selection change
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionState();
            }
        });
    }

    private void updateAddButtonState() {
        boolean enabled = !nameField.getText().trim().isEmpty() &&
                        !quantityField.getText().trim().isEmpty() &&
                        !priceField.getText().trim().isEmpty() &&
                        itemList.getSelectedIndex() == -1;
        addButton.setEnabled(enabled);
    }

    private void updateSelectionState() {
        Item selected = itemList.getSelectedValue();
        boolean hasSelection = selected != null;
        deleteButton.setEnabled(hasSelection);
        updateButton.setEnabled(hasSelection);
        if (hasSelection) {
            nameField.setText(selected.getName());
            quantityField.setText(String.valueOf(selected.getQuantity()));
            priceField.setText(String.valueOf(selected.getPrice()));
            descField.setText(selected.getDescription());
            addButton.setEnabled(false);
        } else {
            nameField.setText("");
            quantityField.setText("");
            priceField.setText("");
            descField.setText("");
            updateAddButtonState();
        }
    }


    private void onAddItem(ActionEvent e) {
        try {
            String name = nameField.getText().trim();
            int quantity = Integer.parseInt(quantityField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            String desc = descField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Item item = new Item(String.valueOf(System.currentTimeMillis()), name, quantity, price, desc);
            repository.save(item);
            listModel.addElement(item);

            // Clear fields
            itemList.clearSelection();
            updateSelectionState();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be numeric!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateItem(ActionEvent e) {
        Item selected = itemList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an item first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField nameUpdate = new JTextField(selected.getName());
        JTextField quantityUpdate = new JTextField(String.valueOf(selected.getQuantity()));
        JTextField priceUpdate = new JTextField(String.valueOf(selected.getPrice()));
        JTextField descUpdate = new JTextField(selected.getDescription());

        Object[] message = {
            "Name:", nameUpdate,
            "Quantity:", quantityUpdate,
            "Price:", priceUpdate,
            "Description:", descUpdate
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Update Item", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                selected.setName(nameUpdate.getText().trim());
                selected.setQuantity(Integer.parseInt(quantityUpdate.getText().trim()));
                selected.setPrice(Double.parseDouble(priceUpdate.getText().trim()));
                selected.setDescription(descUpdate.getText().trim());

                repository.update(selected);
                SwingUtilities.invokeLater(() -> itemList.repaint());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantity and Price must be numeric!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteItem(ActionEvent e) {
        Item selected = itemList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an item first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete Item", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            repository.delete(selected.getId());
            listModel.removeElement(selected);
            updateSelectionState();
        }
    }

    // Simple helper for document listener
    private static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable callback;

        SimpleDocListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) { callback.run(); }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) { callback.run(); }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) { callback.run(); }
    }
}

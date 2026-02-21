package com.example.inventorymanager.view.swing;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.view.InventoryView;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InventoryFrame extends JFrame implements InventoryView {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_TITLE = "Error";

    private transient ItemController controller;
    private DefaultListModel<Item> listModel;
    private JList<Item> itemList;

    private JTextField nameField;
    private JTextField quantityField;
    private JTextField priceField;
    private JTextField descField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;

    public InventoryFrame() {
        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        itemList.setName("itemList");

        setTitle("Inventory Manager");
        setSize(800, 550);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        updateSelectionState();
    }

    public void setController(ItemController controller) {
        this.controller = controller;
    }

    public ItemController getController() {
        return controller;
    }

    public void start() {
        setVisible(true);
        if (controller != null && !Boolean.getBoolean("inventory.test.skipInitialLoad")) {
            Thread loader = new Thread(() -> {
                try {
                    controller.getAllItems();
                } catch (Exception e) {
                    if (!Boolean.getBoolean("inventory.test.silentLoadErrors")) {
                        Logger.getLogger(InventoryFrame.class.getName()).log(Level.SEVERE, "Exception", e);
                    }
                }
            }, "inventory-load-items");
            loader.setDaemon(true);
            loader.start();
        }
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
        addButton.setEnabled(false);
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

        DocumentListener docListener = new SimpleDocListener(this::updateAddButtonState);
        nameField.getDocument().addDocumentListener(docListener);
        quantityField.getDocument().addDocumentListener(docListener);
        priceField.getDocument().addDocumentListener(docListener);

        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionState();
            }
        });
    }

    private void updateAddButtonState() {
        boolean filled = !nameField.getText().trim().isEmpty()
                && !quantityField.getText().trim().isEmpty()
                && !priceField.getText().trim().isEmpty();
        boolean selectable = itemList.getSelectedIndex() == -1;
        addButton.setEnabled(filled && selectable);
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
                JOptionPane.showMessageDialog(this, "Name is required!", ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (controller != null) {
                Item item = new Item(String.valueOf(System.currentTimeMillis()), name, quantity, price, desc);
                controller.addItem(item);
                itemList.clearSelection();
                updateSelectionState();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be numeric!", ERROR_TITLE,
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateItem(ActionEvent e) {
        Item selected = itemList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an item first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (Boolean.getBoolean("inventory.test.skipUpdateDialog")) {
            Item updated = createUpdatedItem(
                    selected.getId(),
                    nameField.getText().trim(),
                    quantityField.getText().trim(),
                    priceField.getText().trim(),
                    descField.getText().trim()
            );
            updateController(updated);
            return;
        }

        JTextField nameUpdate = new JTextField(selected.getName());
        JTextField quantityUpdate = new JTextField(String.valueOf(selected.getQuantity()));
        JTextField priceUpdate = new JTextField(String.valueOf(selected.getPrice()));
        JTextField descUpdate = new JTextField(selected.getDescription());
        nameUpdate.setName("updateNameField");
        quantityUpdate.setName("updateQuantityField");
        priceUpdate.setName("updatePriceField");
        descUpdate.setName("updateDescField");

        Object[] message = {
                "Name:", nameUpdate,
                "Quantity:", quantityUpdate,
                "Price:", priceUpdate,
                "Description:", descUpdate
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Update Item", JOptionPane.OK_CANCEL_OPTION);
        if (shouldApplyUpdate(option)) {
            Item updated = createUpdatedItem(
                    selected.getId(),
                    nameUpdate.getText().trim(),
                    quantityUpdate.getText().trim(),
                    priceUpdate.getText().trim(),
                    descUpdate.getText().trim()
            );
            updateController(updated);
        }
    }

    private boolean shouldApplyUpdate(int option) {
        return option == JOptionPane.OK_OPTION || Boolean.getBoolean("inventory.test.forceUpdateDialogOk");
    }

    private Item createUpdatedItem(String id, String name, String quantityText, String priceText, String description) {
        try {
            return new Item(
                    id,
                    name,
                    Integer.parseInt(quantityText),
                    Double.parseDouble(priceText),
                    description
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be numeric!", ERROR_TITLE,
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void updateController(Item updated) {
        if (updated != null && controller != null) {
            controller.updateItem(updated);
        }
    }

    private void onDeleteItem(ActionEvent e) {
        Item selected = itemList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select an item first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (controller != null) {
            controller.deleteItem(selected);
        }
    }

    @Override
    public void displayItems(List<Item> items) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (Item item : items) {
                listModel.addElement(item);
            }
            itemList.clearSelection();
            updateSelectionState();
        });
    }

    @Override
    public void addItem(Item item) {
        SwingUtilities.invokeLater(() -> {
            listModel.addElement(item);
            itemList.clearSelection();
            updateSelectionState();
        });
    }

    @Override
    public void updateItem(Item item) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < listModel.size(); i++) {
                Item current = listModel.get(i);
                if (current.getId().equals(item.getId())) {
                    listModel.set(i, item);
                    break;
                }
            }
            itemList.repaint();
        });
    }

    @Override
    public void deleteItem(Item item) {
        SwingUtilities.invokeLater(() -> {
            listModel.removeElement(item);
            updateSelectionState();
        });
    }

    @Override
    public void showErrorMessage(String message, Item item) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, ERROR_TITLE, JOptionPane.ERROR_MESSAGE)
        );
    }

    public DefaultListModel<Item> getListModel() {
        return listModel;
    }

    private static class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable callback;

        SimpleDocListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }
    }
}

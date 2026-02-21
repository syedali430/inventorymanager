package com.example.inventorymanager.view.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.example.inventorymanager.controller.ItemController;
import com.example.inventorymanager.model.Item;
import com.example.inventorymanager.view.InventoryView;

public class InventorySwingView extends JFrame implements InventoryView {

    private static final long serialVersionUID = 1L;

    private JTextField txtName;
    private JTextField txtID;
    private JTextField txtQuantity;
    private JTextField txtPrice;
    private JTextField txtDescription;
    private JButton btnDeleteSelected;
    private JButton btnUpdateSelected;
    private JButton btnAdd;
    private JButton btnCancel;

    private JLabel lblErrorMessage;

    private JList<Item> listItems;
    private DefaultListModel<Item> listItemModel;

    private transient ItemController itemController;

    public DefaultListModel<Item> getListItemModel() {
        return listItemModel;
    }

    public void setItemController(ItemController itemController) {
        this.itemController = itemController;
    }

    public InventorySwingView() {
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Inventory Manager");
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[] { 45, 0, 770, 0, 0 };
        gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 173, 0, 23, 0 };
        gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
        getContentPane().setLayout(gridBagLayout);

        JLabel lblId = new JLabel("id");
        GridBagConstraints gbc_lblId = new GridBagConstraints();
        gbc_lblId.anchor = GridBagConstraints.EAST;
        gbc_lblId.insets = new Insets(0, 0, 5, 5);
        gbc_lblId.gridx = 1;
        gbc_lblId.gridy = 0;
        getContentPane().add(lblId, gbc_lblId);

        txtID = new JTextField();
        txtID.setName("idTextBox");
        GridBagConstraints gbc_txtId = new GridBagConstraints();
        gbc_txtId.insets = new Insets(0, 0, 5, 5);
        gbc_txtId.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtId.gridx = 2;
        gbc_txtId.gridy = 0;
        getContentPane().add(txtID, gbc_txtId);
        txtID.setColumns(10);

        JLabel lblName = new JLabel("Name");
        GridBagConstraints gbc_lblName = new GridBagConstraints();
        gbc_lblName.insets = new Insets(0, 0, 5, 5);
        gbc_lblName.anchor = GridBagConstraints.EAST;
        gbc_lblName.gridx = 1;
        gbc_lblName.gridy = 1;
        getContentPane().add(lblName, gbc_lblName);

        txtName = new JTextField();
        txtName.setName("nameTextBox");
        GridBagConstraints gbc_txtName = new GridBagConstraints();
        gbc_txtName.insets = new Insets(0, 0, 5, 5);
        gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtName.gridx = 2;
        gbc_txtName.gridy = 1;
        getContentPane().add(txtName, gbc_txtName);
        txtName.setColumns(10);

        JLabel lblQuantity = new JLabel("Quantity");
        GridBagConstraints gbc_lblQuantity = new GridBagConstraints();
        gbc_lblQuantity.anchor = GridBagConstraints.EAST;
        gbc_lblQuantity.insets = new Insets(0, 0, 5, 5);
        gbc_lblQuantity.gridx = 1;
        gbc_lblQuantity.gridy = 2;
        getContentPane().add(lblQuantity, gbc_lblQuantity);

        txtQuantity = new JTextField();
        txtQuantity.setName("quantityTextBox");
        GridBagConstraints gbc_txtQuantity = new GridBagConstraints();
        gbc_txtQuantity.insets = new Insets(0, 0, 5, 5);
        gbc_txtQuantity.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtQuantity.gridx = 2;
        gbc_txtQuantity.gridy = 2;
        getContentPane().add(txtQuantity, gbc_txtQuantity);
        txtQuantity.setColumns(10);

        JLabel lblPrice = new JLabel("Price");
        GridBagConstraints gbc_lblPrice = new GridBagConstraints();
        gbc_lblPrice.anchor = GridBagConstraints.EAST;
        gbc_lblPrice.insets = new Insets(0, 0, 5, 5);
        gbc_lblPrice.gridx = 1;
        gbc_lblPrice.gridy = 3;
        getContentPane().add(lblPrice, gbc_lblPrice);

        txtPrice = new JTextField();
        txtPrice.setName("priceTextBox");
        GridBagConstraints gbc_txtPrice = new GridBagConstraints();
        gbc_txtPrice.insets = new Insets(0, 0, 5, 5);
        gbc_txtPrice.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtPrice.gridx = 2;
        gbc_txtPrice.gridy = 3;
        getContentPane().add(txtPrice, gbc_txtPrice);
        txtPrice.setColumns(10);

        JLabel lblDescription = new JLabel("Description");
        GridBagConstraints gbc_lblDescription = new GridBagConstraints();
        gbc_lblDescription.anchor = GridBagConstraints.EAST;
        gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
        gbc_lblDescription.gridx = 1;
        gbc_lblDescription.gridy = 4;
        getContentPane().add(lblDescription, gbc_lblDescription);

        txtDescription = new JTextField();
        txtDescription.setName("descriptionTextBox");
        GridBagConstraints gbc_txtDescription = new GridBagConstraints();
        gbc_txtDescription.insets = new Insets(0, 0, 5, 5);
        gbc_txtDescription.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtDescription.gridx = 2;
        gbc_txtDescription.gridy = 4;
        getContentPane().add(txtDescription, gbc_txtDescription);
        txtDescription.setColumns(10);

        btnAdd = new JButton("Add Item");
        btnAdd.setEnabled(false);
        btnAdd.setName("btnAdd");
        btnAdd.addActionListener(e -> new Thread(() -> itemController
                .addItem(new Item(txtID.getText(), txtName.getText(), Integer.parseInt(txtQuantity.getText()),
                        Double.parseDouble(txtPrice.getText()), txtDescription.getText())))
                .start());

        GridBagConstraints gbc_btnAdd = new GridBagConstraints();
        gbc_btnAdd.insets = new Insets(0, 0, 5, 5);
        gbc_btnAdd.gridx = 2;
        gbc_btnAdd.gridy = 5;
        getContentPane().add(btnAdd, gbc_btnAdd);

        btnCancel = new JButton("Cancel");
        btnCancel.setEnabled(false);
        btnCancel.setName("btnCancel");
        btnCancel.addActionListener(e -> {
            txtID.setText("");
            txtName.setText("");
            txtQuantity.setText("");
            txtPrice.setText("");
            txtDescription.setText("");

            btnUpdateSelected.setEnabled(false);
            btnDeleteSelected.setEnabled(false);

            listItems.clearSelection();

            txtID.setEnabled(true);
        });
        GridBagConstraints gbc_btnCancel = new GridBagConstraints();
        gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
        gbc_btnCancel.gridx = 1;
        gbc_btnCancel.gridy = 5;
        getContentPane().add(btnCancel, gbc_btnCancel);

        btnDeleteSelected = new JButton("Delete Selected");
        btnDeleteSelected.setEnabled(false);
        btnDeleteSelected.setName("btnDeleteSelected");
        btnDeleteSelected.addActionListener(
                e -> new Thread(() -> itemController.deleteItem(listItems.getSelectedValue())).start());
        GridBagConstraints gbc_btnDeleteSelected = new GridBagConstraints();
        gbc_btnDeleteSelected.insets = new Insets(0, 0, 5, 5);
        gbc_btnDeleteSelected.gridx = 2;
        gbc_btnDeleteSelected.gridy = 7;
        getContentPane().add(btnDeleteSelected, gbc_btnDeleteSelected);

        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridwidth = 3;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 6;
        getContentPane().add(scrollPane, gbc_scrollPane);

        listItemModel = new DefaultListModel<>();
        listItems = new JList<>(listItemModel);
        listItems.addListSelectionListener(e -> {
            boolean isItemSelected = listItems.getSelectedIndex() != -1;

            btnDeleteSelected.setEnabled(isItemSelected);
            btnCancel.setEnabled(isItemSelected);
            btnUpdateSelected.setEnabled(isItemSelected);

            if (isItemSelected) {
                Item selectedItem = listItems.getSelectedValue();

                txtID.setText(selectedItem.getId());
                txtName.setText(selectedItem.getName());
                txtQuantity.setText(String.valueOf(selectedItem.getQuantity()));
                txtPrice.setText(String.valueOf(selectedItem.getPrice()));
                txtDescription.setText(selectedItem.getDescription());

                txtID.setEnabled(false);

                btnAdd.setEnabled(false);
            } else {
                txtID.setText("");
                txtName.setText("");
                txtQuantity.setText("");
                txtPrice.setText("");
                txtDescription.setText("");

                txtID.setEnabled(true);
            }
        });
        listItems.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Item item = (Item) value;
                return super.getListCellRendererComponent(list, getDisplayString(item), index, isSelected,
                        cellHasFocus);
            }
        });
        scrollPane.setViewportView(listItems);
        listItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listItems.setName("itemList");

        btnUpdateSelected = new JButton("Update Selected");
        btnUpdateSelected.setName("btnUpdateSelected");
        btnUpdateSelected.setEnabled(false);
        btnUpdateSelected.addActionListener(e -> new Thread(() -> {
            String id = txtID.getText();
            String name = txtName.getText();
            int quantity = Integer.parseInt(txtQuantity.getText());
            double price = Double.parseDouble(txtPrice.getText());
            String description = txtDescription.getText();

            Item updatedItem = new Item(id, name, quantity, price, description);

            itemController.updateItem(updatedItem);

        }).start());
        GridBagConstraints gbc_btnUpdateSelected = new GridBagConstraints();
        gbc_btnUpdateSelected.insets = new Insets(0, 0, 5, 5);
        gbc_btnUpdateSelected.gridx = 1;
        gbc_btnUpdateSelected.gridy = 7;
        getContentPane().add(btnUpdateSelected, gbc_btnUpdateSelected);

        lblErrorMessage = new JLabel("");
        lblErrorMessage.setName("errorMessageLabel");
        GridBagConstraints gbc_lblErrorMessage = new GridBagConstraints();
        gbc_lblErrorMessage.gridwidth = 3;
        gbc_lblErrorMessage.insets = new Insets(0, 0, 0, 5);
        gbc_lblErrorMessage.gridx = 0;
        gbc_lblErrorMessage.gridy = 8;
        getContentPane().add(lblErrorMessage, gbc_lblErrorMessage);

        KeyAdapter btnAddEnabler = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                btnAdd.setEnabled(listItems.getSelectedIndex() == -1 && !txtID.getText().trim().isEmpty()
                        && !txtName.getText().trim().isEmpty() && !txtDescription.getText().trim().isEmpty()
                        && !txtPrice.getText().trim().isEmpty() && !txtQuantity.getText().trim().isEmpty());
            }
        };

        txtID.addKeyListener(btnAddEnabler);
        txtName.addKeyListener(btnAddEnabler);
        txtDescription.addKeyListener(btnAddEnabler);
        txtPrice.addKeyListener(btnAddEnabler);
        txtQuantity.addKeyListener(btnAddEnabler);
    }

    @Override
    public void displayItems(List<Item> items) {
        SwingUtilities.invokeLater(() -> {
            listItemModel.clear();
            items.forEach(listItemModel::addElement);
        });
    }

    @Override
    public void addItem(Item item) {
        SwingUtilities.invokeLater(() -> {
            listItemModel.addElement(item);
            resetErrorLabel();
        });
    }

    @Override
    public void deleteItem(Item item) {
        SwingUtilities.invokeLater(() -> {
            listItemModel.removeElement(item);
            resetErrorLabel();
        });
    }

    @Override
    public void updateItem(Item item) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < listItemModel.size(); i++) {
                if (listItemModel.get(i).getId().equals(item.getId())) {
                    listItemModel.set(i, item);
                    break;
                }
            }
            resetErrorLabel();
        });
    }

    @Override
    public void showErrorMessage(String message, Item item) {
        SwingUtilities.invokeLater(() -> lblErrorMessage.setText(message + ": " + getDisplayString(item)));
    }

    private void resetErrorLabel() {
        lblErrorMessage.setText(" ");
    }

    private String getDisplayString(Item item) {
        return item.getId() + " - " + item.getName() + " - " + item.getQuantity() + " - " + item.getPrice() + " - "
                + item.getDescription();
    }
}

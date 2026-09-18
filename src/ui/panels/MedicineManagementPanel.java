package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import exception.ValidationException;
import model.Medicine;
import service.InventoryService;
import ui.components.RoundedPanel;
import ui.components.SearchTextField;
import ui.components.StyledButton;
import util.DateUtil;
import util.UITheme;
import util.ValidationUtil;

/**
 * Module 1: Medicine Management
 * Provides complete CRUD operations, multi-attribute search, category filtering,
 * input validation, and an interactive modern data grid.
 */
public class MedicineManagementPanel extends JPanel {
    private final InventoryService inventoryService;

    private JTable medicineTable;
    private DefaultTableModel tableModel;
    private SearchTextField searchField;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> statusComboBox;
    private JLabel countLabel;

    public MedicineManagementPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
        refreshTable();

        // Listen for inventory updates from elsewhere
        inventoryService.addChangeListener(this::refreshTable);
    }

    private void initUI() {
        // TOP: Header & Toolbar
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // Title Row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Medicine Catalog & Management");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel subLbl = new JLabel("Add, edit, delete, and search pharmaceutical inventory records");
        subLbl.setFont(UITheme.FONT_BODY);
        subLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        titleBox.add(titleLbl);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subLbl);
        titleRow.add(titleBox, BorderLayout.WEST);

        // Action Buttons
        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsBox.setOpaque(false);

        StyledButton btnAdd = new StyledButton("+ Add Medicine", StyledButton.ButtonType.PRIMARY);
        btnAdd.addActionListener(e -> openMedicineDialog(null));

        StyledButton btnEdit = new StyledButton("✎ Edit Selected", StyledButton.ButtonType.SECONDARY);
        btnEdit.addActionListener(e -> editSelectedMedicine());

        StyledButton btnDelete = new StyledButton("🗑 Delete", StyledButton.ButtonType.DANGER);
        btnDelete.addActionListener(e -> deleteSelectedMedicine());

        StyledButton btnRefresh = new StyledButton("↻ Refresh", StyledButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshTable());

        actionsBox.add(btnAdd);
        actionsBox.add(btnEdit);
        actionsBox.add(btnDelete);
        actionsBox.add(btnRefresh);

        titleRow.add(actionsBox, BorderLayout.EAST);
        topContainer.add(titleRow);
        topContainer.add(Box.createVerticalStrut(16));

        // Filter / Search Bar Card
        RoundedPanel filterBar = new RoundedPanel(12, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        filterBar.setLayout(new BorderLayout(12, 0));
        filterBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // Search Field on the Left
        searchField = new SearchTextField("Search medicine by name, ID, category, or manufacturer...", 25);
        searchField.setPreferredSize(new Dimension(380, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });
        filterBar.add(searchField, BorderLayout.WEST);

        // Filters on the Right
        JPanel filtersRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filtersRight.setOpaque(false);

        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(UITheme.FONT_BODY_BOLD);
        catLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(UITheme.FONT_BODY);
        categoryComboBox.setPreferredSize(new Dimension(160, 34));
        categoryComboBox.addActionListener(e -> filterData());

        JLabel statLbl = new JLabel("Status:");
        statLbl.setFont(UITheme.FONT_BODY_BOLD);
        statLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        statusComboBox = new JComboBox<>(new String[]{"All Status", "In Stock", "Low Stock", "Expiring Soon", "Expired"});
        statusComboBox.setFont(UITheme.FONT_BODY);
        statusComboBox.setPreferredSize(new Dimension(130, 34));
        statusComboBox.addActionListener(e -> filterData());

        countLabel = new JLabel("Showing 0 medicines");
        countLabel.setFont(UITheme.FONT_BODY);
        countLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        filtersRight.add(catLbl);
        filtersRight.add(categoryComboBox);
        filtersRight.add(statLbl);
        filtersRight.add(statusComboBox);
        filtersRight.add(Box.createHorizontalStrut(10));
        filtersRight.add(countLabel);

        filterBar.add(filtersRight, BorderLayout.EAST);
        topContainer.add(filterBar);

        add(topContainer, BorderLayout.NORTH);

        // CENTER: Modern Table
        RoundedPanel tableCard = new RoundedPanel(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        String[] columnNames = {
                "Medicine ID", "Medicine Name", "Category", "Manufacturer",
                "Batch", "Price ($)", "Stock Qty", "Expiry Date", "Min Stock", "Status"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        medicineTable = new JTable(tableModel);
        UITheme.styleTable(medicineTable);
        medicineTable.getColumnModel().getColumn(9).setCellRenderer(new UITheme.StatusBadgeRenderer());

        // Widths
        medicineTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        medicineTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        medicineTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        medicineTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        medicineTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        medicineTable.getColumnModel().getColumn(5).setPreferredWidth(75);
        medicineTable.getColumnModel().getColumn(6).setPreferredWidth(75);
        medicineTable.getColumnModel().getColumn(7).setPreferredWidth(95);
        medicineTable.getColumnModel().getColumn(8).setPreferredWidth(75);
        medicineTable.getColumnModel().getColumn(9).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(medicineTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshCategories() {
        String current = (String) categoryComboBox.getSelectedItem();
        categoryComboBox.removeAllItems();
        List<String> categories = inventoryService.getAllCategories();
        for (String c : categories) {
            categoryComboBox.addItem(c);
        }
        if (current != null) {
            categoryComboBox.setSelectedItem(current);
        }
    }

    public void refreshTable() {
        refreshCategories();
        filterData();
    }

    private void filterData() {
        String query = searchField != null ? searchField.getText() : "";
        String cat = categoryComboBox != null ? (String) categoryComboBox.getSelectedItem() : "All Categories";
        String stat = statusComboBox != null ? (String) statusComboBox.getSelectedItem() : "All Status";

        List<Medicine> filtered = inventoryService.search(query, cat, stat);

        tableModel.setRowCount(0);
        for (Medicine m : filtered) {
            tableModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getCategory(),
                    m.getManufacturer(),
                    m.getBatchNumber(),
                    String.format("%.2f", m.getPrice()),
                    m.getQuantity(),
                    DateUtil.formatDate(m.getExpiryDate()),
                    m.getMinStockLevel(),
                    m.getStatus()
            });
        }

        if (countLabel != null) {
            countLabel.setText("Showing " + filtered.size() + " medicines");
        }
    }

    private void editSelectedMedicine() {
        int selectedRow = medicineTable.getSelectedRow();
        if (selectedRow < 0) {
            UITheme.showWarning(this, "Please select a medicine from the table to edit.");
            return;
        }

        String id = (String) tableModel.getValueAt(selectedRow, 0);
        Medicine med = inventoryService.findById(id);
        if (med != null) {
            openMedicineDialog(med);
        }
    }

    private void deleteSelectedMedicine() {
        int selectedRow = medicineTable.getSelectedRow();
        if (selectedRow < 0) {
            UITheme.showWarning(this, "Please select a medicine to delete.");
            return;
        }

        String id = (String) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        boolean confirmed = UITheme.showConfirm(
                this,
                "Are you sure you want to delete '" + name + "' (ID: " + id + ")?\nThis action cannot be undone.",
                "Confirm Delete Medicine"
        );

        if (confirmed) {
            boolean success = inventoryService.deleteMedicine(id);
            if (success) {
                UITheme.showSuccess(this, "Medicine deleted successfully.");
                refreshTable();
            } else {
                UITheme.showError(this, "Failed to delete medicine.");
            }
        }
    }

    /**
     * Modal Dialog for Adding or Editing Medicine with complete input validation.
     */
    private void openMedicineDialog(Medicine existing) {
        boolean isEdit = existing != null;
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, isEdit ? "Edit Medicine Record" : "Add New Medicine", true);
        dialog.setSize(480, 560);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Title
        JLabel titleLbl = new JLabel(isEdit ? "Update Medicine Details" : "Register New Medicine");
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MAIN);
        mainPanel.add(titleLbl, BorderLayout.NORTH);

        // Form Fields (GridBagLayout)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        JTextField txtId = new JTextField(isEdit ? existing.getId() : "");
        if (isEdit) {
            txtId.setEditable(false);
            txtId.setBackground(new Color(241, 245, 249));
        }
        JTextField txtName = new JTextField(isEdit ? existing.getName() : "");
        JTextField txtCategory = new JTextField(isEdit ? existing.getCategory() : "");
        JTextField txtManufacturer = new JTextField(isEdit ? existing.getManufacturer() : "");
        JTextField txtBatch = new JTextField(isEdit ? existing.getBatchNumber() : "");
        JTextField txtPrice = new JTextField(isEdit ? String.valueOf(existing.getPrice()) : "");
        JTextField txtQuantity = new JTextField(isEdit ? String.valueOf(existing.getQuantity()) : "");
        JTextField txtExpiry = new JTextField(isEdit ? DateUtil.formatDateIso(existing.getExpiryDate()) : LocalDate.now().plusMonths(12).toString());
        JTextField txtMinStock = new JTextField(isEdit ? String.valueOf(existing.getMinStockLevel()) : "15");

        addFormField(formPanel, gbc, 0, "Medicine ID *:", txtId);
        addFormField(formPanel, gbc, 1, "Medicine Name *:", txtName);
        addFormField(formPanel, gbc, 2, "Category *:", txtCategory);
        addFormField(formPanel, gbc, 3, "Manufacturer *:", txtManufacturer);
        addFormField(formPanel, gbc, 4, "Batch Number *:", txtBatch);
        addFormField(formPanel, gbc, 5, "Unit Price ($) *:", txtPrice);
        addFormField(formPanel, gbc, 6, "Initial Stock Qty *:", txtQuantity);
        addFormField(formPanel, gbc, 7, "Expiry Date (YYYY-MM-DD) *:", txtExpiry);
        addFormField(formPanel, gbc, 8, "Minimum Stock Threshold *:", txtMinStock);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        StyledButton btnCancel = new StyledButton("Cancel", StyledButton.ButtonType.SECONDARY);
        btnCancel.addActionListener(e -> dialog.dispose());

        StyledButton btnSave = new StyledButton(isEdit ? "Update Medicine" : "Save Medicine", StyledButton.ButtonType.PRIMARY);
        btnSave.addActionListener(e -> {
            try {
                String id = txtId.getText().trim();
                String name = txtName.getText().trim();
                String cat = txtCategory.getText().trim();
                String mfg = txtManufacturer.getText().trim();
                String batch = txtBatch.getText().trim();
                double price = ValidationUtil.parseDouble(txtPrice.getText(), "Unit Price");
                int qty = ValidationUtil.parseInt(txtQuantity.getText(), "Stock Quantity");
                LocalDate expiry = ValidationUtil.parseDate(txtExpiry.getText(), "Expiry Date");
                int minStock = ValidationUtil.parseInt(txtMinStock.getText(), "Minimum Stock Threshold");

                Medicine medicineObj = new Medicine(id, name, cat, mfg, batch, price, qty, expiry, minStock);

                if (isEdit) {
                    inventoryService.updateMedicine(medicineObj);
                    UITheme.showSuccess(dialog, "Medicine record updated successfully!");
                } else {
                    inventoryService.addMedicine(medicineObj);
                    UITheme.showSuccess(dialog, "New medicine registered successfully!");
                }

                dialog.dispose();
                refreshTable();
            } catch (ValidationException ex) {
                UITheme.showError(dialog, ex.getMessage());
            } catch (Exception ex) {
                UITheme.showError(dialog, "Unexpected error: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BODY_BOLD);
        lbl.setForeground(UITheme.COLOR_TEXT_MAIN);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setFont(UITheme.FONT_BODY);
        field.setPreferredSize(new Dimension(220, 30));
        panel.add(field, gbc);
    }
}

package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import exception.ExpiredMedicineException;
import exception.InsufficientStockException;
import exception.ValidationException;
import model.Medicine;
import model.Sale;
import model.SaleItem;
import service.BillingService;
import service.InventoryService;
import ui.components.RoundedPanel;
import ui.components.SearchTextField;
import ui.components.StyledButton;
import util.DateUtil;
import util.UITheme;

/**
 * Module 3: Sales & POS Billing
 * High-speed Point of Sale terminal with live stock checks, cart calculations,
 * FEFO batch visibility, receipt generator, and automated inventory deduction.
 */
public class BillingPanel extends JPanel {
    private final InventoryService inventoryService;
    private final BillingService billingService;

    // Active Cart
    private Sale activeCart;

    // Left Catalog UI
    private SearchTextField catalogSearchField;
    private JTable catalogTable;
    private DefaultTableModel catalogTableModel;
    private JSpinner quantitySpinner;

    // Right Cart UI
    private JTextField txtCustomerName;
    private JTextField txtCustomerPhone;
    private JComboBox<String> cmbPaymentMethod;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;

    // Totals Labels
    private JLabel lblSubtotalVal;
    private JTextField txtDiscount;
    private JLabel lblTaxVal;
    private JLabel lblTotalVal;
    private JLabel lblBillNumber;

    public BillingPanel(InventoryService inventoryService, BillingService billingService) {
        this.inventoryService = inventoryService;
        this.billingService = billingService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initNewCart();
        initUI();
        refreshCatalog();

        inventoryService.addChangeListener(this::refreshCatalog);
    }

    private void initNewCart() {
        this.activeCart = new Sale();
        this.activeCart.setBillNumber(billingService.generateNextBillNumber());
        this.activeCart.setDateTime(LocalDateTime.now());
    }

    private void initUI() {
        // Top Header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Point of Sale (POS) & Billing");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel subLbl = new JLabel("Dispense medicines, verify stock in real-time, and generate customer receipts");
        subLbl.setFont(UITheme.FONT_BODY);
        subLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        titleBox.add(titleLbl);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subLbl);
        headerRow.add(titleBox, BorderLayout.WEST);

        lblBillNumber = new JLabel("Invoice: " + activeCart.getBillNumber());
        lblBillNumber.setFont(UITheme.FONT_SUBTITLE);
        lblBillNumber.setForeground(UITheme.COLOR_PRIMARY);
        headerRow.add(lblBillNumber, BorderLayout.EAST);

        add(headerRow, BorderLayout.NORTH);

        // Center: Split Panes (55% Left Catalog, 45% Right Cart)
        JPanel splitContainer = new JPanel(new BorderLayout(16, 0));
        splitContainer.setOpaque(false);

        // LEFT: Medicine Catalog Panel
        RoundedPanel leftCatalogCard = new RoundedPanel(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        leftCatalogCard.setLayout(new BorderLayout(0, 12));
        leftCatalogCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Catalog Search & Header
        JPanel catalogHeader = new JPanel(new BorderLayout(8, 8));
        catalogHeader.setOpaque(false);

        JLabel catHeading = new JLabel("Medicine Catalog");
        catHeading.setFont(UITheme.FONT_SUBTITLE);
        catHeading.setForeground(UITheme.COLOR_TEXT_MAIN);
        catalogHeader.add(catHeading, BorderLayout.NORTH);

        catalogSearchField = new SearchTextField("Type medicine name or ID to search catalog...", 20);
        catalogSearchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshCatalog(); }
            public void removeUpdate(DocumentEvent e) { refreshCatalog(); }
            public void changedUpdate(DocumentEvent e) { refreshCatalog(); }
        });
        catalogHeader.add(catalogSearchField, BorderLayout.CENTER);

        leftCatalogCard.add(catalogHeader, BorderLayout.NORTH);

        // Catalog Table
        String[] catCols = {"ID", "Medicine Name", "Batch", "Available", "Price ($)", "Expiry Date", "Status"};
        catalogTableModel = new DefaultTableModel(catCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        catalogTable = new JTable(catalogTableModel);
        UITheme.styleTable(catalogTable);
        catalogTable.getColumnModel().getColumn(6).setCellRenderer(new UITheme.StatusBadgeRenderer());
        catalogTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        catalogTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        catalogTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        catalogTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        catalogTable.getColumnModel().getColumn(4).setPreferredWidth(65);
        catalogTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        catalogTable.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane catScrollPane = new JScrollPane(catalogTable);
        catScrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        catScrollPane.getViewport().setBackground(Color.WHITE);
        leftCatalogCard.add(catScrollPane, BorderLayout.CENTER);

        // Bottom Add-to-Cart controls
        JPanel addBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        addBar.setOpaque(false);

        JLabel qtyLabel = new JLabel("Qty:");
        qtyLabel.setFont(UITheme.FONT_BODY_BOLD);

        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        quantitySpinner.setFont(UITheme.FONT_BODY);
        quantitySpinner.setPreferredSize(new Dimension(80, 34));

        StyledButton btnAddToCart = new StyledButton("➕ Add To Bill", StyledButton.ButtonType.PRIMARY);
        btnAddToCart.addActionListener(e -> addItemToCart());

        addBar.add(qtyLabel);
        addBar.add(quantitySpinner);
        addBar.add(btnAddToCart);

        leftCatalogCard.add(addBar, BorderLayout.SOUTH);
        splitContainer.add(leftCatalogCard, BorderLayout.CENTER);

        // RIGHT: Active Cart Panel
        RoundedPanel rightCartCard = new RoundedPanel(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        rightCartCard.setLayout(new BorderLayout(0, 12));
        rightCartCard.setPreferredSize(new Dimension(480, 0));
        rightCartCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Customer Inputs
        JPanel customerBox = new JPanel(new GridBagLayout());
        customerBox.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        txtCustomerName = new JTextField("Walk-in Customer");
        txtCustomerName.setFont(UITheme.FONT_BODY);
        txtCustomerPhone = new JTextField();
        txtCustomerPhone.setFont(UITheme.FONT_BODY);
        cmbPaymentMethod = new JComboBox<>(new String[]{"Cash", "Credit/Debit Card", "UPI / QR Code", "Insurance"});
        cmbPaymentMethod.setFont(UITheme.FONT_BODY);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        customerBox.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        customerBox.add(txtCustomerName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        customerBox.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        customerBox.add(txtCustomerPhone, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        customerBox.add(new JLabel("Payment:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        customerBox.add(cmbPaymentMethod, gbc);

        rightCartCard.add(customerBox, BorderLayout.NORTH);

        // Cart Table
        String[] cartCols = {"Medicine Name", "Batch", "Price", "Qty", "Subtotal"};
        cartTableModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        cartTable = new JTable(cartTableModel);
        UITheme.styleTable(cartTable);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(65);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(45);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(70);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        cartScroll.getViewport().setBackground(Color.WHITE);
        rightCartCard.add(cartScroll, BorderLayout.CENTER);

        // Cart Footer & Totals Calculation Box
        JPanel cartFooter = new JPanel();
        cartFooter.setOpaque(false);
        cartFooter.setLayout(new BoxLayout(cartFooter, BoxLayout.Y_AXIS));

        // Action to remove item or update qty
        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        cartActions.setOpaque(false);
        StyledButton btnRemove = new StyledButton("Remove Item", StyledButton.ButtonType.DANGER);
        btnRemove.setFont(UITheme.FONT_CAPTION);
        btnRemove.setPreferredSize(new Dimension(105, 28));
        btnRemove.addActionListener(e -> removeSelectedCartItem());

        StyledButton btnClear = new StyledButton("Clear All", StyledButton.ButtonType.SECONDARY);
        btnClear.setFont(UITheme.FONT_CAPTION);
        btnClear.setPreferredSize(new Dimension(90, 28));
        btnClear.addActionListener(e -> clearCart());

        cartActions.add(btnRemove);
        cartActions.add(btnClear);
        cartFooter.add(cartActions);
        cartFooter.add(Box.createVerticalStrut(10));

        // Subtotal, Discount, Tax, Total
        JPanel calcPanel = new JPanel(new GridLayout(4, 2, 4, 6));
        calcPanel.setOpaque(false);

        calcPanel.add(new JLabel("Subtotal:"));
        lblSubtotalVal = new JLabel("$ 0.00", JLabel.RIGHT);
        lblSubtotalVal.setFont(UITheme.FONT_BODY_BOLD);
        calcPanel.add(lblSubtotalVal);

        calcPanel.add(new JLabel("Discount ($):"));
        txtDiscount = new JTextField("0.00");
        txtDiscount.setHorizontalAlignment(JTextField.RIGHT);
        txtDiscount.setFont(UITheme.FONT_BODY);
        txtDiscount.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateTotals(); }
            public void removeUpdate(DocumentEvent e) { updateTotals(); }
            public void changedUpdate(DocumentEvent e) { updateTotals(); }
        });
        calcPanel.add(txtDiscount);

        calcPanel.add(new JLabel("Tax / GST (5%):"));
        lblTaxVal = new JLabel("$ 0.00", JLabel.RIGHT);
        lblTaxVal.setFont(UITheme.FONT_BODY);
        calcPanel.add(lblTaxVal);

        JLabel lblTotalTitle = new JLabel("TOTAL DUE:");
        lblTotalTitle.setFont(UITheme.FONT_SUBTITLE);
        calcPanel.add(lblTotalTitle);

        lblTotalVal = new JLabel("$ 0.00", JLabel.RIGHT);
        lblTotalVal.setFont(UITheme.FONT_STAT_NUM.deriveFont(22f));
        lblTotalVal.setForeground(UITheme.COLOR_PRIMARY);
        calcPanel.add(lblTotalVal);

        cartFooter.add(calcPanel);
        cartFooter.add(Box.createVerticalStrut(14));

        // Checkout Button
        StyledButton btnCheckout = new StyledButton("✓ Complete Sale & Generate Bill", StyledButton.ButtonType.SUCCESS);
        btnCheckout.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        btnCheckout.setFont(UITheme.FONT_SUBTITLE);
        btnCheckout.addActionListener(e -> processCheckout());

        cartFooter.add(btnCheckout);

        rightCartCard.add(cartFooter, BorderLayout.SOUTH);
        splitContainer.add(rightCartCard, BorderLayout.EAST);

        add(splitContainer, BorderLayout.CENTER);
    }

    public void refreshCatalog() {
        String q = catalogSearchField != null ? catalogSearchField.getText() : "";
        List<Medicine> meds = inventoryService.search(q, null, null);

        catalogTableModel.setRowCount(0);
        for (Medicine m : meds) {
            catalogTableModel.addRow(new Object[]{
                    m.getId(),
                    m.getName(),
                    m.getBatchNumber(),
                    m.getQuantity() + " units",
                    String.format("%.2f", m.getPrice()),
                    DateUtil.formatDate(m.getExpiryDate()),
                    m.getStatus()
            });
        }
    }

    private void addItemToCart() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow < 0) {
            UITheme.showWarning(this, "Please select a medicine from the catalog table first.");
            return;
        }

        String medId = (String) catalogTableModel.getValueAt(selectedRow, 0);
        int qty = (Integer) quantitySpinner.getValue();

        try {
            SaleItem item = billingService.prepareCartItem(medId, qty, activeCart);

            // Check if already in active cart -> update quantity
            boolean merged = false;
            for (SaleItem existing : activeCart.getItems()) {
                if (existing.getMedicineId().equalsIgnoreCase(medId)) {
                    existing.setQuantity(existing.getQuantity() + qty);
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                activeCart.addItem(item);
            }

            refreshCartTable();
            quantitySpinner.setValue(1);
        } catch (ExpiredMedicineException ex) {
            UITheme.showError(this, "SAFETY RESTRICTION:\n" + ex.getMessage() + "\nExpired medicines cannot be sold!");
        } catch (InsufficientStockException ex) {
            UITheme.showWarning(this, "STOCK ALERT:\n" + ex.getMessage());
        } catch (ValidationException ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    private void removeSelectedCartItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            UITheme.showWarning(this, "Please select an item from the cart to remove.");
            return;
        }
        activeCart.removeItem(selectedRow);
        refreshCartTable();
    }

    private void clearCart() {
        if (activeCart.getItems().isEmpty()) return;
        boolean confirm = UITheme.showConfirm(this, "Clear all medicines from the current bill?", "Clear Cart");
        if (confirm) {
            activeCart.clearItems();
            refreshCartTable();
        }
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (SaleItem it : activeCart.getItems()) {
            cartTableModel.addRow(new Object[]{
                    it.getMedicineName(),
                    it.getBatchNumber(),
                    String.format("$ %.2f", it.getUnitPrice()),
                    it.getQuantity(),
                    String.format("$ %.2f", it.getSubtotal())
            });
        }
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = 0.0;
        for (SaleItem it : activeCart.getItems()) {
            subtotal += it.getSubtotal();
        }

        double discount = 0.0;
        if (txtDiscount != null) {
            try {
                discount = Double.parseDouble(txtDiscount.getText().trim());
                if (discount < 0) discount = 0;
            } catch (NumberFormatException ignored) {
            }
        }

        // 5% standard pharmacy sales tax
        double taxableAmount = Math.max(0, subtotal - discount);
        double tax = taxableAmount * 0.05;
        double total = taxableAmount + tax;

        activeCart.setDiscountAmount(discount);
        activeCart.setTaxAmount(tax);
        activeCart.calculateTotals();

        lblSubtotalVal.setText(String.format("$ %.2f", subtotal));
        lblTaxVal.setText(String.format("$ %.2f", tax));
        lblTotalVal.setText(String.format("$ %.2f", total));
    }

    private void processCheckout() {
        if (activeCart.getItems().isEmpty()) {
            UITheme.showWarning(this, "Cannot complete sale: Cart is empty!");
            return;
        }

        String custName = txtCustomerName.getText().trim();
        String custPhone = txtCustomerPhone.getText().trim();
        String payment = (String) cmbPaymentMethod.getSelectedItem();

        activeCart.setCustomerName(custName.isEmpty() ? "Walk-in Customer" : custName);
        activeCart.setCustomerContact(custPhone.isEmpty() ? "N/A" : custPhone);
        activeCart.setPaymentMethod(payment);

        try {
            Sale completedSale = billingService.checkout(activeCart);

            // Show Receipt Dialog
            showReceiptPreview(completedSale);

            // Reset cart for next customer
            initNewCart();
            lblBillNumber.setText("Invoice: " + activeCart.getBillNumber());
            txtCustomerName.setText("Walk-in Customer");
            txtCustomerPhone.setText("");
            txtDiscount.setText("0.00");
            refreshCartTable();
            refreshCatalog();

        } catch (ExpiredMedicineException ex) {
            UITheme.showError(this, "Transaction blocked: Expired medicine detected.\n" + ex.getMessage());
        } catch (InsufficientStockException ex) {
            UITheme.showError(this, "Transaction blocked: Insufficient inventory.\n" + ex.getMessage());
        } catch (ValidationException ex) {
            UITheme.showError(this, ex.getMessage());
        }
    }

    /**
     * Displays a clean on-screen receipt preview modal with save/print options.
     */
    private void showReceiptPreview(Sale sale) {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, "MEDICARE - Bill Receipt #" + sale.getBillNumber(), true);
        dialog.setSize(520, 640);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titleLbl = new JLabel("Transaction Completed Successfully! ✓");
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        titleLbl.setForeground(UITheme.COLOR_SUCCESS);
        content.add(titleLbl, BorderLayout.NORTH);

        JTextArea txtReceipt = new JTextArea(sale.generateReceiptText());
        txtReceipt.setFont(UITheme.FONT_MONO);
        txtReceipt.setEditable(false);
        txtReceipt.setBackground(new Color(248, 250, 252));
        txtReceipt.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(txtReceipt);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        content.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JLabel fileNotice = new JLabel("Saved to receipts/" + sale.getBillNumber() + ".txt");
        fileNotice.setFont(UITheme.FONT_CAPTION);
        fileNotice.setForeground(UITheme.COLOR_TEXT_MUTED);

        StyledButton btnClose = new StyledButton("Close & Next Customer", StyledButton.ButtonType.PRIMARY);
        btnClose.addActionListener(e -> dialog.dispose());

        btnRow.add(fileNotice);
        btnRow.add(Box.createHorizontalStrut(16));
        btnRow.add(btnClose);
        content.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }
}

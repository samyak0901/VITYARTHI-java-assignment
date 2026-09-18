package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
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
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import model.Sale;
import service.BillingService;
import service.ReportService;
import ui.components.RoundedPanel;
import ui.components.SearchTextField;
import ui.components.StyledButton;
import util.DateUtil;
import util.UITheme;

/**
 * Module 4 (Part 1): Sales History
 * Displays past sales transactions, offers date-range filtering, search by bill or customer,
 * and allows re-viewing or printing receipts.
 */
public class SalesHistoryPanel extends JPanel {
    private final BillingService billingService;
    private final ReportService reportService;

    private JTable salesTable;
    private DefaultTableModel tableModel;
    private SearchTextField searchField;
    private JComboBox<String> timeFilterCombo;
    private JLabel summaryLabel;

    public SalesHistoryPanel(BillingService billingService, ReportService reportService) {
        this.billingService = billingService;
        this.reportService = reportService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
        refreshSales();

        billingService.addSaleListener(this::refreshSales);
    }

    private void initUI() {
        // TOP: Header & Filters
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // Title Row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Sales & Transaction History");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel subLbl = new JLabel("View past customer invoices, payment methods, and historical receipts");
        subLbl.setFont(UITheme.FONT_BODY);
        subLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        titleBox.add(titleLbl);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subLbl);
        titleRow.add(titleBox, BorderLayout.WEST);

        // Actions
        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsBox.setOpaque(false);

        StyledButton btnViewReceipt = new StyledButton("📄 View / Print Receipt", StyledButton.ButtonType.PRIMARY);
        btnViewReceipt.addActionListener(e -> viewSelectedReceipt());

        StyledButton btnRefresh = new StyledButton("↻ Refresh", StyledButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshSales());

        actionsBox.add(btnViewReceipt);
        actionsBox.add(btnRefresh);
        titleRow.add(actionsBox, BorderLayout.EAST);

        topContainer.add(titleRow);
        topContainer.add(Box.createVerticalStrut(16));

        // Filter / Search Toolbar
        RoundedPanel filterBar = new RoundedPanel(12, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        filterBar.setLayout(new BorderLayout(12, 0));
        filterBar.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        searchField = new SearchTextField("Search by bill number, customer name, or payment mode...", 25);
        searchField.setPreferredSize(new Dimension(380, 36));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterData(); }
            public void removeUpdate(DocumentEvent e) { filterData(); }
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });
        filterBar.add(searchField, BorderLayout.WEST);

        JPanel filtersRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filtersRight.setOpaque(false);

        JLabel lblTime = new JLabel("Timeframe:");
        lblTime.setFont(UITheme.FONT_BODY_BOLD);
        lblTime.setForeground(UITheme.COLOR_TEXT_MUTED);

        timeFilterCombo = new JComboBox<>(new String[]{"All Time", "Today", "Last 7 Days", "This Month"});
        timeFilterCombo.setFont(UITheme.FONT_BODY);
        timeFilterCombo.setPreferredSize(new Dimension(140, 34));
        timeFilterCombo.addActionListener(e -> filterData());

        summaryLabel = new JLabel("0 records");
        summaryLabel.setFont(UITheme.FONT_BODY);
        summaryLabel.setForeground(UITheme.COLOR_TEXT_MUTED);

        filtersRight.add(lblTime);
        filtersRight.add(timeFilterCombo);
        filtersRight.add(Box.createHorizontalStrut(10));
        filtersRight.add(summaryLabel);

        filterBar.add(filtersRight, BorderLayout.EAST);
        topContainer.add(filterBar);

        add(topContainer, BorderLayout.NORTH);

        // CENTER: Sales Table
        RoundedPanel tableCard = new RoundedPanel(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        tableCard.setLayout(new BorderLayout());

        String[] columns = {
                "Bill Number", "Date & Time", "Customer Name", "Contact",
                "Items Sold", "Subtotal", "Discount", "Tax", "Total Paid", "Payment Mode"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        salesTable = new JTable(tableModel);
        UITheme.styleTable(salesTable);

        salesTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        salesTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        salesTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        salesTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        salesTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        salesTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        salesTable.getColumnModel().getColumn(6).setPreferredWidth(75);
        salesTable.getColumnModel().getColumn(7).setPreferredWidth(75);
        salesTable.getColumnModel().getColumn(8).setPreferredWidth(95);
        salesTable.getColumnModel().getColumn(9).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(salesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshSales() {
        filterData();
    }

    private void filterData() {
        String filter = timeFilterCombo != null ? (String) timeFilterCombo.getSelectedItem() : "All Time";
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        List<Sale> list = reportService.filterSales(filter);

        tableModel.setRowCount(0);
        double totalVolume = 0.0;
        int count = 0;

        for (Sale s : list) {
            // Apply text query
            boolean match = query.isEmpty()
                    || s.getBillNumber().toLowerCase().contains(query)
                    || s.getCustomerName().toLowerCase().contains(query)
                    || s.getPaymentMethod().toLowerCase().contains(query)
                    || s.getCustomerContact().toLowerCase().contains(query);

            if (match) {
                tableModel.addRow(new Object[]{
                        s.getBillNumber(),
                        DateUtil.formatDateTime(s.getDateTime()),
                        s.getCustomerName(),
                        s.getCustomerContact(),
                        s.getTotalQuantity() + " units",
                        String.format("$ %.2f", s.getSubtotal()),
                        String.format("$ %.2f", s.getDiscountAmount()),
                        String.format("$ %.2f", s.getTaxAmount()),
                        String.format("$ %.2f", s.getTotalAmount()),
                        s.getPaymentMethod()
                });
                totalVolume += s.getTotalAmount();
                count++;
            }
        }

        if (summaryLabel != null) {
            summaryLabel.setText(String.format("Showing %d sales ($ %.2f total)", count, totalVolume));
        }
    }

    private void viewSelectedReceipt() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow < 0) {
            UITheme.showWarning(this, "Please select an invoice from the table to view its receipt.");
            return;
        }

        String billNo = (String) tableModel.getValueAt(selectedRow, 0);
        Sale target = null;
        for (Sale s : billingService.getSalesHistory()) {
            if (s.getBillNumber().equalsIgnoreCase(billNo)) {
                target = s;
                break;
            }
        }

        if (target != null) {
            showReceiptModal(target);
        } else {
            UITheme.showError(this, "Could not locate bill details for " + billNo);
        }
    }

    private void showReceiptModal(Sale sale) {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, "MEDICARE Invoice Receipt - " + sale.getBillNumber(), true);
        dialog.setSize(520, 640);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titleLbl = new JLabel("Official Invoice Receipt");
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MAIN);
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

        JLabel fileNotice = new JLabel("File: receipts/" + sale.getBillNumber() + ".txt");
        fileNotice.setFont(UITheme.FONT_CAPTION);
        fileNotice.setForeground(UITheme.COLOR_TEXT_MUTED);

        StyledButton btnClose = new StyledButton("Close", StyledButton.ButtonType.PRIMARY);
        btnClose.addActionListener(e -> dialog.dispose());

        btnRow.add(fileNotice);
        btnRow.add(Box.createHorizontalStrut(16));
        btnRow.add(btnClose);
        content.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }
}

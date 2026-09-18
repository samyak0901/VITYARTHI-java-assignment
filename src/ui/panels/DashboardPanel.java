package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import model.Medicine;
import model.StockStatus;
import service.BillingService;
import service.InventoryService;
import service.ReportService;
import ui.components.RoundedPanel;
import ui.components.StatCard;
import ui.components.StyledButton;
import util.DateUtil;
import util.UITheme;

/**
 * Modern overview dashboard showcasing KPI metrics, critical inventory alerts,
 * and quick-action navigation.
 */
public class DashboardPanel extends JPanel {
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final ReportService reportService;
    private final java.util.function.Consumer<String> navigationHandler;

    // Stat Cards
    private StatCard cardTotalMedicines;
    private StatCard cardTotalStock;
    private StatCard cardLowStock;
    private StatCard cardExpiringSoon;
    private StatCard cardExpired;
    private StatCard cardTodaySales;

    // Alert Table
    private JTable alertTable;
    private DefaultTableModel alertTableModel;

    // Summary Details Labels
    private JLabel totalValuationLabel;
    private JLabel todayTxnCountLabel;
    private JLabel avgOrderValLabel;

    public DashboardPanel(InventoryService inventoryService, BillingService billingService,
                          ReportService reportService, java.util.function.Consumer<String> navigationHandler) {
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.reportService = reportService;
        this.navigationHandler = navigationHandler;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();

        // Register observer listeners for automatic dashboard updates
        inventoryService.addChangeListener(this::refreshData);
        billingService.addSaleListener(this::refreshData);
    }

    private void initUI() {
        // TOP SECTION: Header + KPI Grid
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // Welcome & Quick Action Bar
        JPanel headerBar = new JPanel(new BorderLayout(16, 0));
        headerBar.setOpaque(false);

        JPanel titlesPanel = new JPanel();
        titlesPanel.setOpaque(false);
        titlesPanel.setLayout(new BoxLayout(titlesPanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Pharmacy Overview Dashboard");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel subtitleLbl = new JLabel("Real-time inventory levels, expiry warnings, and financial metrics");
        subtitleLbl.setFont(UITheme.FONT_BODY);
        subtitleLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        titlesPanel.add(titleLbl);
        titlesPanel.add(Box.createVerticalStrut(4));
        titlesPanel.add(subtitleLbl);
        headerBar.add(titlesPanel, BorderLayout.WEST);

        // Action Buttons Bar
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsBar.setOpaque(false);

        StyledButton btnNewSale = new StyledButton("+ New Sale", StyledButton.ButtonType.SUCCESS);
        btnNewSale.addActionListener(e -> navigationHandler.accept("Billing"));

        StyledButton btnAddMed = new StyledButton("+ Add Medicine", StyledButton.ButtonType.PRIMARY);
        btnAddMed.addActionListener(e -> navigationHandler.accept("Medicines"));

        StyledButton btnRefresh = new StyledButton("↻ Refresh", StyledButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshData());

        actionsBar.add(btnNewSale);
        actionsBar.add(btnAddMed);
        actionsBar.add(btnRefresh);
        headerBar.add(actionsBar, BorderLayout.EAST);

        topContainer.add(headerBar);
        topContainer.add(Box.createVerticalStrut(18));

        // KPI Metric Cards Grid (2 rows x 3 columns)
        JPanel kpiGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        kpiGrid.setOpaque(false);

        cardTotalMedicines = new StatCard("Total Medicines", "0", "Catalog products", "💊", UITheme.COLOR_PRIMARY);
        cardTotalStock = new StatCard("Total Stock Units", "0", "Available in store", "📦", UITheme.COLOR_ACCENT);
        cardLowStock = new StatCard("Low Stock Alert", "0", "Below threshold", "⚠️", UITheme.COLOR_WARNING);
        cardExpiringSoon = new StatCard("Expiring Soon", "0", "Within next 30 days", "⏳", UITheme.COLOR_ORANGE);
        cardExpired = new StatCard("Expired Medicines", "0", "Do not dispense", "🚫", UITheme.COLOR_DANGER);
        cardTodaySales = new StatCard("Today's Revenue", "$ 0.00", "0 transactions", "💰", UITheme.COLOR_SUCCESS);

        kpiGrid.add(cardTotalMedicines);
        kpiGrid.add(cardTotalStock);
        kpiGrid.add(cardLowStock);
        kpiGrid.add(cardExpiringSoon);
        kpiGrid.add(cardExpired);
        kpiGrid.add(cardTodaySales);

        topContainer.add(kpiGrid);
        add(topContainer, BorderLayout.NORTH);

        // CENTER SECTION: Split Layout (Left: Priority Action Alerts; Right: Quick Financial Stats)
        JPanel contentSplit = new JPanel(new BorderLayout(16, 0));
        contentSplit.setOpaque(false);

        // Left Panel: Critical Alerts Card
        RoundedPanel alertCard = new RoundedPanel(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        alertCard.setLayout(new BorderLayout(0, 12));
        alertCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel alertHeader = new JPanel(new BorderLayout());
        alertHeader.setOpaque(false);
        JLabel alertTitle = new JLabel("Critical Inventory & Expiry Alerts");
        alertTitle.setFont(UITheme.FONT_SUBTITLE);
        alertTitle.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel alertSub = new JLabel("Items requiring immediate attention (Expired, Expiring Soon, or Low Stock)");
        alertSub.setFont(UITheme.FONT_CAPTION);
        alertSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel alertTitleBox = new JPanel();
        alertTitleBox.setOpaque(false);
        alertTitleBox.setLayout(new BoxLayout(alertTitleBox, BoxLayout.Y_AXIS));
        alertTitleBox.add(alertTitle);
        alertTitleBox.add(alertSub);

        alertHeader.add(alertTitleBox, BorderLayout.WEST);

        StyledButton btnViewAllInv = new StyledButton("Open Inventory", StyledButton.ButtonType.SECONDARY);
        btnViewAllInv.setPreferredSize(new Dimension(130, 32));
        btnViewAllInv.addActionListener(e -> navigationHandler.accept("Inventory"));
        alertHeader.add(btnViewAllInv, BorderLayout.EAST);

        alertCard.add(alertHeader, BorderLayout.NORTH);

        // Table for Alerts
        String[] cols = {"Medicine ID", "Medicine Name", "Batch", "Stock Qty", "Expiry Date", "Status"};
        alertTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        alertTable = new JTable(alertTableModel);
        UITheme.styleTable(alertTable);
        alertTable.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusBadgeRenderer());
        alertTable.getColumnModel().getColumn(0).setPreferredWidth(85);
        alertTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        alertTable.getColumnModel().getColumn(2).setPreferredWidth(75);
        alertTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        alertTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        alertTable.getColumnModel().getColumn(5).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(alertTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        alertCard.add(scrollPane, BorderLayout.CENTER);

        contentSplit.add(alertCard, BorderLayout.CENTER);

        // Right Panel: Financial & Store Health Summary Card
        RoundedPanel summaryCard = new RoundedPanel(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        summaryCard.setLayout(new BorderLayout(0, 14));
        summaryCard.setPreferredSize(new Dimension(280, 0));
        summaryCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel sumTitle = new JLabel("Store Financial Status");
        sumTitle.setFont(UITheme.FONT_SUBTITLE);
        sumTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        summaryCard.add(sumTitle, BorderLayout.NORTH);

        JPanel sumItems = new JPanel();
        sumItems.setOpaque(false);
        sumItems.setLayout(new BoxLayout(sumItems, BoxLayout.Y_AXIS));

        totalValuationLabel = createSummaryRow(sumItems, "Total Inventory Value:", "$ 0.00");
        todayTxnCountLabel = createSummaryRow(sumItems, "Today's Invoices:", "0");
        avgOrderValLabel = createSummaryRow(sumItems, "Avg. Ticket Value:", "$ 0.00");

        sumItems.add(Box.createVerticalGlue());

        // Helpful FEFO notice banner
        RoundedPanel fefoBanner = new RoundedPanel(10, new Color(240, 253, 250), UITheme.COLOR_PRIMARY_LIGHT, 1);
        fefoBanner.setLayout(new BorderLayout(8, 6));
        fefoBanner.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel fefoTitle = new JLabel("FEFO Protocol Active");
        fefoTitle.setFont(UITheme.FONT_BODY_BOLD);
        fefoTitle.setForeground(UITheme.COLOR_PRIMARY_DARK);

        JLabel fefoDesc = new JLabel("<html><b>First Expiry, First Out:</b> Earliest expiring batches are automatically prioritized for sales dispensing.</html>");
        fefoDesc.setFont(UITheme.FONT_CAPTION);
        fefoDesc.setForeground(UITheme.COLOR_TEXT_MAIN);

        fefoBanner.add(fefoTitle, BorderLayout.NORTH);
        fefoBanner.add(fefoDesc, BorderLayout.CENTER);
        sumItems.add(fefoBanner);

        summaryCard.add(sumItems, BorderLayout.CENTER);
        contentSplit.add(summaryCard, BorderLayout.EAST);

        add(contentSplit, BorderLayout.CENTER);
    }

    private JLabel createSummaryRow(JPanel parent, String title, String val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_BODY);
        lblTitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        JLabel lblVal = new JLabel(val);
        lblVal.setFont(UITheme.FONT_BODY_BOLD);
        lblVal.setForeground(UITheme.COLOR_TEXT_MAIN);

        row.add(lblTitle, BorderLayout.WEST);
        row.add(lblVal, BorderLayout.EAST);

        parent.add(row);
        parent.add(Box.createVerticalStrut(10));
        return lblVal;
    }

    public void refreshData() {
        // Update KPI Cards
        cardTotalMedicines.setValue(String.valueOf(inventoryService.getTotalMedicinesCount()));
        cardTotalStock.setValue(String.valueOf(inventoryService.getTotalStockUnits()));
        cardLowStock.setValue(String.valueOf(inventoryService.getLowStockCount()));
        cardExpiringSoon.setValue(String.valueOf(inventoryService.getExpiringSoonCount()));
        cardExpired.setValue(String.valueOf(inventoryService.getExpiredCount()));

        double todayRev = reportService.getTodayRevenue();
        int todayTxn = reportService.getTodayTransactionsCount();
        cardTodaySales.setValue(String.format("$ %.2f", todayRev));
        cardTodaySales.setSubtitle(todayTxn + (todayTxn == 1 ? " invoice" : " invoices"));

        // Financial side summary
        totalValuationLabel.setText(String.format("$ %.2f", inventoryService.getTotalInventoryValue()));
        todayTxnCountLabel.setText(String.valueOf(todayTxn));
        avgOrderValLabel.setText(String.format("$ %.2f", reportService.getAverageTransactionValue()));

        // Populate Alert Table (Low Stock, Expiring Soon, Expired)
        alertTableModel.setRowCount(0);
        List<Medicine> all = inventoryService.getAllMedicines();
        int alertCount = 0;
        for (Medicine m : all) {
            if (m.isExpired() || m.isLowStock() || m.isExpiringSoon(30)) {
                alertTableModel.addRow(new Object[]{
                        m.getId(),
                        m.getName(),
                        m.getBatchNumber(),
                        m.getQuantity() + " units",
                        DateUtil.formatDate(m.getExpiryDate()),
                        m.getStatus()
                });
                alertCount++;
            }
        }

        if (alertCount == 0) {
            alertTableModel.addRow(new Object[]{"--", "All inventory is healthy and in stock", "--", "--", "--", StockStatus.IN_STOCK});
        }
    }
}

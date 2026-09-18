package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import service.BillingService;
import service.InventoryService;
import service.ReportService;
import ui.components.RoundedPanel;
import ui.components.StatCard;
import ui.components.StyledButton;
import util.UITheme;

/**
 * Module 4 (Part 2): Reports & Analytics
 * Financial intelligence, best-selling product rankings, transaction volumes,
 * and executive summary reports.
 */
public class ReportsPanel extends JPanel {
    private final BillingService billingService;
    private final ReportService reportService;
    private final InventoryService inventoryService;

    // Metric Cards
    private StatCard cardTotalRev;
    private StatCard cardTotalTxn;
    private StatCard cardTodayRev;
    private StatCard cardAvgOrder;

    // Best Sellers Table
    private JTable bestSellersTable;
    private DefaultTableModel bestSellersModel;

    public ReportsPanel(BillingService billingService, ReportService reportService, InventoryService inventoryService) {
        this.billingService = billingService;
        this.reportService = reportService;
        this.inventoryService = inventoryService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
        refreshReports();

        billingService.addSaleListener(this::refreshReports);
    }

    private void initUI() {
        // TOP: Title & Action
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Sales Analytics & Performance Reports");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel subLbl = new JLabel("Revenue growth metrics, best-selling pharmaceuticals, and financial reports");
        subLbl.setFont(UITheme.FONT_BODY);
        subLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        titleBox.add(titleLbl);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subLbl);
        headerRow.add(titleBox, BorderLayout.WEST);

        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsBox.setOpaque(false);

        StyledButton btnExportReport = new StyledButton("📊 View Executive Report", StyledButton.ButtonType.PRIMARY);
        btnExportReport.addActionListener(e -> showExecutiveReportDialog());

        StyledButton btnRefresh = new StyledButton("↻ Refresh", StyledButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshReports());

        actionsBox.add(btnExportReport);
        actionsBox.add(btnRefresh);
        headerRow.add(actionsBox, BorderLayout.EAST);

        topContainer.add(headerRow);
        topContainer.add(Box.createVerticalStrut(16));

        // 4 KPI Cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);

        cardTotalRev = new StatCard("Total Lifetime Sales", "$ 0.00", "Gross pharmacy receipts", "💵", UITheme.COLOR_PRIMARY);
        cardTotalTxn = new StatCard("Completed Invoices", "0", "All-time customers served", "🧾", UITheme.COLOR_ACCENT);
        cardTodayRev = new StatCard("Today's Gross Sales", "$ 0.00", "Current day turnover", "📈", UITheme.COLOR_SUCCESS);
        cardAvgOrder = new StatCard("Average Ticket Size", "$ 0.00", "Per customer transaction", "🎯", UITheme.COLOR_ORANGE);

        statsRow.add(cardTotalRev);
        statsRow.add(cardTotalTxn);
        statsRow.add(cardTodayRev);
        statsRow.add(cardAvgOrder);

        topContainer.add(statsRow);
        add(topContainer, BorderLayout.NORTH);

        // CENTER: Best-Sellers Grid + Inventory Valuation Breakdown
        JPanel centerSplit = new JPanel(new BorderLayout(16, 0));
        centerSplit.setOpaque(false);

        // Left Panel: Best Selling Medicines
        RoundedPanel bestSellersCard = new RoundedPanel(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        bestSellersCard.setLayout(new BorderLayout(0, 12));
        bestSellersCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel bsHeader = new JPanel(new BorderLayout());
        bsHeader.setOpaque(false);

        JLabel bsTitle = new JLabel("Top-Performing & Best-Selling Medicines");
        bsTitle.setFont(UITheme.FONT_SUBTITLE);
        bsTitle.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel bsSub = new JLabel("Ranked by cumulative units dispensed to patients");
        bsSub.setFont(UITheme.FONT_CAPTION);
        bsSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel bsTitleBox = new JPanel();
        bsTitleBox.setOpaque(false);
        bsTitleBox.setLayout(new BoxLayout(bsTitleBox, BoxLayout.Y_AXIS));
        bsTitleBox.add(bsTitle);
        bsTitleBox.add(bsSub);
        bsHeader.add(bsTitleBox, BorderLayout.WEST);

        bestSellersCard.add(bsHeader, BorderLayout.NORTH);

        String[] bsCols = {"Rank", "Medicine Name", "Units Dispensed", "Total Revenue Generated"};
        bestSellersModel = new DefaultTableModel(bsCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        bestSellersTable = new JTable(bestSellersModel);
        UITheme.styleTable(bestSellersTable);

        bestSellersTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        bestSellersTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        bestSellersTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        bestSellersTable.getColumnModel().getColumn(3).setPreferredWidth(140);

        JScrollPane bsScroll = new JScrollPane(bestSellersTable);
        bsScroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        bsScroll.getViewport().setBackground(Color.WHITE);
        bestSellersCard.add(bsScroll, BorderLayout.CENTER);

        centerSplit.add(bestSellersCard, BorderLayout.CENTER);

        // Right Panel: Business Health & Financial Indicators
        RoundedPanel healthCard = new RoundedPanel(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        healthCard.setLayout(new BorderLayout(0, 14));
        healthCard.setPreferredSize(new Dimension(300, 0));
        healthCard.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel healthTitle = new JLabel("Inventory Capital Summary");
        healthTitle.setFont(UITheme.FONT_SUBTITLE);
        healthTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        healthCard.add(healthTitle, BorderLayout.NORTH);

        JPanel healthItems = new JPanel();
        healthItems.setOpaque(false);
        healthItems.setLayout(new BoxLayout(healthItems, BoxLayout.Y_AXIS));

        addInfoRow(healthItems, "Active Catalog SKUs:", String.valueOf(inventoryService.getTotalMedicinesCount()));
        addInfoRow(healthItems, "Warehouse Stock Count:", inventoryService.getTotalStockUnits() + " units");
        addInfoRow(healthItems, "Warehouse Capital Asset:", String.format("$ %.2f", inventoryService.getTotalInventoryValue()));
        addInfoRow(healthItems, "Low Stock Re-orders:", inventoryService.getLowStockCount() + " items");
        addInfoRow(healthItems, "Expiring Within 30d:", inventoryService.getExpiringSoonCount() + " items");
        addInfoRow(healthItems, "Expired (Restricted):", inventoryService.getExpiredCount() + " items");

        healthItems.add(Box.createVerticalGlue());

        // Quick Tip Pill
        RoundedPanel tipPanel = new RoundedPanel(10, new Color(248, 250, 252), UITheme.COLOR_BORDER, 1);
        tipPanel.setLayout(new BorderLayout(6, 6));
        tipPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel tipHeader = new JLabel("Audit & Compliance Tip");
        tipHeader.setFont(UITheme.FONT_BODY_BOLD);
        tipHeader.setForeground(UITheme.COLOR_PRIMARY);

        JLabel tipBody = new JLabel("<html>Perform routine weekly reconciliations of physical inventory with the <b>FEFO Expiring Soon</b> tab to avoid spoilage.</html>");
        tipBody.setFont(UITheme.FONT_CAPTION);
        tipBody.setForeground(UITheme.COLOR_TEXT_MUTED);

        tipPanel.add(tipHeader, BorderLayout.NORTH);
        tipPanel.add(tipBody, BorderLayout.CENTER);
        healthItems.add(tipPanel);

        healthCard.add(healthItems, BorderLayout.CENTER);
        centerSplit.add(healthCard, BorderLayout.EAST);

        add(centerSplit, BorderLayout.CENTER);
    }

    private void addInfoRow(JPanel parent, String title, String val) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_BODY);
        lblTitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        JLabel lblVal = new JLabel(val);
        lblVal.setFont(UITheme.FONT_BODY_BOLD);
        lblVal.setForeground(UITheme.COLOR_TEXT_MAIN);

        row.add(lblTitle, BorderLayout.WEST);
        row.add(lblVal, BorderLayout.EAST);

        parent.add(row);
        parent.add(Box.createVerticalStrut(8));
    }

    public void refreshReports() {
        cardTotalRev.setValue(String.format("$ %.2f", reportService.getTotalRevenue()));
        cardTotalTxn.setValue(String.valueOf(reportService.getTotalTransactions()));
        cardTodayRev.setValue(String.format("$ %.2f", reportService.getTodayRevenue()));
        cardAvgOrder.setValue(String.format("$ %.2f", reportService.getAverageTransactionValue()));

        // Best sellers table
        bestSellersModel.setRowCount(0);
        List<ReportService.BestSellerRecord> bestSellers = reportService.getBestSellingMedicines();
        int rank = 1;
        for (ReportService.BestSellerRecord rec : bestSellers) {
            bestSellersModel.addRow(new Object[]{
                    "#" + (rank++),
                    rec.getMedicineName(),
                    rec.getTotalUnitsSold() + " units",
                    String.format("$ %.2f", rec.getTotalRevenue())
            });
        }

        if (bestSellers.isEmpty()) {
            bestSellersModel.addRow(new Object[]{"--", "No sales completed yet", "--", "--"});
        }
    }

    private void showExecutiveReportDialog() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, "MEDICARE - Executive Pharmacy Report", true);
        dialog.setSize(560, 620);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titleLbl = new JLabel("Executive Financial Summary");
        titleLbl.setFont(UITheme.FONT_SUBTITLE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MAIN);
        content.add(titleLbl, BorderLayout.NORTH);

        JTextArea txtReport = new JTextArea(reportService.generateSummaryReportText());
        txtReport.setFont(UITheme.FONT_MONO);
        txtReport.setEditable(false);
        txtReport.setBackground(new Color(248, 250, 252));
        txtReport.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(txtReport);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        content.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        StyledButton btnClose = new StyledButton("Close Report", StyledButton.ButtonType.PRIMARY);
        btnClose.addActionListener(e -> dialog.dispose());

        btnRow.add(btnClose);
        content.add(btnRow, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }
}

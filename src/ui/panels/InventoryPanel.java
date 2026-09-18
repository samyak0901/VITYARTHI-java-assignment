package ui.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import exception.ValidationException;
import model.Medicine;
import model.StockStatus;
import service.InventoryService;
import ui.components.RoundedPanel;
import ui.components.StatCard;
import ui.components.StyledButton;
import util.DateUtil;
import util.UITheme;

/**
 * Module 2: Inventory & Expiry Management
 * Tracks stock health, highlights FEFO (First Expiry, First Out) priorities,
 * provides one-click restock operations, and flags expired or low-stock items.
 */
public class InventoryPanel extends JPanel {
    private final InventoryService inventoryService;

    // Stat Cards
    private StatCard cardTotalQty;
    private StatCard cardTotalValuation;
    private StatCard cardLowStock;
    private StatCard cardExpiringSoon;
    private StatCard cardExpired;

    // View selector: ALL_FEFO, LOW_STOCK, EXPIRING_SOON, EXPIRED
    private String currentFilter = "ALL_FEFO";

    private StyledButton btnTabFefo;
    private StyledButton btnTabLow;
    private StyledButton btnTabExpiring;
    private StyledButton btnTabExpired;

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JLabel tableSummaryLabel;

    public InventoryPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
        refreshInventory();

        inventoryService.addChangeListener(this::refreshInventory);
    }

    private void initUI() {
        // TOP: Header & Stat Cards
        JPanel topContainer = new JPanel();
        topContainer.setOpaque(false);
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // Header Title Row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Inventory & Expiry Control");
        titleLbl.setFont(UITheme.FONT_TITLE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        JLabel subLbl = new JLabel("Real-time warehouse tracking with FEFO (First Expiry, First Out) prioritization");
        subLbl.setFont(UITheme.FONT_BODY);
        subLbl.setForeground(UITheme.COLOR_TEXT_MUTED);

        titleBox.add(titleLbl);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subLbl);
        headerRow.add(titleBox, BorderLayout.WEST);

        // Restock Button
        JPanel actionsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsBox.setOpaque(false);

        StyledButton btnRestock = new StyledButton("➕ Restock Selected", StyledButton.ButtonType.PRIMARY);
        btnRestock.addActionListener(e -> restockSelectedMedicine());

        StyledButton btnRefresh = new StyledButton("↻ Refresh", StyledButton.ButtonType.SECONDARY);
        btnRefresh.addActionListener(e -> refreshInventory());

        actionsBox.add(btnRestock);
        actionsBox.add(btnRefresh);
        headerRow.add(actionsBox, BorderLayout.EAST);

        topContainer.add(headerRow);
        topContainer.add(Box.createVerticalStrut(16));

        // KPI Metric Cards Row (5 Cards)
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statsRow.setOpaque(false);

        cardTotalQty = new StatCard("Total Units", "0", "Units in warehouse", "📦", UITheme.COLOR_PRIMARY);
        cardTotalValuation = new StatCard("Inventory Value", "$ 0.00", "Asset worth", "💎", UITheme.COLOR_ACCENT);
        cardLowStock = new StatCard("Low Stock Items", "0", "Below minimum limit", "⚠️", UITheme.COLOR_WARNING);
        cardExpiringSoon = new StatCard("Expiring <= 30d", "0", "Prioritize selling", "⏳", UITheme.COLOR_ORANGE);
        cardExpired = new StatCard("Expired Items", "0", "Unsafe to dispense", "🚫", UITheme.COLOR_DANGER);

        cardTotalQty.setPreferredSize(new Dimension(175, 100));
        cardTotalValuation.setPreferredSize(new Dimension(185, 100));
        cardLowStock.setPreferredSize(new Dimension(175, 100));
        cardExpiringSoon.setPreferredSize(new Dimension(175, 100));
        cardExpired.setPreferredSize(new Dimension(175, 100));

        statsRow.add(cardTotalQty);
        statsRow.add(cardTotalValuation);
        statsRow.add(cardLowStock);
        statsRow.add(cardExpiringSoon);
        statsRow.add(cardExpired);

        topContainer.add(statsRow);
        topContainer.add(Box.createVerticalStrut(16));

        // FILTER TABS BAR
        RoundedPanel tabsBar = new RoundedPanel(12, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        tabsBar.setLayout(new BorderLayout(12, 0));
        tabsBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel tabsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tabsLeft.setOpaque(false);

        btnTabFefo = new StyledButton("⭐ All Batches (FEFO Sorted)", StyledButton.ButtonType.PRIMARY);
        btnTabFefo.addActionListener(e -> setTabFilter("ALL_FEFO"));

        btnTabLow = new StyledButton("⚠️ Low Stock Only", StyledButton.ButtonType.SECONDARY);
        btnTabLow.addActionListener(e -> setTabFilter("LOW_STOCK"));

        btnTabExpiring = new StyledButton("⏳ Expiring Soon (30d)", StyledButton.ButtonType.SECONDARY);
        btnTabExpiring.addActionListener(e -> setTabFilter("EXPIRING_SOON"));

        btnTabExpired = new StyledButton("🚫 Expired Medicines", StyledButton.ButtonType.SECONDARY);
        btnTabExpired.addActionListener(e -> setTabFilter("EXPIRED"));

        tabsLeft.add(btnTabFefo);
        tabsLeft.add(btnTabLow);
        tabsLeft.add(btnTabExpiring);
        tabsLeft.add(btnTabExpired);

        tabsBar.add(tabsLeft, BorderLayout.WEST);

        tableSummaryLabel = new JLabel("Showing all batches ordered by earliest expiry");
        tableSummaryLabel.setFont(UITheme.FONT_CAPTION);
        tableSummaryLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        tabsBar.add(tableSummaryLabel, BorderLayout.EAST);

        topContainer.add(tabsBar);
        add(topContainer, BorderLayout.NORTH);

        // CENTER: Data Table
        RoundedPanel tableCard = new RoundedPanel(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        tableCard.setLayout(new BorderLayout());

        String[] columns = {
                "FEFO Rank", "Medicine ID", "Medicine Name", "Batch No.",
                "Stock Qty", "Min Level", "Unit Price", "Total Value", "Expiry Date", "Days Remaining", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        UITheme.styleTable(inventoryTable);
        inventoryTable.getColumnModel().getColumn(10).setCellRenderer(new UITheme.StatusBadgeRenderer());

        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        inventoryTable.getColumnModel().getColumn(5).setPreferredWidth(70);
        inventoryTable.getColumnModel().getColumn(6).setPreferredWidth(75);
        inventoryTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        inventoryTable.getColumnModel().getColumn(8).setPreferredWidth(95);
        inventoryTable.getColumnModel().getColumn(9).setPreferredWidth(100);
        inventoryTable.getColumnModel().getColumn(10).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    private void setTabFilter(String filter) {
        this.currentFilter = filter;
        btnTabFefo.setActive(filter.equals("ALL_FEFO"));
        btnTabLow.setActive(filter.equals("LOW_STOCK"));
        btnTabExpiring.setActive(filter.equals("EXPIRING_SOON"));
        btnTabExpired.setActive(filter.equals("EXPIRED"));

        refreshTableData();
    }

    public void refreshInventory() {
        // Update Stat Cards
        cardTotalQty.setValue(String.valueOf(inventoryService.getTotalStockUnits()));
        cardTotalValuation.setValue(String.format("$ %.2f", inventoryService.getTotalInventoryValue()));
        cardLowStock.setValue(String.valueOf(inventoryService.getLowStockCount()));
        cardExpiringSoon.setValue(String.valueOf(inventoryService.getExpiringSoonCount()));
        cardExpired.setValue(String.valueOf(inventoryService.getExpiredCount()));

        refreshTableData();
    }

    private void refreshTableData() {
        List<Medicine> list;
        switch (currentFilter) {
            case "LOW_STOCK":
                list = inventoryService.getLowStockMedicines();
                tableSummaryLabel.setText(String.format("Showing %d items at or below minimum re-order threshold", list.size()));
                break;
            case "EXPIRING_SOON":
                list = inventoryService.getExpiringSoonMedicines(30);
                tableSummaryLabel.setText(String.format("Showing %d batches expiring within 30 days", list.size()));
                break;
            case "EXPIRED":
                list = inventoryService.getExpiredMedicines();
                tableSummaryLabel.setText(String.format("Showing %d expired batches (RESTRICTED FROM SALE)", list.size()));
                break;
            default: // ALL_FEFO
                list = inventoryService.getAllMedicines();
                Collections.sort(list); // FEFO Sort (Comparable)
                tableSummaryLabel.setText(String.format("Showing %d total batches sorted by FEFO (earliest expiry first)", list.size()));
                break;
        }

        tableModel.setRowCount(0);
        int rank = 1;
        for (Medicine m : list) {
            long days = m.getDaysUntilExpiry();
            String daysText;
            if (m.isExpired()) {
                daysText = Math.abs(days) + " days ago (Expired)";
            } else {
                daysText = days + " days left";
            }

            tableModel.addRow(new Object[]{
                    "#" + (rank++),
                    m.getId(),
                    m.getName(),
                    m.getBatchNumber(),
                    m.getQuantity() + " units",
                    m.getMinStockLevel() + " units",
                    String.format("$ %.2f", m.getPrice()),
                    String.format("$ %.2f", m.getTotalValue()),
                    DateUtil.formatDate(m.getExpiryDate()),
                    daysText,
                    m.getStatus()
            });
        }
    }

    private void restockSelectedMedicine() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow < 0) {
            UITheme.showWarning(this, "Please select a medicine row from the table to restock.");
            return;
        }

        String medId = (String) tableModel.getValueAt(selectedRow, 1);
        String medName = (String) tableModel.getValueAt(selectedRow, 2);

        String input = JOptionPane.showInputDialog(
                this,
                "Enter additional quantity units to add for:\n" + medName + " (ID: " + medId + ")",
                "Restock Medicine",
                JOptionPane.PLAIN_MESSAGE
        );

        if (input != null && !input.trim().isEmpty()) {
            try {
                int addUnits = Integer.parseInt(input.trim());
                if (addUnits <= 0) {
                    UITheme.showError(this, "Restock units must be greater than zero.");
                    return;
                }
                inventoryService.restockMedicine(medId, addUnits);
                UITheme.showSuccess(this, "Successfully added " + addUnits + " units to " + medName + "!");
                refreshInventory();
            } catch (NumberFormatException e) {
                UITheme.showError(this, "Please enter a valid numeric integer.");
            } catch (ValidationException e) {
                UITheme.showError(this, e.getMessage());
            }
        }
    }
}

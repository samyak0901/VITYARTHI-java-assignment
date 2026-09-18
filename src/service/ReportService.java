package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.Sale;
import model.SaleItem;
import util.DateUtil;

/**
 * Service providing sales history filtering, financial aggregations,
 * top-seller analytics, and reporting metrics.
 */
public class ReportService {
    private final BillingService billingService;

    public ReportService(BillingService billingService) {
        this.billingService = billingService;
    }

    public double getTotalRevenue() {
        return billingService.getSalesHistory().stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();
    }

    public int getTotalTransactions() {
        return billingService.getSalesHistory().size();
    }

    public double getTodayRevenue() {
        LocalDate today = LocalDate.now();
        return billingService.getSalesHistory().stream()
                .filter(s -> s.getDateTime() != null && s.getDateTime().toLocalDate().isEqual(today))
                .mapToDouble(Sale::getTotalAmount)
                .sum();
    }

    public int getTodayTransactionsCount() {
        LocalDate today = LocalDate.now();
        return (int) billingService.getSalesHistory().stream()
                .filter(s -> s.getDateTime() != null && s.getDateTime().toLocalDate().isEqual(today))
                .count();
    }

    public double getAverageTransactionValue() {
        int count = getTotalTransactions();
        return count == 0 ? 0.0 : getTotalRevenue() / count;
    }

    /**
     * Filters sales transactions based on timeframe.
     * "All Time", "Today", "Last 7 Days", "This Month"
     */
    public List<Sale> filterSales(String filter) {
        List<Sale> all = billingService.getSalesHistory();
        if (filter == null || filter.equalsIgnoreCase("All Time")) {
            return all;
        }

        LocalDate today = LocalDate.now();
        if (filter.equalsIgnoreCase("Today")) {
            return all.stream()
                    .filter(s -> s.getDateTime() != null && s.getDateTime().toLocalDate().isEqual(today))
                    .collect(Collectors.toList());
        } else if (filter.equalsIgnoreCase("Last 7 Days")) {
            LocalDate weekAgo = today.minusDays(7);
            return all.stream()
                    .filter(s -> s.getDateTime() != null && !s.getDateTime().toLocalDate().isBefore(weekAgo))
                    .collect(Collectors.toList());
        } else if (filter.equalsIgnoreCase("This Month")) {
            return all.stream()
                    .filter(s -> s.getDateTime() != null
                            && s.getDateTime().getYear() == today.getYear()
                            && s.getDateTime().getMonth() == today.getMonth())
                    .collect(Collectors.toList());
        }
        return all;
    }

    /**
     * Aggregates and ranks medicines by total units sold.
     */
    public static class BestSellerRecord {
        private final String medicineName;
        private int totalUnitsSold;
        private double totalRevenue;

        public BestSellerRecord(String medicineName, int totalUnitsSold, double totalRevenue) {
            this.medicineName = medicineName;
            this.totalUnitsSold = totalUnitsSold;
            this.totalRevenue = totalRevenue;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public int getTotalUnitsSold() {
            return totalUnitsSold;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }

        public void addSale(int qty, double revenue) {
            this.totalUnitsSold += qty;
            this.totalRevenue += revenue;
        }
    }

    public List<BestSellerRecord> getBestSellingMedicines() {
        Map<String, BestSellerRecord> map = new HashMap<>();

        for (Sale sale : billingService.getSalesHistory()) {
            for (SaleItem item : sale.getItems()) {
                String name = item.getMedicineName();
                BestSellerRecord rec = map.get(name);
                if (rec == null) {
                    rec = new BestSellerRecord(name, item.getQuantity(), item.getSubtotal());
                    map.put(name, rec);
                } else {
                    rec.addSale(item.getQuantity(), item.getSubtotal());
                }
            }
        }

        List<BestSellerRecord> list = new ArrayList<>(map.values());
        list.sort((a, b) -> Integer.compare(b.getTotalUnitsSold(), a.getTotalUnitsSold()));
        return list;
    }

    /**
     * Formats an executive sales report text for viewing or export.
     */
    public String generateSummaryReportText() {
        StringBuilder sb = new StringBuilder();
        sb.append("===============================================================\n");
        sb.append("                 MEDICARE PHARMACY SALES REPORT                \n");
        sb.append("           Generated on: ").append(DateUtil.formatDateTime(java.time.LocalDateTime.now())).append("\n");
        sb.append("===============================================================\n\n");
        sb.append("FINANCIAL OVERVIEW:\n");
        sb.append("---------------------------------------------------------------\n");
        sb.append(String.format("  Total Lifetime Revenue  : $ %.2f\n", getTotalRevenue()));
        sb.append(String.format("  Total Transactions      : %d\n", getTotalTransactions()));
        sb.append(String.format("  Today's Revenue         : $ %.2f\n", getTodayRevenue()));
        sb.append(String.format("  Today's Transactions    : %d\n", getTodayTransactionsCount()));
        sb.append(String.format("  Average Order Value     : $ %.2f\n", getAverageTransactionValue()));
        sb.append("\n");
        sb.append("TOP 5 BEST-SELLING MEDICINES:\n");
        sb.append("---------------------------------------------------------------\n");
        sb.append(String.format("  %-28s %-12s %-15s\n", "Medicine Name", "Units Sold", "Total Revenue"));
        sb.append("  -------------------------------------------------------------\n");

        List<BestSellerRecord> top = getBestSellingMedicines();
        int limit = Math.min(5, top.size());
        if (limit == 0) {
            sb.append("  No sales data recorded yet.\n");
        } else {
            for (int i = 0; i < limit; i++) {
                BestSellerRecord r = top.get(i);
                sb.append(String.format("  %-28s %-12d $ %12.2f\n", r.getMedicineName(), r.getTotalUnitsSold(), r.getTotalRevenue()));
            }
        }
        sb.append("\n===============================================================\n");
        return sb.toString();
    }
}

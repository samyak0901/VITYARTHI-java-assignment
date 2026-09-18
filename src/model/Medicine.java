package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import util.DateUtil;

/**
 * Represents a medicine product in the pharmacy inventory.
 * Inherits from Item and implements Comparable to support FEFO (First Expiry, First Out).
 */
public class Medicine extends Item implements Comparable<Medicine> {
    private String category;
    private String manufacturer;
    private String batchNumber;
    private LocalDate expiryDate;
    private int minStockLevel;

    /**
     * Default constructor
     */
    public Medicine() {
        super();
        this.minStockLevel = 10;
    }

    /**
     * Parameterized constructor demonstrating constructor chaining with super()
     */
    public Medicine(String id, String name, String category, String manufacturer,
                    String batchNumber, double price, int quantity,
                    LocalDate expiryDate, int minStockLevel) {
        super(id, name, price, quantity);
        this.category = category;
        this.manufacturer = manufacturer;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.minStockLevel = Math.max(0, minStockLevel);
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = Math.max(0, minStockLevel);
    }

    // Business Logic & Health Check Methods

    /**
     * Checks if the medicine is already past its expiration date.
     */
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Checks if the medicine is expiring within the specified number of days (default 30 days).
     */
    public boolean isExpiringSoon(int withinDays) {
        if (expiryDate == null || isExpired()) return false;
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, expiryDate);
        return daysRemaining >= 0 && daysRemaining <= withinDays;
    }

    public boolean isExpiringSoon() {
        return isExpiringSoon(30);
    }

    /**
     * Checks if available quantity is less than or equal to minimum threshold.
     */
    public boolean isLowStock() {
        return getQuantity() <= minStockLevel;
    }

    /**
     * Calculates remaining shelf life in days. Negative if expired.
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Evaluates comprehensive stock status based on inventory levels and expiry dates.
     */
    public StockStatus getStatus() {
        if (isExpired()) {
            return StockStatus.EXPIRED;
        }
        if (isLowStock()) {
            return StockStatus.LOW_STOCK;
        }
        if (isExpiringSoon(30)) {
            return StockStatus.EXPIRING_SOON;
        }
        return StockStatus.IN_STOCK;
    }

    @Override
    public String getDisplayText() {
        return String.format("%s (%s) - Batch %s - Exp: %s",
                getName(), getCategory(), getBatchNumber(), DateUtil.formatDate(expiryDate));
    }

    /**
     * Implements FEFO (First Expiry, First Out) ordering.
     * Batches that expire sooner appear first. If expiry dates are equal, sort by ID.
     */
    @Override
    public int compareTo(Medicine other) {
        if (other == null) return 1;
        if (this.expiryDate == null && other.expiryDate == null) {
            return this.getId().compareToIgnoreCase(other.getId());
        }
        if (this.expiryDate == null) return 1;
        if (other.expiryDate == null) return -1;

        int dateComparison = this.expiryDate.compareTo(other.expiryDate);
        if (dateComparison != 0) {
            return dateComparison;
        }
        return this.getId().compareToIgnoreCase(other.getId());
    }

    /**
     * Converts medicine object to CSV line.
     */
    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%s,%.2f,%d,%s,%d",
                escapeCsv(getId()),
                escapeCsv(getName()),
                escapeCsv(getCategory()),
                escapeCsv(getManufacturer()),
                escapeCsv(getBatchNumber()),
                getPrice(),
                getQuantity(),
                DateUtil.formatDateIso(getExpiryDate()),
                getMinStockLevel()
        );
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    @Override
    public String toString() {
        return String.format("Medicine{id='%s', name='%s', cat='%s', batch='%s', price=%.2f, qty=%d, exp=%s}",
                getId(), getName(), category, batchNumber, getPrice(), getQuantity(), expiryDate);
    }
}

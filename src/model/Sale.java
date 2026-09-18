package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import util.DateUtil;

/**
 * Represents a complete sales invoice transaction.
 */
public class Sale {
    private String billNumber;
    private LocalDateTime dateTime;
    private String customerName;
    private String customerContact;
    private List<SaleItem> items;
    private double subtotal;
    private double discountAmount;
    private double taxAmount;
    private double totalAmount;
    private String paymentMethod;

    public Sale() {
        this.items = new ArrayList<>();
        this.dateTime = LocalDateTime.now();
        this.customerName = "Walk-in Customer";
        this.customerContact = "N/A";
        this.paymentMethod = "Cash";
    }

    public Sale(String billNumber, LocalDateTime dateTime, String customerName, String customerContact, String paymentMethod) {
        this();
        this.billNumber = billNumber;
        this.dateTime = dateTime != null ? dateTime : LocalDateTime.now();
        this.customerName = (customerName != null && !customerName.trim().isEmpty()) ? customerName.trim() : "Walk-in Customer";
        this.customerContact = (customerContact != null && !customerContact.trim().isEmpty()) ? customerContact.trim() : "N/A";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Cash";
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public List<SaleItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(SaleItem item) {
        if (item != null) {
            this.items.add(item);
            calculateTotals();
        }
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            calculateTotals();
        }
    }

    public void clearItems() {
        this.items.clear();
        calculateTotals();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = Math.max(0, discountAmount);
        calculateTotals();
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = Math.max(0, taxAmount);
        calculateTotals();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public int getTotalQuantity() {
        int totalQty = 0;
        for (SaleItem item : items) {
            totalQty += item.getQuantity();
        }
        return totalQty;
    }

    public void calculateTotals() {
        this.subtotal = 0.0;
        for (SaleItem item : items) {
            this.subtotal += item.getSubtotal();
        }
        this.totalAmount = Math.max(0.0, this.subtotal - this.discountAmount + this.taxAmount);
    }

    /**
     * Generates a clean, professional pharmacy invoice text receipt.
     */
    public String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================================\n");
        sb.append("                       MEDICARE                          \n");
        sb.append("         Medical Shop Inventory Management System         \n");
        sb.append("          124 Health Avenue, Care City, MD 56001         \n");
        sb.append("                    Phone: +1 800-MEDICARE               \n");
        sb.append("=========================================================\n");
        sb.append(String.format(" Bill Number : %-25s\n", billNumber));
        sb.append(String.format(" Date & Time : %-25s\n", DateUtil.formatDateTime(dateTime)));
        sb.append(String.format(" Customer    : %-25s\n", customerName));
        sb.append(String.format(" Contact     : %-25s\n", customerContact));
        sb.append(String.format(" Pay Mode    : %-25s\n", paymentMethod));
        sb.append("---------------------------------------------------------\n");
        sb.append(String.format("%-20s %-8s %5s %9s %10s\n", "Medicine Name", "Batch", "Qty", "Price", "Subtotal"));
        sb.append("---------------------------------------------------------\n");

        for (SaleItem item : items) {
            String name = item.getMedicineName();
            if (name.length() > 20) {
                name = name.substring(0, 17) + "...";
            }
            sb.append(String.format("%-20s %-8s %5d %9.2f %10.2f\n",
                    name,
                    item.getBatchNumber(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
        }

        sb.append("---------------------------------------------------------\n");
        sb.append(String.format("%-36s %18.2f\n", "Subtotal:", subtotal));
        if (discountAmount > 0) {
            sb.append(String.format("%-36s %18.2f\n", "Discount:", -discountAmount));
        }
        if (taxAmount > 0) {
            sb.append(String.format("%-36s %18.2f\n", "Tax/GST:", taxAmount));
        }
        sb.append("=========================================================\n");
        sb.append(String.format(" TOTAL AMOUNT:                           $ %12.2f\n", totalAmount));
        sb.append("=========================================================\n");
        sb.append("                    Thank you for choosing               \n");
        sb.append("                           MEDICARE!                     \n");
        sb.append("       Medicines sold are non-refundable after 7 days.   \n");
        sb.append("                 Wishing you speedy recovery!            \n");
        sb.append("=========================================================\n");
        return sb.toString();
    }

    /**
     * Serializes items to a string for CSV persistence.
     * Item format: id;name;batch;price;qty | id;name;batch;price;qty
     */
    public String getItemsSerialized() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            SaleItem it = items.get(i);
            sb.append(it.getMedicineId()).append(";")
              .append(it.getMedicineName()).append(";")
              .append(it.getBatchNumber()).append(";")
              .append(it.getUnitPrice()).append(";")
              .append(it.getQuantity());
            if (i < items.size() - 1) {
                sb.append("|");
            }
        }
        return sb.toString();
    }

    /**
     * Converts sale record to CSV row.
     */
    public String toCsvRow() {
        return String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%s,\"%s\"",
                escapeCsv(billNumber),
                DateUtil.formatDateTimeIso(dateTime),
                escapeCsv(customerName),
                escapeCsv(customerContact),
                subtotal,
                discountAmount,
                taxAmount,
                totalAmount,
                escapeCsv(paymentMethod),
                getItemsSerialized().replace("\"", "\"\"")
        );
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }
}

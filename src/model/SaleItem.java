package model;

/**
 * Represents a line item within a sales transaction or cart.
 */
public class SaleItem {
    private String medicineId;
    private String medicineName;
    private String batchNumber;
    private double unitPrice;
    private int quantity;
    private double subtotal;

    public SaleItem() {
    }

    public SaleItem(String medicineId, String medicineName, String batchNumber, double unitPrice, int quantity) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.batchNumber = batchNumber;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice * quantity;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = this.unitPrice * this.quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = this.unitPrice * this.quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void recalculateSubtotal() {
        this.subtotal = this.unitPrice * this.quantity;
    }

    @Override
    public String toString() {
        return String.format("%s (Batch %s) x %d @ $%.2f = $%.2f",
                medicineName, batchNumber, quantity, unitPrice, subtotal);
    }
}

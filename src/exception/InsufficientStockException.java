package exception;

/**
 * Thrown when an operation attempts to allocate or sell more items than available in stock.
 */
public class InsufficientStockException extends MedicareException {
    private final String medicineId;
    private final String medicineName;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String medicineId, String medicineName, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for '%s' (ID: %s). Requested: %d, Available: %d",
                medicineName, medicineId, requestedQuantity, availableQuantity));
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}

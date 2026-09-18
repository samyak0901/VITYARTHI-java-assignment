package exception;

import java.time.LocalDate;

/**
 * Thrown when a pharmacist or transaction attempts to sell or dispense an expired medicine.
 */
public class ExpiredMedicineException extends MedicareException {
    private final String medicineId;
    private final String medicineName;
    private final LocalDate expiryDate;

    public ExpiredMedicineException(String medicineId, String medicineName, LocalDate expiryDate) {
        super(String.format("Cannot dispense expired medicine '%s' (ID: %s). Expiry Date: %s",
                medicineName, medicineId, expiryDate));
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.expiryDate = expiryDate;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}

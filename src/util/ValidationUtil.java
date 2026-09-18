package util;

import java.time.LocalDate;
import java.util.List;
import exception.ValidationException;
import model.Medicine;

/**
 * Utility class for sanitizing and validating business domain inputs.
 */
public final class ValidationUtil {

    private ValidationUtil() {
    }

    /**
     * Validates an entire medicine object before saving or updating.
     */
    public static void validateMedicine(Medicine med, boolean isNew, List<Medicine> existing) throws ValidationException {
        if (med == null) {
            throw new ValidationException("Medicine object cannot be null.");
        }

        // ID Validation
        if (med.getId() == null || med.getId().trim().isEmpty()) {
            throw new ValidationException("Medicine ID", "Medicine ID cannot be blank.");
        }
        String cleanId = med.getId().trim();
        if (cleanId.length() < 2) {
            throw new ValidationException("Medicine ID", "Medicine ID must be at least 2 characters.");
        }

        // Check unique ID
        if (isNew && existing != null) {
            for (Medicine other : existing) {
                if (other != null && cleanId.equalsIgnoreCase(other.getId().trim())) {
                    throw new ValidationException("Medicine ID", "A medicine with ID '" + cleanId + "' already exists.");
                }
            }
        }

        // Name Validation
        if (med.getName() == null || med.getName().trim().isEmpty()) {
            throw new ValidationException("Medicine Name", "Medicine name cannot be blank.");
        }
        if (med.getName().trim().length() < 2) {
            throw new ValidationException("Medicine Name", "Medicine name must be at least 2 characters long.");
        }

        // Category Validation
        if (med.getCategory() == null || med.getCategory().trim().isEmpty()) {
            throw new ValidationException("Category", "Category must be specified.");
        }

        // Manufacturer Validation
        if (med.getManufacturer() == null || med.getManufacturer().trim().isEmpty()) {
            throw new ValidationException("Manufacturer", "Manufacturer name cannot be blank.");
        }

        // Batch Number
        if (med.getBatchNumber() == null || med.getBatchNumber().trim().isEmpty()) {
            throw new ValidationException("Batch Number", "Batch number cannot be blank.");
        }

        // Price Validation
        if (med.getPrice() <= 0.0) {
            throw new ValidationException("Price", "Unit price must be a positive number greater than 0.");
        }

        // Quantity Validation
        if (med.getQuantity() < 0) {
            throw new ValidationException("Quantity", "Stock quantity cannot be negative.");
        }

        // Expiry Date Validation
        if (med.getExpiryDate() == null) {
            throw new ValidationException("Expiry Date", "Expiry date must be specified in YYYY-MM-DD format.");
        }

        // Minimum Stock Threshold
        if (med.getMinStockLevel() < 0) {
            throw new ValidationException("Minimum Stock Level", "Minimum stock level cannot be negative.");
        }
    }

    public static double parseDouble(String str, String fieldName) throws ValidationException {
        if (str == null || str.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required.");
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName, fieldName + " must be a valid numeric decimal value.");
        }
    }

    public static int parseInt(String str, String fieldName) throws ValidationException {
        if (str == null || str.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required.");
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName, fieldName + " must be a valid integer number.");
        }
    }

    public static LocalDate parseDate(String str, String fieldName) throws ValidationException {
        if (str == null || str.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " is required (YYYY-MM-DD).");
        }
        LocalDate d = DateUtil.parseDate(str);
        if (d == null) {
            throw new ValidationException(fieldName, "Invalid date format for " + fieldName + ". Please use YYYY-MM-DD (e.g. 2026-12-31).");
        }
        return d;
    }
}

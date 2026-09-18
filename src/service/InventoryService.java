package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import exception.InsufficientStockException;
import exception.ValidationException;
import model.Medicine;
import model.StockStatus;
import util.FileManager;
import util.ValidationUtil;

/**
 * Service managing medicines, stock levels, FEFO batch ordering, and persistence.
 * Implements observer callbacks so UI components update automatically on changes.
 */
public class InventoryService {
    private final List<Medicine> medicines;
    private final List<Runnable> changeListeners;

    public InventoryService() {
        this.medicines = new ArrayList<>();
        this.changeListeners = new ArrayList<>();
        loadData();
    }

    public synchronized void loadData() {
        medicines.clear();
        medicines.addAll(FileManager.loadMedicines());
        notifyListeners();
    }

    public synchronized boolean saveData() {
        return FileManager.saveMedicines(medicines);
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (Runnable listener : changeListeners) {
            try {
                listener.run();
            } catch (Exception e) {
                System.err.println("Error in change listener: " + e.getMessage());
            }
        }
    }

    public synchronized List<Medicine> getAllMedicines() {
        return new ArrayList<>(medicines);
    }

    public synchronized Medicine findById(String id) {
        if (id == null) return null;
        for (Medicine med : medicines) {
            if (id.equalsIgnoreCase(med.getId().trim())) {
                return med;
            }
        }
        return null;
    }

    /**
     * Adds a new medicine, performing validation and checking ID uniqueness.
     */
    public synchronized void addMedicine(Medicine med) throws ValidationException {
        ValidationUtil.validateMedicine(med, true, medicines);
        medicines.add(med);
        saveData();
        notifyListeners();
    }

    /**
     * Updates an existing medicine.
     */
    public synchronized void updateMedicine(Medicine updated) throws ValidationException {
        if (updated == null) {
            throw new ValidationException("Medicine cannot be null.");
        }
        Medicine existing = findById(updated.getId());
        if (existing == null) {
            throw new ValidationException("Medicine not found with ID: " + updated.getId());
        }

        // Validate
        ValidationUtil.validateMedicine(updated, false, medicines);

        // Update fields
        existing.setName(updated.getName().trim());
        existing.setCategory(updated.getCategory().trim());
        existing.setManufacturer(updated.getManufacturer().trim());
        existing.setBatchNumber(updated.getBatchNumber().trim());
        existing.setPrice(updated.getPrice());
        existing.setQuantity(updated.getQuantity());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setMinStockLevel(updated.getMinStockLevel());

        saveData();
        notifyListeners();
    }

    /**
     * Deletes a medicine by ID.
     */
    public synchronized boolean deleteMedicine(String id) {
        Medicine med = findById(id);
        if (med != null) {
            medicines.remove(med);
            saveData();
            notifyListeners();
            return true;
        }
        return false;
    }

    /**
     * Quick restock functionality: increases quantity for a medicine.
     */
    public synchronized void restockMedicine(String id, int additionalQty) throws ValidationException {
        if (additionalQty <= 0) {
            throw new ValidationException("Additional quantity must be greater than zero.");
        }
        Medicine med = findById(id);
        if (med == null) {
            throw new ValidationException("Medicine not found with ID: " + id);
        }
        med.setQuantity(med.getQuantity() + additionalQty);
        saveData();
        notifyListeners();
    }

    /**
     * Deducts stock upon sale. Throws InsufficientStockException if requested > available.
     */
    public synchronized void deductStock(String id, int quantity) throws InsufficientStockException {
        Medicine med = findById(id);
        if (med == null) {
            throw new InsufficientStockException(id, "Unknown Item", quantity, 0);
        }
        if (med.getQuantity() < quantity) {
            throw new InsufficientStockException(med.getId(), med.getName(), quantity, med.getQuantity());
        }
        med.setQuantity(med.getQuantity() - quantity);
        saveData();
        notifyListeners();
    }

    // FEFO (First Expiry, First Out) Logic
    /**
     * Returns batches of a specific medicine name sorted by earliest expiry date.
     * Demonstrates FEFO inventory management.
     */
    public synchronized List<Medicine> getFefoBatches(String medicineName) {
        if (medicineName == null) return Collections.emptyList();
        List<Medicine> batches = medicines.stream()
                .filter(m -> m.getName().equalsIgnoreCase(medicineName.trim()))
                .sorted() // Uses Medicine's compareTo which implements FEFO
                .collect(Collectors.toList());
        return batches;
    }

    // Filtering & Queries
    public synchronized List<Medicine> getLowStockMedicines() {
        return medicines.stream()
                .filter(Medicine::isLowStock)
                .sorted(Comparator.comparingInt(Medicine::getQuantity))
                .collect(Collectors.toList());
    }

    public synchronized List<Medicine> getExpiringSoonMedicines(int days) {
        return medicines.stream()
                .filter(m -> m.isExpiringSoon(days))
                .sorted()
                .collect(Collectors.toList());
    }

    public synchronized List<Medicine> getExpiredMedicines() {
        return medicines.stream()
                .filter(Medicine::isExpired)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Multi-field search supporting ID, Name, Category, Manufacturer, with category and status filter.
     */
    public synchronized List<Medicine> search(String query, String categoryFilter, String statusFilter) {
        String q = query != null ? query.trim().toLowerCase() : "";

        return medicines.stream().filter(m -> {
            // Text search
            boolean matchQuery = q.isEmpty()
                    || m.getId().toLowerCase().contains(q)
                    || m.getName().toLowerCase().contains(q)
                    || m.getCategory().toLowerCase().contains(q)
                    || m.getManufacturer().toLowerCase().contains(q)
                    || m.getBatchNumber().toLowerCase().contains(q);

            if (!matchQuery) return false;

            // Category filter
            if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("All Categories") && !categoryFilter.trim().isEmpty()) {
                if (!m.getCategory().equalsIgnoreCase(categoryFilter.trim())) {
                    return false;
                }
            }

            // Status filter
            if (statusFilter != null && !statusFilter.equalsIgnoreCase("All Status") && !statusFilter.trim().isEmpty()) {
                StockStatus s = m.getStatus();
                if (!s.getLabel().equalsIgnoreCase(statusFilter.trim())) {
                    return false;
                }
            }

            return true;
        }).collect(Collectors.toList());
    }

    public synchronized List<String> getAllCategories() {
        List<String> cats = medicines.stream()
                .map(Medicine::getCategory)
                .filter(c -> c != null && !c.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        cats.add(0, "All Categories");
        return cats;
    }

    // Dashboard & Analytics Aggregations
    public synchronized int getTotalMedicinesCount() {
        return medicines.size();
    }

    public synchronized int getTotalStockUnits() {
        int total = 0;
        for (Medicine m : medicines) {
            total += m.getQuantity();
        }
        return total;
    }

    public synchronized double getTotalInventoryValue() {
        double total = 0.0;
        for (Medicine m : medicines) {
            total += m.getTotalValue();
        }
        return total;
    }

    public synchronized int getLowStockCount() {
        int count = 0;
        for (Medicine m : medicines) {
            if (m.isLowStock()) count++;
        }
        return count;
    }

    public synchronized int getExpiringSoonCount() {
        int count = 0;
        for (Medicine m : medicines) {
            if (m.isExpiringSoon(30)) count++;
        }
        return count;
    }

    public synchronized int getExpiredCount() {
        int count = 0;
        for (Medicine m : medicines) {
            if (m.isExpired()) count++;
        }
        return count;
    }
}

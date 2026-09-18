package service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import exception.ExpiredMedicineException;
import exception.InsufficientStockException;
import exception.ValidationException;
import model.Medicine;
import model.Sale;
import model.SaleItem;
import util.FileManager;

/**
 * Service managing Point of Sale (POS) operations, cart calculations,
 * stock reservation validation, invoice generation, and receipt printing.
 */
public class BillingService {
    private final InventoryService inventoryService;
    private final List<Sale> salesHistory;
    private final List<Runnable> saleListeners;
    private static final AtomicInteger BILL_COUNTER = new AtomicInteger(1001);

    public BillingService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.salesHistory = new ArrayList<>();
        this.saleListeners = new ArrayList<>();
        loadSalesHistory();
    }

    public synchronized void loadSalesHistory() {
        salesHistory.clear();
        salesHistory.addAll(FileManager.loadSales());
        // Initialize bill counter higher than any existing bill
        for (Sale s : salesHistory) {
            String bNo = s.getBillNumber();
            if (bNo != null && bNo.contains("-")) {
                try {
                    String lastPart = bNo.substring(bNo.lastIndexOf("-") + 1);
                    int num = Integer.parseInt(lastPart);
                    if (num >= BILL_COUNTER.get()) {
                        BILL_COUNTER.set(num + 1);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    public void addSaleListener(Runnable listener) {
        if (listener != null && !saleListeners.contains(listener)) {
            saleListeners.add(listener);
        }
    }

    private void notifySaleListeners() {
        for (Runnable l : saleListeners) {
            try {
                l.run();
            } catch (Exception e) {
                System.err.println("Error in sale listener: " + e.getMessage());
            }
        }
    }

    public synchronized List<Sale> getSalesHistory() {
        return new ArrayList<>(salesHistory);
    }

    /**
     * Generates a modern sequential invoice/bill number.
     * Format: BILL-YYYYMMDD-XXXX
     */
    public synchronized String generateNextBillNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("BILL-%s-%04d", datePart, BILL_COUNTER.getAndIncrement());
    }

    /**
     * Validates and creates a SaleItem to add to a cart.
     * Strictly verifies that the medicine is not expired and stock is sufficient.
     */
    public SaleItem prepareCartItem(String medicineId, int quantity, Sale currentCart)
            throws ExpiredMedicineException, InsufficientStockException, ValidationException {
        if (quantity <= 0) {
            throw new ValidationException("Quantity", "Quantity must be at least 1.");
        }

        Medicine med = inventoryService.findById(medicineId);
        if (med == null) {
            throw new ValidationException("Medicine", "Selected medicine was not found.");
        }

        // Check if expired
        if (med.isExpired()) {
            throw new ExpiredMedicineException(med.getId(), med.getName(), med.getExpiryDate());
        }

        // Check how many of this item are already in cart
        int inCartQty = 0;
        if (currentCart != null) {
            for (SaleItem it : currentCart.getItems()) {
                if (it.getMedicineId().equalsIgnoreCase(medicineId)) {
                    inCartQty += it.getQuantity();
                }
            }
        }

        int totalRequired = inCartQty + quantity;
        if (totalRequired > med.getQuantity()) {
            throw new InsufficientStockException(med.getId(), med.getName(), totalRequired, med.getQuantity());
        }

        return new SaleItem(med.getId(), med.getName(), med.getBatchNumber(), med.getPrice(), quantity);
    }

    /**
     * Completes checkout transaction:
     * 1. Validates all cart items and current available stock.
     * 2. Decrements inventory for each item.
     * 3. Saves sale record to sales.csv.
     * 4. Generates formatted receipt in receipts/.
     */
    public synchronized Sale checkout(Sale cart)
            throws ValidationException, InsufficientStockException, ExpiredMedicineException {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new ValidationException("Cart is empty. Please add medicines before checkout.");
        }

        // Re-verify stock & expiration right before finalizing
        for (SaleItem item : cart.getItems()) {
            Medicine med = inventoryService.findById(item.getMedicineId());
            if (med == null) {
                throw new ValidationException("Item '" + item.getMedicineName() + "' is no longer available.");
            }
            if (med.isExpired()) {
                throw new ExpiredMedicineException(med.getId(), med.getName(), med.getExpiryDate());
            }
            if (med.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(med.getId(), med.getName(), item.getQuantity(), med.getQuantity());
            }
        }

        // Ensure bill number and timestamp are assigned
        if (cart.getBillNumber() == null || cart.getBillNumber().trim().isEmpty()) {
            cart.setBillNumber(generateNextBillNumber());
        }
        cart.setDateTime(LocalDateTime.now());
        cart.calculateTotals();

        // Deduct inventory
        for (SaleItem item : cart.getItems()) {
            inventoryService.deductStock(item.getMedicineId(), item.getQuantity());
        }

        // Record sale
        salesHistory.add(0, cart); // newest first
        FileManager.saveSales(salesHistory);

        // Generate and save receipt file
        FileManager.saveReceiptToFile(cart);

        notifySaleListeners();
        return cart;
    }
}

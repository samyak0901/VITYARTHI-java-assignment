

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import exception.ExpiredMedicineException;
import exception.InsufficientStockException;
import exception.ValidationException;
import model.Medicine;
import model.Sale;
import model.SaleItem;
import model.StockStatus;
import service.BillingService;
import service.InventoryService;
import service.ReportService;
import util.FileManager;

/**
 * Automated Headless Verification Suite for MEDICARE.
 * Validates all required college Java project requirements, OOP principles,
 * business logic, validation rules, FEFO sorting, and custom exceptions.
 */
public class TestRunner {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   MEDICARE SYSTEM AUTOMATED VERIFICATION SUITE   ");
        System.out.println("=================================================");

        testOOPAndModel();
        testValidationRules();
        testFefoLogic();
        testStockAndExpiryDetection();
        testBillingAndStockDeduction();
        testSafetyExceptions();
        testFilePersistenceAndReceipt();
        testReportService();

        System.out.println("\n-------------------------------------------------");
        System.out.printf("SUMMARY: %d Passed, %d Failed.\n", testsPassed, testsFailed);
        System.out.println("=================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " - Details: " + details);
            testsFailed++;
        }
    }

    private static void testOOPAndModel() {
        System.out.println("\n--- 1. Testing Classes, Objects, Inheritance & Polymorphism ---");
        LocalDate exp = LocalDate.now().plusMonths(6);
        Medicine med = new Medicine("TEST-01", "Amoxicillin Test", "Antibiotics", "Test Lab", "B-99", 50.0, 10, exp, 5);

        // Verify inheritance from Item
        assertTrue("Inheritance (Medicine is an Item)", med instanceof model.Item, "Medicine should extend Item");
        assertTrue("Polymorphic getTotalValue() calculation", med.getTotalValue() == 500.0, "Total value should be 50.0 * 10 = 500.0");
        assertTrue("Polymorphic getDisplayText() not empty", med.getDisplayText() != null && !med.getDisplayText().isEmpty(), "DisplayText expected");
        assertTrue("Encapsulation getters", med.getName().equals("Amoxicillin Test") && med.getBatchNumber().equals("B-99"), "Getters should match");
    }

    private static void testValidationRules() {
        System.out.println("\n--- 2. Testing Input Validation Rules ---");
        InventoryService inv = new InventoryService();

        // Test Empty Name
        try {
            Medicine invalidMed = new Medicine("INV-01", "", "General", "Lab", "B1", 10.0, 5, LocalDate.now().plusMonths(2), 2);
            inv.addMedicine(invalidMed);
            assertTrue("Reject empty name", false, "Should throw ValidationException for empty name");
        } catch (ValidationException e) {
            assertTrue("Reject empty name", true, "");
        }

        // Test Negative Price
        try {
            Medicine invalidPrice = new Medicine("INV-02", "Negative Price Med", "General", "Lab", "B1", -5.0, 5, LocalDate.now().plusMonths(2), 2);
            inv.addMedicine(invalidPrice);
            assertTrue("Reject negative price", false, "Should throw ValidationException for negative price");
        } catch (ValidationException e) {
            assertTrue("Reject negative price", true, "");
        }

        // Test Duplicate ID
        try {
            Medicine m1 = new Medicine("DUP-01", "Med One", "General", "Lab", "B1", 10.0, 5, LocalDate.now().plusMonths(2), 2);
            inv.addMedicine(m1);
            Medicine m2 = new Medicine("DUP-01", "Med Duplicate", "General", "Lab", "B2", 15.0, 5, LocalDate.now().plusMonths(2), 2);
            inv.addMedicine(m2);
            assertTrue("Reject duplicate ID", false, "Should throw ValidationException for duplicate ID");
        } catch (ValidationException e) {
            assertTrue("Reject duplicate ID", true, "");
        }
    }

    private static void testFefoLogic() {
        System.out.println("\n--- 3. Testing FEFO (First Expiry, First Out) Logic ---");
        InventoryService inv = new InventoryService();

        // Add 2 batches of same drug with different expiry
        LocalDate today = LocalDate.now();
        try {
            inv.addMedicine(new Medicine("FEFO-01", "TestFefoDrug", "Pain", "Lab", "Batch-Far", 20.0, 30, today.plusMonths(12), 5));
            inv.addMedicine(new Medicine("FEFO-02", "TestFefoDrug", "Pain", "Lab", "Batch-Soon", 20.0, 15, today.plusMonths(2), 5));
        } catch (ValidationException ignored) {
        }

        List<Medicine> batches = inv.getFefoBatches("TestFefoDrug");
        boolean isFefoOrdered = batches.size() >= 2 && batches.get(0).getBatchNumber().equals("Batch-Soon");
        assertTrue("FEFO Batch Prioritization", isFefoOrdered, "Batch-Soon should precede Batch-Far in FEFO query");
    }

    private static void testStockAndExpiryDetection() {
        System.out.println("\n--- 4. Testing Stock & Expiry Status Detection ---");
        LocalDate today = LocalDate.now();

        Medicine normal = new Medicine("S-01", "Normal Med", "Cat", "Mfg", "B1", 10.0, 50, today.plusMonths(10), 10);
        Medicine low = new Medicine("S-02", "Low Stock Med", "Cat", "Mfg", "B2", 10.0, 5, today.plusMonths(10), 10);
        Medicine expiring = new Medicine("S-03", "Expiring Med", "Cat", "Mfg", "B3", 10.0, 20, today.plusDays(15), 5);
        Medicine expired = new Medicine("S-04", "Expired Med", "Cat", "Mfg", "B4", 10.0, 20, today.minusDays(5), 5);

        assertTrue("Detect Normal In Stock", normal.getStatus() == StockStatus.IN_STOCK, "Should be IN_STOCK");
        assertTrue("Detect Low Stock", low.isLowStock() && low.getStatus() == StockStatus.LOW_STOCK, "Should be LOW_STOCK");
        assertTrue("Detect Expiring Soon (<30d)", expiring.isExpiringSoon(30) && expiring.getStatus() == StockStatus.EXPIRING_SOON, "Should be EXPIRING_SOON");
        assertTrue("Detect Expired", expired.isExpired() && expired.getStatus() == StockStatus.EXPIRED, "Should be EXPIRED");
    }

    private static void testBillingAndStockDeduction() {
        System.out.println("\n--- 5. Testing Sales, Billing, and Stock Reduction ---");
        InventoryService inv = new InventoryService();
        BillingService billing = new BillingService(inv);

        // Add a test medicine with known initial stock
        String medId = "BILL-TEST-99";
        try {
            inv.addMedicine(new Medicine(medId, "Checkout Test Med", "General", "TestMfg", "B-CHECK", 25.0, 50, LocalDate.now().plusMonths(8), 10));
        } catch (ValidationException ignored) {
        }

        int initialQty = inv.findById(medId).getQuantity();

        // Create Cart and Checkout 5 units
        Sale cart = new Sale();
        try {
            SaleItem item = billing.prepareCartItem(medId, 5, cart);
            cart.addItem(item);
            assertTrue("Cart subtotal check", cart.getSubtotal() == 125.0, "5 * 25.0 = 125.0");

            Sale completed = billing.checkout(cart);
            assertTrue("Checkout returned completed sale", completed != null, "Sale should not be null");

            int finalQty = inv.findById(medId).getQuantity();
            assertTrue("Stock automatically reduced in inventory", finalQty == (initialQty - 5),
                    "Stock should reduce by 5: expected " + (initialQty - 5) + ", actual " + finalQty);
        } catch (Exception e) {
            assertTrue("Checkout completed without unexpected exception", false, e.getMessage());
        }
    }

    private static void testSafetyExceptions() {
        System.out.println("\n--- 6. Testing Safety Exceptions (Stock Limit & Expiry Restrictions) ---");
        InventoryService inv = new InventoryService();
        BillingService billing = new BillingService(inv);

        LocalDate today = LocalDate.now();
        String expiredId = "EXP-TEST-01";
        String lowStockId = "LOW-TEST-01";

        try {
            inv.addMedicine(new Medicine(expiredId, "Dangerous Expired Med", "General", "Mfg", "B-EXP", 10.0, 10, today.minusDays(10), 2));
            inv.addMedicine(new Medicine(lowStockId, "Only Three In Stock", "General", "Mfg", "B-LOW", 10.0, 3, today.plusMonths(5), 1));
        } catch (ValidationException ignored) {
        }

        // 1. Attempt to add expired medicine to cart -> Must throw ExpiredMedicineException
        Sale cart = new Sale();
        try {
            billing.prepareCartItem(expiredId, 1, cart);
            assertTrue("Block selling expired medicine", false, "Should throw ExpiredMedicineException");
        } catch (ExpiredMedicineException e) {
            assertTrue("Block selling expired medicine", true, "");
        } catch (Exception e) {
            assertTrue("Block selling expired medicine", false, "Wrong exception: " + e.getClass().getName());
        }

        // 2. Attempt to add more than available stock (request 10 when only 3 available) -> Must throw InsufficientStockException
        try {
            billing.prepareCartItem(lowStockId, 10, cart);
            assertTrue("Block overselling stock", false, "Should throw InsufficientStockException");
        } catch (InsufficientStockException e) {
            assertTrue("Block overselling stock", true, "");
        } catch (Exception e) {
            assertTrue("Block overselling stock", false, "Wrong exception: " + e.getClass().getName());
        }
    }

    private static void testFilePersistenceAndReceipt() {
        System.out.println("\n--- 7. Testing File Handling & Receipt Generation ---");
        // Verify data directory and files
        File dataDir = new File("data");
        File receiptsDir = new File("receipts");

        assertTrue("Data directory exists", dataDir.exists() && dataDir.isDirectory(), "data/ directory should exist");
        assertTrue("Receipts directory exists", receiptsDir.exists() && receiptsDir.isDirectory(), "receipts/ directory should exist");

        // Verify receipt generation
        Sale testSale = new Sale("BILL-TEST-FILE", java.time.LocalDateTime.now(), "Jane Doe", "555-0199", "Card");
        testSale.addItem(new SaleItem("M1", "Paracetamol", "B101", 10.0, 2));
        testSale.calculateTotals();

        String receiptPath = FileManager.saveReceiptToFile(testSale);
        File receiptFile = new File(receiptPath);
        assertTrue("Receipt .txt file generated", receiptFile.exists() && receiptFile.length() > 0,
                "Receipt file should exist and have content: " + receiptPath);
    }

    private static void testReportService() {
        System.out.println("\n--- 8. Testing Report Analytics ---");
        InventoryService inv = new InventoryService();
        BillingService billing = new BillingService(inv);
        ReportService reportService = new ReportService(billing);

        assertTrue("Total revenue metric accessible", reportService.getTotalRevenue() >= 0.0, "Revenue should be non-negative");
        assertTrue("Transactions count non-negative", reportService.getTotalTransactions() >= 0, "Transaction count should be >= 0");
        assertTrue("Summary report text formatted", reportService.generateSummaryReportText().contains("MEDICARE"),
                "Report text should contain MEDICARE header");
    }
}

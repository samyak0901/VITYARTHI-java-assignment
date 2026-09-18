package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Medicine;
import model.Sale;
import model.SaleItem;

/**
 * Handles all file-based persistent storage for medicines, sales transactions, and printable receipts.
 * Demonstrates robust Java File I/O, try-with-resources, and automated directory initialization.
 */
public final class FileManager {
    private static final String DATA_DIR = "data";
    private static final String RECEIPTS_DIR = "receipts";
    private static final String MEDICINES_FILE = DATA_DIR + File.separator + "medicines.csv";
    private static final String SALES_FILE = DATA_DIR + File.separator + "sales.csv";

    private static final String MEDICINES_HEADER = "id,name,category,manufacturer,batchNumber,price,quantity,expiryDate,minStockLevel";
    private static final String SALES_HEADER = "billNumber,dateTime,customerName,customerContact,subtotal,discount,tax,totalAmount,paymentMethod,items";

    private FileManager() {
    }

    /**
     * Initializes storage directories if they do not exist.
     */
    public static void initializeDirectories() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(RECEIPTS_DIR));
        } catch (IOException e) {
            System.err.println("Warning: Could not create directories: " + e.getMessage());
        }
    }

    /**
     * Loads all medicines from medicines.csv.
     * If the file is missing or empty, generates realistic seed data.
     */
    public static List<Medicine> loadMedicines() {
        initializeDirectories();
        List<Medicine> list = new ArrayList<>();
        File file = new File(MEDICINES_FILE);

        if (!file.exists() || file.length() == 0) {
            list = generateSeedMedicines();
            saveMedicines(list);
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Medicine med = parseMedicineCsvLine(line);
                if (med != null) {
                    list.add(med);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading medicines file: " + e.getMessage());
            list = generateSeedMedicines();
        }

        return list;
    }

    /**
     * Saves the full list of medicines to medicines.csv.
     */
    public static boolean saveMedicines(List<Medicine> medicines) {
        initializeDirectories();
        File file = new File(MEDICINES_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(MEDICINES_HEADER);
            writer.newLine();
            for (Medicine med : medicines) {
                if (med != null) {
                    writer.write(med.toCsvRow());
                    writer.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving medicines file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads sales records from sales.csv.
     */
    public static List<Sale> loadSales() {
        initializeDirectories();
        List<Sale> list = new ArrayList<>();
        File file = new File(SALES_FILE);

        if (!file.exists() || file.length() == 0) {
            return list;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Sale sale = parseSaleCsvLine(line);
                if (sale != null) {
                    list.add(sale);
                    File receiptFile = new File(RECEIPTS_DIR + File.separator + sale.getBillNumber() + ".txt");
                    if (!receiptFile.exists()) {
                        saveReceiptToFile(sale);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading sales file: " + e.getMessage());
        }

        return list;
    }

    /**
     * Appends or rewrites sales records to sales.csv.
     */
    public static boolean saveSales(List<Sale> sales) {
        initializeDirectories();
        File file = new File(SALES_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(SALES_HEADER);
            writer.newLine();
            for (Sale sale : sales) {
                if (sale != null) {
                    writer.write(sale.toCsvRow());
                    writer.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error saving sales file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves a sales invoice receipt as a .txt file inside receipts/.
     */
    public static String saveReceiptToFile(Sale sale) {
        if (sale == null) return null;
        initializeDirectories();
        String filename = RECEIPTS_DIR + File.separator + sale.getBillNumber() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(sale.generateReceiptText());
            return filename;
        } catch (IOException e) {
            System.err.println("Error writing receipt file: " + e.getMessage());
            return null;
        }
    }

    private static Medicine parseMedicineCsvLine(String line) {
        List<String> cols = parseCsvColumns(line);
        if (cols.size() < 9) return null;

        try {
            String id = cols.get(0);
            String name = cols.get(1);
            String category = cols.get(2);
            String manufacturer = cols.get(3);
            String batch = cols.get(4);
            double price = Double.parseDouble(cols.get(5));
            int quantity = Integer.parseInt(cols.get(6));
            LocalDate expiry = DateUtil.parseDate(cols.get(7));
            int minStock = Integer.parseInt(cols.get(8));

            return new Medicine(id, name, category, manufacturer, batch, price, quantity, expiry, minStock);
        } catch (Exception e) {
            System.err.println("Could not parse medicine row: " + line + " -> " + e.getMessage());
            return null;
        }
    }

    private static Sale parseSaleCsvLine(String line) {
        List<String> cols = parseCsvColumns(line);
        if (cols.size() < 10) return null;

        try {
            String billNumber = cols.get(0);
            LocalDateTime dt = DateUtil.parseDateTime(cols.get(1));
            String customerName = cols.get(2);
            String customerPhone = cols.get(3);
            double subtotal = Double.parseDouble(cols.get(4));
            double discount = Double.parseDouble(cols.get(5));
            double tax = Double.parseDouble(cols.get(6));
            double total = Double.parseDouble(cols.get(7));
            String paymentMethod = cols.get(8);
            String itemsSerialized = cols.get(9);

            Sale sale = new Sale(billNumber, dt, customerName, customerPhone, paymentMethod);
            sale.setDiscountAmount(discount);
            sale.setTaxAmount(tax);

            // Parse serialized items
            if (itemsSerialized != null && !itemsSerialized.trim().isEmpty()) {
                String[] itemTokens = itemsSerialized.split("\\|");
                for (String itStr : itemTokens) {
                    String[] parts = itStr.split(";");
                    if (parts.length >= 5) {
                        String mId = parts[0];
                        String mName = parts[1];
                        String bNum = parts[2];
                        double uPrice = Double.parseDouble(parts[3]);
                        int qty = Integer.parseInt(parts[4]);
                        sale.addItem(new SaleItem(mId, mName, bNum, uPrice, qty));
                    }
                }
            }
            return sale;
        } catch (Exception e) {
            System.err.println("Could not parse sale row: " + line + " -> " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper to safely parse CSV line handling quoted commas.
     */
    private static List<String> parseCsvColumns(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    /**
     * Seeds realistic medicine data with varied statuses (Normal, Low Stock, Expiring Soon, Expired, Multi-batch).
     */
    private static List<Medicine> generateSeedMedicines() {
        List<Medicine> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 1. Normal In-Stock Medicines
        list.add(new Medicine("MED-101", "Paracetamol 500mg", "Analgesic / Antipyretic", "GSK Pharma", "B101", 12.50, 150, today.plusMonths(14), 20));
        list.add(new Medicine("MED-102", "Azithromycin 500mg", "Antibiotics", "Pfizer Inc.", "AZ-442", 95.00, 60, today.plusMonths(18), 15));
        list.add(new Medicine("MED-103", "Cetirizine 10mg", "Antihistamine", "Cipla Labs", "CT-890", 25.00, 120, today.plusMonths(11), 25));
        list.add(new Medicine("MED-104", "Ibuprofen 400mg", "NSAID / Pain Relief", "Abbott Healthcare", "IB-312", 18.00, 80, today.plusMonths(16), 20));
        list.add(new Medicine("MED-105", "Amoxicillin 500mg", "Antibiotics", "Novartis", "AM-771", 65.00, 45, today.plusMonths(9), 15));
        list.add(new Medicine("MED-106", "Pantoprazole 40mg", "Antacid / Gastro", "Sun Pharma", "PN-204", 42.00, 90, today.plusMonths(15), 20));
        list.add(new Medicine("MED-107", "Vitamin D3 60k IU", "Supplements / Vitamins", "Mankind Pharma", "VD-553", 110.00, 50, today.plusMonths(20), 10));
        list.add(new Medicine("MED-108", "ORS Electrolyte Powder", "Rehydration", "FDC Ltd.", "ORS-99", 22.00, 140, today.plusMonths(24), 30));

        // 2. Multi-batch FEFO demo: Paracetamol older batch expiring soonest vs newer batch
        list.add(new Medicine("MED-109", "Paracetamol 500mg", "Analgesic / Antipyretic", "GSK Pharma", "B098", 12.00, 35, today.plusDays(18), 20)); // Expiring in 18 days!
        list.add(new Medicine("MED-110", "Cough Relief Syrup 100ml", "Respiratory / Syrup", "Dabur India", "CR-102", 75.00, 40, today.plusMonths(10), 15));

        // 3. Low Stock Items (quantity <= minStockLevel)
        list.add(new Medicine("MED-111", "Antacid Mint Gel 200ml", "Antacid / Digestive", "Reckitt Benckiser", "AG-004", 85.00, 4, today.plusMonths(12), 15));
        list.add(new Medicine("MED-112", "Insulin Glargine 100IU", "Diabetes", "Sanofi Aventis", "IN-700", 680.00, 3, today.plusMonths(8), 10));
        list.add(new Medicine("MED-113", "Atorvastatin 20mg", "Cardiovascular", "Dr. Reddy's", "AT-601", 145.00, 6, today.plusMonths(13), 15));

        // 4. Expiring Soon (< 30 days)
        list.add(new Medicine("MED-114", "Eye Drops Lubricant 10ml", "Ophthalmology", "Allergan", "ED-221", 130.00, 18, today.plusDays(12), 10));
        list.add(new Medicine("MED-115", "Metformin 500mg", "Diabetes", "Torrent Pharma", "MF-332", 32.00, 25, today.plusDays(22), 15));

        // 5. Already Expired Item (demonstrates guard against selling expired medicine)
        list.add(new Medicine("MED-116", "Multivitamin Daily Zinc", "Vitamins", "Bayer Corp.", "MV-119", 90.00, 12, today.minusDays(15), 10));

        return list;
    }
}

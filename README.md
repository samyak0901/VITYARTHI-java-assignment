# ⚕ MEDICARE — Medical Shop Inventory Management System

A production-grade, desktop Pharmacy Management & Point of Sale (POS) application built natively in **Java Swing**. Engineered for real-world pharmacy operations, **MEDICARE** incorporates modern UI/UX design, real-time inventory tracking, **FEFO (First Expiry, First Out)** batch prioritization, strict safety constraints preventing expired sales, automated invoice receipt generation, and business intelligence analytics.

---

## 📋 Table of Contents
1. [Problem Statement](#-problem-statement)
2. [Project Objective](#-project-objective)
3. [Key Features](#-key-features)
4. [Technologies Used](#-technologies-used)
5. [Java Programming Concepts Demonstrated](#-java-programming-concepts-demonstrated)
6. [Core Functional Modules](#-core-functional-modules)
7. [Default Login Credentials](#-default-login-credentials)
8. [Project Architecture & Directory Structure](#-project-architecture--directory-structure)
9. [How to Run in VS Code / CLI](#-how-to-run-in-vs-code--cli)
10. [Automated Verification & Testing](#-automated-verification--testing)
11. [Future Enhancements](#-future-enhancements)

---

## 🎯 Problem Statement

Traditional retail pharmacies face significant logistical and safety challenges:
- Accidental dispensing of expired medicines posing severe patient health risks.
- Revenue loss due to stock obsolescence when older batches sit behind newer batches.
- Inaccurate stock counts resulting in stockouts during medical emergencies.
- Slow manual billing, billing errors, and lack of automated printable receipts.
- Absence of real-time sales reporting and top-seller visibility.

**MEDICARE** solves these issues with automated FEFO batch management, safety locks on expired goods, instant POS calculations, and automatic inventory decrements.

---

## 🚀 Project Objective

To build an educational yet enterprise-ready pharmacy management desktop system in pure Java demonstrating advanced Object-Oriented Programming (OOP) principles, clean multi-tier architecture, robust exception handling, and an elegant, modern desktop user experience without third-party framework bloat.

---

## ✨ Key Features

- 🔒 **Secure Authentication**: Clean modal sign-in screen with credential validation.
- 💊 **Complete Medicine Catalog CRUD**: Add, edit, delete, view, and dynamically search medicines across multiple fields (ID, Name, Category, Manufacturer, Batch).
- 🏷 **Visual Status Badging**: Pill-shaped status indicators (`IN STOCK`, `LOW STOCK`, `EXPIRING SOON`, `EXPIRED`).
- ⭐ **FEFO (First Expiry, First Out) Inventory Logic**: Automatically prioritizes and dispatches batches nearing expiration to minimize drug spoilage.
- 📦 **One-Click Re-stocking**: Instant inventory top-up with real-time stock recalculations.
- 🧾 **High-Speed POS Billing Terminal**:
  - Live medicine search and selection.
  - Multi-item cart with quantity adjusters.
  - Automatic subtotal, customizable discount, and tax calculations.
  - **Strict Safety Interlocks**: Hard block against selling expired items or overselling quantity.
  - Generates itemized `.txt` receipts saved to `receipts/` and displays an on-screen preview.
- 📜 **Historical Sales Ledger**: Complete searchable history of customer invoices with date range filters.
- 📈 **Business Intelligence Reports**: Live calculation of total revenue, transactions count, average ticket size, and ranking of top-selling pharmaceutical products.
- 🕒 **Live Dashboard Digital Clock**: Real-time header clock and reactive KPI summary cards.

---

## 🛠 Technologies Used

- **Language**: Java 17+ / Java 24
- **GUI Framework**: Java Swing & Java AWT (Pure native, zero external JAR dependencies)
- **Rendering Engine**: Custom `Graphics2D` with anti-aliasing (`KEY_ANTIALIASING`) and curved geometry
- **Persistence**: File I/O (`BufferedReader`, `BufferedWriter`, CSV tables in `data/`, formatted text receipts in `receipts/`)
- **IDE Compatibility**: Visual Studio Code, IntelliJ IDEA, Eclipse, NetBeans, or Command Prompt / PowerShell

---

## 🎓 Java Programming Concepts Demonstrated

This project serves as a comprehensive showcase of foundational and advanced Java programming concepts:

| Concept | Implementation in MEDICARE |
|---|---|
| **Classes & Objects** | Concrete real-world representations (`Medicine`, `Sale`, `SaleItem`, `StatCard`, `InventoryService`). |
| **Encapsulation** | Strict `private` fields with validated getters, setters, and internal state calculations. |
| **Inheritance** | `Medicine extends Item`; custom UI widgets (`RoundedPanel`, `StyledButton`, `SearchTextField`) extend Swing base components (`JPanel`, `JButton`, `JTextField`). |
| **Polymorphism** | Method overriding (`getTotalValue()`, `getDisplayText()`, `paintComponent()`); polymorphic interface usage (`Comparable<Medicine>`, `TableCellRenderer`, `ActionListener`). |
| **Collections Framework** | `List<Medicine>`, `ArrayList<SaleItem>`, `Map<String, BestSellerRecord>`, Streams, Lambda expressions, and `Comparator`. |
| **Exception Handling** | Custom checked domain exceptions (`InsufficientStockException`, `ExpiredMedicineException`, `ValidationException`), multi-catch blocks, and graceful recovery. |
| **File Handling** | CSV serialization/deserialization, automatic directory bootstrapping, try-with-resources stream management (`BufferedReader`, `BufferedWriter`). |
| **Date Handling** | Modern `java.time.LocalDate`, `LocalDateTime`, `DateTimeFormatter`, and `ChronoUnit` date calculations. |
| **Input Validation** | Defensive programming verifying non-blank strings, positive prices, non-negative stock counts, ISO date validity, and unique IDs. |

---

## 🧩 Core Functional Modules

### Module 1: Medicine Management (`MedicineManagementPanel`)
- Interactive data table with custom badge renderers.
- Multi-parameter live search (searches ID, Name, Category, and Manufacturer simultaneously).
- Add & Edit modal dialogs with rigorous validation (positive price, valid `YYYY-MM-DD` date, unique ID constraint).
- Safe deletion with confirmation modal.

### Module 2: Inventory & Expiry Management (`InventoryPanel`)
- Real-time stock valuation ($) and warehouse units count.
- **Segmented Filter Tabs**:
  - `All Batches (FEFO Sorted)`: Ranks batches with earliest expiry at the top.
  - `Low Stock Only`: Medicines where `quantity <= minStockLevel`.
  - `Expiring Soon (30d)`: Batches expiring within the next 30 days.
  - `Expired Medicines`: Quarantined batches blocked from dispensing.
- One-click restock tool to replenish stock on the fly.

### Module 3: Sales & POS Billing (`BillingPanel`)
- Split POS terminal layout:
  - **Left catalog**: Filterable drug table with live stock status and batch information.
  - **Right invoice**: Customer metadata, active cart items, subtotal, discount, tax, and grand total.
- **Safety Validations**:
  - Rejects selling more than available stock (`InsufficientStockException`).
  - Completely blocks selling expired drugs (`ExpiredMedicineException`).
- Automatic inventory stock deduction and state persistence upon checkout.
- Generates formatted `.txt` invoice receipts in `receipts/` directory.

### Module 4: Sales History & Reports (`SalesHistoryPanel` & `ReportsPanel`)
- Filter past sales by: *Today*, *Last 7 Days*, *This Month*, or *All Time*.
- Re-print or inspect itemized receipts for any completed order.
- Best-selling medicines leaderboard ranked by units sold.
- Financial health indicators: total sales revenue, average ticket size, and gross inventory asset value.

### Module 5: Dashboard & System Shell (`MainFrame` & `DashboardPanel`)
- Dark navigation sidebar with active tab indicator.
- Live digital clock in the header updated every second.
- 6 Key Performance Metric cards with visual accent bars.
- Critical Alert grid surfacing urgent inventory notices.

---

## 🔑 Default Login Credentials

| Role | Username | Password |
|---|---|---|
| **System Administrator / Chief Pharmacist** | `admin` | `admin123` |

---

## 📂 Project Architecture & Directory Structure

```
Medical Inventory/
├── .vscode/
│   ├── launch.json              # VS Code run/debug profiles
│   ├── settings.json            # Java source/output directories and Code Runner config
│   └── tasks.json               # Automated build & test tasks
├── data/
│   ├── medicines.csv            # Persistent medicine inventory records
│   └── sales.csv                # Persistent sales transaction records
├── receipts/                    # Formatted customer invoice text files (.txt)
│   ├── BILL-20260916-1001.txt
│   └── ...
├── src/
│   ├── Main.java                # Application entry point
│   ├── TestRunner.java          # Headless automated verification suite
│   ├── model/
│   │   ├── Item.java            # Abstract base class
│   │   ├── Medicine.java        # Concrete class extending Item
│   │   ├── Sale.java            # Sales invoice entity
│   │   ├── SaleItem.java        # Line item in an invoice
│   │   └── StockStatus.java     # Status enum (IN_STOCK, LOW_STOCK, etc.)
│   ├── exception/
│   │   ├── MedicareException.java
│   │   ├── InsufficientStockException.java
│   │   ├── ExpiredMedicineException.java
│   │   └── ValidationException.java
│   ├── service/
│   │   ├── InventoryService.java
│   │   ├── BillingService.java
│   │   └── ReportService.java
│   ├── util/
│   │   ├── DateUtil.java
│   │   ├── ValidationUtil.java
│   │   ├── FileManager.java
│   │   └── UITheme.java
│   └── ui/
│       ├── LoginFrame.java
│       ├── MainFrame.java
│       ├── components/
│       │   ├── RoundedPanel.java
│       │   ├── StyledButton.java
│       │   ├── StatCard.java
│       │   └── SearchTextField.java
│       └── panels/
│           ├── DashboardPanel.java
│           ├── MedicineManagementPanel.java
│           ├── InventoryPanel.java
│           ├── BillingPanel.java
│           ├── SalesHistoryPanel.java
│           └── ReportsPanel.java
├── run.bat                      # One-click Windows batch launcher
└── README.md                    # Project documentation
```

---

## 💻 How to Run in VS Code / CLI

### Method 1: In VS Code (Recommended)
1. Open this project folder in VS Code (`File` -> `Open Folder...`).
2. Open `src/Main.java`.
3. Click the **Run** button above `main()` or press **F5** (or click the Code Runner **Play** button).
4. Enter credentials: `admin` / `admin123`.

### Method 2: One-Click Windows Batch
Simply double-click `run.bat` in the project root directory.

### Method 3: Command Line (PowerShell or Command Prompt)
```bash
# Option A: From workspace root
javac -d bin -sourcepath src src/Main.java
java -cp bin Main

# Option B: From inside src folder
cd src
javac Main.java
java Main
```

---

## 🧪 Automated Verification & Testing

An automated headless verification test suite is included in `src/TestRunner.java`. It verifies all business logic, validation rules, FEFO sorting, and custom exceptions without opening GUI windows.

To execute the test suite:
```bash
# From workspace root:
javac -d bin -sourcepath src src/TestRunner.java
java -cp bin TestRunner

# Or from inside src:
cd src
javac TestRunner.java
java TestRunner
```

### Verification Output:
```
=================================================
   MEDICARE SYSTEM AUTOMATED VERIFICATION SUITE   
=================================================

--- 1. Testing Classes, Objects, Inheritance & Polymorphism ---
[PASS] Inheritance (Medicine is an Item)
[PASS] Polymorphic getTotalValue() calculation
[PASS] Polymorphic getDisplayText() not empty
[PASS] Encapsulation getters

--- 2. Testing Input Validation Rules ---
[PASS] Reject empty name
[PASS] Reject negative price
[PASS] Reject duplicate ID

--- 3. Testing FEFO (First Expiry, First Out) Logic ---
[PASS] FEFO Batch Prioritization

--- 4. Testing Stock & Expiry Status Detection ---
[PASS] Detect Normal In Stock
[PASS] Detect Low Stock
[PASS] Detect Expiring Soon (<30d)
[PASS] Detect Expired

--- 5. Testing Sales, Billing, and Stock Reduction ---
[PASS] Cart subtotal check
[PASS] Checkout returned completed sale
[PASS] Stock automatically reduced in inventory

--- 6. Testing Safety Exceptions (Stock Limit & Expiry Restrictions) ---
[PASS] Block selling expired medicine
[PASS] Block overselling stock

--- 7. Testing File Handling & Receipt Generation ---
[PASS] Data directory exists
[PASS] Receipts directory exists
[PASS] Receipt .txt file generated

--- 8. Testing Report Analytics ---
[PASS] Total revenue metric accessible
[PASS] Transactions count non-negative
[PASS] Summary report text formatted

-------------------------------------------------
SUMMARY: 23 Passed, 0 Failed.
=================================================
```

---

## 🔮 Future Enhancements

- 🖨 **Thermal Printer Direct Spooling**: Direct communication with POS thermal receipt printers via raw socket or ESC/POS commands.
- 📷 **Barcode / QR Scanner Integration**: USB barcode scanner listener for instant drug lookup by scanning medicine packaging.
- 👥 **Multi-User Role Management**: Role-based access control distinguishing between Junior Cashiers and Chief Pharmacists.
- ☁ **Cloud DB Sync**: Optional SQLite or MySQL backend bridge for centralized multi-branch chain pharmacy syncing.

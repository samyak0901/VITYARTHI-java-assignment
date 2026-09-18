

import javax.swing.SwingUtilities;
import service.BillingService;
import service.InventoryService;
import service.ReportService;
import ui.LoginFrame;
import util.FileManager;
import util.UITheme;

/**
 * Application Entry Point for MEDICARE — Medical Shop Inventory Management System.
 * Initializes storage, services, UI anti-aliasing rendering hints, and launches the login screen.
 */
public class Main {
    public static void main(String[] args) {
        // Configure font rendering and system look & feel
        UITheme.setupSystemSettings();

        // Initialize local file directories (data/ and receipts/)
        FileManager.initializeDirectories();

        // Launch UI safely on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize core domain services
                InventoryService inventoryService = new InventoryService();
                BillingService billingService = new BillingService(inventoryService);
                ReportService reportService = new ReportService(billingService);

                // Show Authentication Screen
                LoginFrame loginFrame = new LoginFrame(inventoryService, billingService, reportService);
                loginFrame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Fatal error initializing MEDICARE: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

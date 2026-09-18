package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import service.BillingService;
import service.InventoryService;
import service.ReportService;
import ui.components.RoundedPanel;
import ui.components.StyledButton;
import ui.panels.BillingPanel;
import ui.panels.DashboardPanel;
import ui.panels.InventoryPanel;
import ui.panels.MedicineManagementPanel;
import ui.panels.ReportsPanel;
import ui.panels.SalesHistoryPanel;
import util.UITheme;

/**
 * Main application window for MEDICARE.
 * Features an elegant dark sidebar, live digital clock, status pills,
 * and a smooth CardLayout container managing all functional modules.
 */
public class MainFrame extends JFrame {
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final ReportService reportService;

    // Card Layout Container
    private CardLayout cardLayout;
    private JPanel contentContainer;

    // Active Tab Tracking
    private String currentCard = "Dashboard";
    private final Map<String, StyledButton> navButtons = new HashMap<>();

    // Header Components
    private JLabel breadcrumbLabel;
    private JLabel clockLabel;

    public MainFrame(InventoryService inventoryService, BillingService billingService, ReportService reportService) {
        super("MEDICARE — Medical Shop Inventory Management System");
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.reportService = reportService;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1020, 680));
        setLocationRelativeTo(null);

        initUI();
        startClockTimer();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.COLOR_BG);

        // 1. LEFT SIDEBAR NAVIGATION
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. MAIN WORKSPACE (Header + Card Content)
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setOpaque(false);

        // Workspace Header
        JPanel headerBar = createHeaderBar();
        workspace.add(headerBar, BorderLayout.NORTH);

        // Workspace Content Cards
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setOpaque(false);

        // Register Panels
        DashboardPanel dashboardPanel = new DashboardPanel(inventoryService, billingService, reportService, this::navigateTo);
        MedicineManagementPanel medicinePanel = new MedicineManagementPanel(inventoryService);
        InventoryPanel inventoryPanel = new InventoryPanel(inventoryService);
        BillingPanel billingPanel = new BillingPanel(inventoryService, billingService);
        SalesHistoryPanel salesHistoryPanel = new SalesHistoryPanel(billingService, reportService);
        ReportsPanel reportsPanel = new ReportsPanel(billingService, reportService, inventoryService);

        contentContainer.add(dashboardPanel, "Dashboard");
        contentContainer.add(medicinePanel, "Medicines");
        contentContainer.add(inventoryPanel, "Inventory");
        contentContainer.add(billingPanel, "Billing");
        contentContainer.add(salesHistoryPanel, "Sales History");
        contentContainer.add(reportsPanel, "Reports");

        workspace.add(contentContainer, BorderLayout.CENTER);
        root.add(workspace, BorderLayout.CENTER);

        setContentPane(root);
        navigateTo("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UITheme.COLOR_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BorderLayout(0, 16));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));

        // Brand Banner at Top
        JPanel brandBox = new JPanel();
        brandBox.setOpaque(false);
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoRow.setOpaque(false);

        JLabel logoIcon = new JLabel("⚕");
        logoIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28));
        logoIcon.setForeground(UITheme.COLOR_PRIMARY_LIGHT);

        JLabel logoText = new JLabel("MEDICARE");
        logoText.setFont(UITheme.FONT_BRAND);
        logoText.setForeground(Color.WHITE);

        logoRow.add(logoIcon);
        logoRow.add(logoText);
        brandBox.add(logoRow);

        JLabel logoSub = new JLabel("  Pharmacy Management");
        logoSub.setFont(UITheme.FONT_CAPTION);
        logoSub.setForeground(new Color(148, 163, 184)); // Slate 400
        brandBox.add(logoSub);

        sidebar.add(brandBox, BorderLayout.NORTH);

        // Menu Buttons in Center
        JPanel navMenu = new JPanel();
        navMenu.setOpaque(false);
        navMenu.setLayout(new BoxLayout(navMenu, BoxLayout.Y_AXIS));

        addNavButton(navMenu, "Dashboard", "📊  Dashboard");
        addNavButton(navMenu, "Medicines", "💊  Medicines");
        addNavButton(navMenu, "Inventory", "📦  Inventory & FEFO");
        addNavButton(navMenu, "Billing", "🧾  Billing / POS");
        addNavButton(navMenu, "Sales History", "📜  Sales History");
        addNavButton(navMenu, "Reports", "📈  Reports");

        sidebar.add(navMenu, BorderLayout.CENTER);

        // Bottom User Profile & Logout
        JPanel bottomBox = new JPanel();
        bottomBox.setOpaque(false);
        bottomBox.setLayout(new BoxLayout(bottomBox, BoxLayout.Y_AXIS));

        RoundedPanel profileCard = new RoundedPanel(10, new Color(30, 41, 59), null, 0);
        profileCard.setLayout(new BorderLayout(8, 0));
        profileCard.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        profileCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JLabel userIcon = new JLabel("👨‍⚕️");
        userIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        profileCard.add(userIcon, BorderLayout.WEST);

        JPanel userText = new JPanel();
        userText.setOpaque(false);
        userText.setLayout(new BoxLayout(userText, BoxLayout.Y_AXIS));

        JLabel userName = new JLabel("Admin Pharmacist");
        userName.setFont(UITheme.FONT_BODY_BOLD);
        userName.setForeground(Color.WHITE);

        JLabel userStatus = new JLabel("● Online");
        userStatus.setFont(UITheme.FONT_CAPTION);
        userStatus.setForeground(UITheme.COLOR_SUCCESS);

        userText.add(userName);
        userText.add(userStatus);
        profileCard.add(userText, BorderLayout.CENTER);

        bottomBox.add(profileCard);
        bottomBox.add(Box.createVerticalStrut(10));

        StyledButton btnLogout = new StyledButton("🚪  Sign Out", StyledButton.ButtonType.DANGER);
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLogout.addActionListener(e -> performLogout());
        bottomBox.add(btnLogout);

        sidebar.add(bottomBox, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavButton(JPanel container, String cardName, String label) {
        StyledButton btn = new StyledButton(label, StyledButton.ButtonType.SIDEBAR);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        btn.addActionListener(e -> navigateTo(cardName));

        container.add(btn);
        container.add(Box.createVerticalStrut(6));
        navButtons.put(cardName, btn);
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(UITheme.COLOR_HEADER_BG);
        header.setPreferredSize(new Dimension(0, 56));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)
        ));

        // Breadcrumb Left
        breadcrumbLabel = new JLabel("Dashboard Overview");
        breadcrumbLabel.setFont(UITheme.FONT_SUBTITLE);
        breadcrumbLabel.setForeground(UITheme.COLOR_TEXT_MAIN);
        header.add(breadcrumbLabel, BorderLayout.WEST);

        // Right Info: FEFO Pill + Live Clock
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        // FEFO Active Pill
        RoundedPanel fefoPill = new RoundedPanel(8, new Color(236, 253, 245), UITheme.COLOR_SUCCESS, 1);
        fefoPill.setLayout(new BorderLayout());
        fefoPill.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        JLabel fefoText = new JLabel("FEFO Protocol: ACTIVE");
        fefoText.setFont(UITheme.FONT_CAPTION.deriveFont(Font.BOLD));
        fefoText.setForeground(UITheme.COLOR_SUCCESS);
        fefoPill.add(fefoText, BorderLayout.CENTER);
        rightHeader.add(fefoPill);

        // Clock Label
        clockLabel = new JLabel("00:00:00 AM");
        clockLabel.setFont(UITheme.FONT_BODY_BOLD);
        clockLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        rightHeader.add(clockLabel);

        header.add(rightHeader, BorderLayout.EAST);
        return header;
    }

    public void navigateTo(String cardName) {
        this.currentCard = cardName;
        cardLayout.show(contentContainer, cardName);

        // Update button active state
        for (Map.Entry<String, StyledButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equalsIgnoreCase(cardName));
        }

        // Update breadcrumb
        if (breadcrumbLabel != null) {
            breadcrumbLabel.setText("MEDICARE  /  " + cardName);
        }
    }

    private void startClockTimer() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  |  hh:mm:ss a");
        Timer timer = new Timer(1000, e -> {
            if (clockLabel != null) {
                clockLabel.setText(LocalDateTime.now().format(dtf));
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private void performLogout() {
        boolean confirm = UITheme.showConfirm(this, "Are you sure you want to sign out of MEDICARE?", "Confirm Logout");
        if (confirm) {
            this.dispose();
            LoginFrame loginFrame = new LoginFrame(inventoryService, billingService, reportService);
            loginFrame.setVisible(true);
        }
    }
}

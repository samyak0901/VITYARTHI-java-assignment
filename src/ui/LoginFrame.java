package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import service.BillingService;
import service.InventoryService;
import service.ReportService;
import ui.components.RoundedPanel;
import ui.components.StyledButton;
import util.UITheme;

/**
 * Authentication window with a clean medical design and card layout.
 * Default credentials: admin / admin123
 */
public class LoginFrame extends JFrame {
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final ReportService reportService;

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame(InventoryService inventoryService, BillingService billingService, ReportService reportService) {
        super("MEDICARE — Login");
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.reportService = reportService;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BG);

        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());

        RoundedPanel loginCard = new RoundedPanel(18, Color.WHITE, UITheme.COLOR_BORDER, 1);
        loginCard.setLayout(new BorderLayout(0, 20));
        loginCard.setPreferredSize(new Dimension(380, 480));
        loginCard.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        // Top Brand & Icon Header
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));

        JLabel logoIcon = new JLabel("⚕");
        logoIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 42));
        logoIcon.setForeground(UITheme.COLOR_PRIMARY);
        logoIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel brandTitle = new JLabel("MEDICARE");
        brandTitle.setFont(UITheme.FONT_BRAND);
        brandTitle.setForeground(UITheme.COLOR_TEXT_MAIN);
        brandTitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel brandSub = new JLabel("Medical Shop Inventory Management System");
        brandSub.setFont(UITheme.FONT_CAPTION);
        brandSub.setForeground(UITheme.COLOR_TEXT_MUTED);
        brandSub.setAlignmentX(CENTER_ALIGNMENT);

        brandPanel.add(logoIcon);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(brandTitle);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(brandSub);

        loginCard.add(brandPanel, BorderLayout.NORTH);

        // Center Inputs Form
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(UITheme.FONT_BODY_BOLD);
        userLbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        txtUsername = new JTextField("admin");
        txtUsername.setFont(UITheme.FONT_BODY);
        txtUsername.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UITheme.FONT_BODY_BOLD);
        passLbl.setForeground(UITheme.COLOR_TEXT_MAIN);

        txtPassword = new JPasswordField("admin123");
        txtPassword.setFont(UITheme.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        // Submit on Enter key in either field
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterKey);
        txtPassword.addKeyListener(enterKey);

        formPanel.add(userLbl);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(txtUsername);
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(passLbl);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(txtPassword);

        // Demo credentials hint pill
        formPanel.add(Box.createVerticalStrut(14));
        RoundedPanel hintPanel = new RoundedPanel(8, new Color(241, 245, 249), UITheme.COLOR_BORDER, 1);
        hintPanel.setLayout(new BorderLayout());
        hintPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        hintPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel hintText = new JLabel("Demo Login: admin / admin123", SwingConstants.CENTER);
        hintText.setFont(UITheme.FONT_CAPTION);
        hintText.setForeground(UITheme.COLOR_TEXT_MUTED);
        hintPanel.add(hintText, BorderLayout.CENTER);
        formPanel.add(hintPanel);

        loginCard.add(formPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        StyledButton btnLogin = new StyledButton("Sign In to Dashboard", StyledButton.ButtonType.PRIMARY);
        btnLogin.setFont(UITheme.FONT_SUBTITLE);
        btnLogin.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.addActionListener(e -> performLogin());

        bottomPanel.add(btnLogin);
        loginCard.add(bottomPanel, BorderLayout.SOUTH);

        add(loginCard);
    }

    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            UITheme.showWarning(this, "Please enter both username and password.");
            return;
        }

        // Validate educational demo credentials
        if ("admin".equals(username) && "admin123".equals(password)) {
            // Success -> Open Main Dashboard Frame
            MainFrame mainFrame = new MainFrame(inventoryService, billingService, reportService);
            mainFrame.setVisible(true);
            this.dispose();
        } else {
            UITheme.showError(this, "Invalid credentials!\nUsername or password is incorrect. Please try again.");
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }
}

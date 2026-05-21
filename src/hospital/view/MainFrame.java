package hospital.view;

import hospital.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * The application's top-level JFrame.
 * Hosts the sidebar navigation and the main content area using a CardLayout.
 */
public class MainFrame extends JFrame {

    private static final int W = 1200;
    private static final int H = 760;

    private JPanel contentArea;
    private CardLayout cardLayout;

    private JButton activeSidebarBtn = null;

    public MainFrame() {
        setTitle("Hospital Patient Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(W, H);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(),     BorderLayout.WEST);
        root.add(buildContentArea(), BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Sidebar ────────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UIHelper.PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(210, H));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // Branding
        JPanel brand = new JPanel(new BorderLayout());
        brand.setBackground(UIHelper.PRIMARY_DARK);
        brand.setBorder(new EmptyBorder(24, 20, 20, 20));
        JLabel appName = new JLabel("HospitalPMS");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        appName.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Patient Management");
        sub.setFont(UIHelper.FONT_SMALL);
        sub.setForeground(new Color(0xB0C4DE));
        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 2));
        brandText.setOpaque(false);
        brandText.add(appName);
        brandText.add(sub);
        brand.add(brandText, BorderLayout.CENTER);
        sidebar.add(brand);

        // Nav separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2B5F8A));
        sep.setMaximumSize(new Dimension(210, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));

        // Nav items
        activeSidebarBtn = addNavItem(sidebar, "Dashboard",    "dashboard",    true);
        addNavItem(sidebar, "Patients",      "patients",    false);
        addNavItem(sidebar, "Doctors",       "doctors",     false);
        addNavItem(sidebar, "Appointments",  "appointments",false);

        sidebar.add(Box.createVerticalGlue());

        // Footer
        JLabel footer = new JLabel("v1.0.0  –  Eclipse Project");
        footer.setFont(UIHelper.FONT_SMALL);
        footer.setForeground(new Color(0x7A9BBF));
        footer.setBorder(new EmptyBorder(12, 20, 16, 20));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        return sidebar;
    }

    private JButton addNavItem(JPanel sidebar, String label, String card, boolean active) {
        JButton btn = new JButton(label);
        btn.setFont(UIHelper.FONT_BODY);
        btn.setForeground(active ? Color.WHITE : new Color(0xB0C4DE));
        btn.setBackground(active ? UIHelper.PRIMARY : UIHelper.PRIMARY_DARK);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 24, 12, 24));
        btn.setMaximumSize(new Dimension(210, 48));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            cardLayout.show(contentArea, card);
            if (activeSidebarBtn != null) {
                activeSidebarBtn.setBackground(UIHelper.PRIMARY_DARK);
                activeSidebarBtn.setForeground(new Color(0xB0C4DE));
            }
            btn.setBackground(UIHelper.PRIMARY);
            btn.setForeground(Color.WHITE);
            activeSidebarBtn = btn;
        });

        sidebar.add(btn);
        return btn;
    }

    // ── Content area ───────────────────────────────────────────────────────────

    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIHelper.BG);

        contentArea.add(DashboardPanel.create(), "dashboard");
        contentArea.add(new PatientPanel(),       "patients");
        contentArea.add(new DoctorPanel(),        "doctors");
        contentArea.add(new AppointmentPanel(),   "appointments");

        return contentArea;
    }
}
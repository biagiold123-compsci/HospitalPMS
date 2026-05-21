package hospital.view;

import hospital.repository.DataRepository;
import hospital.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Landing dashboard showing high-level statistics.
 * Implements RepositoryListener to auto-refresh when data changes.
 */
public class DashboardPanel extends JPanel implements DataRepository.RepositoryListener {

    private final DataRepository repo = DataRepository.getInstance();

    private JLabel lblPatients;
    private JLabel lblDoctors;
    private JLabel lblAppointments;
    private JLabel lblScheduled;

    public DashboardPanel() {
        repo.addListener(this);
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout(0, 24));
        setBorder(new EmptyBorder(32, 32, 32, 32));

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildStatsRow(),   BorderLayout.CENTER);
        add(buildQuickHelp(),  BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = UIHelper.headerLabel("Dashboard");
        JLabel sub   = UIHelper.mutedLabel("Welcome to the Hospital Patient Management System");
        sub.setFont(sub.getFont().deriveFont(13f));
        p.add(title, BorderLayout.NORTH);
        p.add(sub,   BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        JLabel[] p = new JLabel[1];
        JLabel[] d = new JLabel[1];
        JLabel[] a = new JLabel[1];
        JLabel[] s = new JLabel[1];

        row.add(buildStatCard("Patients", "0", UIHelper.PRIMARY, p));
        row.add(buildStatCard("Doctors", "0", UIHelper.ACCENT, d));
        row.add(buildStatCard("Appointments", "0", new Color(0x6F42C1), a));
        row.add(buildStatCard("Scheduled", "0", UIHelper.WARN, s));

        lblPatients = p[0];
        lblDoctors = d[0];
        lblAppointments = a[0];
        lblScheduled = s[0];

        return row;
    }

    /** Returns a styled card with a big number and a caption. */
    private JPanel buildStatCard(String caption, String value, Color accent, JLabel[] holder) {

        JPanel card = UIHelper.cardPanel();
        card.setLayout(new BorderLayout(0, 4));

        JLabel numLbl = new JLabel(value, SwingConstants.CENTER);
        numLbl.setFont(new Font("Segoe UI", Font.BOLD, 42));
        numLbl.setForeground(accent);

        JLabel capLbl = new JLabel(caption, SwingConstants.CENTER);
        capLbl.setFont(UIHelper.FONT_BODY);
        capLbl.setForeground(UIHelper.TEXT_MUTED);

        JSeparator sep = new JSeparator();
        sep.setForeground(accent);
        sep.setBackground(accent);

        card.add(sep, BorderLayout.NORTH);
        card.add(numLbl, BorderLayout.CENTER);
        card.add(capLbl, BorderLayout.SOUTH);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
                new EmptyBorder(20, 16, 20, 16)
        ));

        holder[0] = numLbl;
        return card;
    }

    private JPanel buildQuickHelp() {
        JPanel p = UIHelper.cardPanel();
        p.setLayout(new GridLayout(1, 3, 16, 0));

        p.add(helpItem("Patients tab",     "Register, search, and edit patient records."));
        p.add(helpItem("Doctors tab",      "Manage physician profiles and availability."));
        p.add(helpItem("Appointments tab", "Schedule, cancel, or complete appointments."));
        return p;
    }

    private JPanel helpItem(String title, String desc) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(UIHelper.FONT_HEADER);
        t.setForeground(UIHelper.PRIMARY);
        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(UIHelper.FONT_SMALL);
        d.setForeground(UIHelper.TEXT_MUTED);
        p.add(t, BorderLayout.NORTH);
        p.add(d, BorderLayout.CENTER);
        return p;
    }

    private void refresh() {
        if (lblPatients == null) return;
        lblPatients.setText(String.valueOf(repo.getTotalPatients()));
        lblDoctors.setText(String.valueOf(repo.getTotalDoctors()));
        lblAppointments.setText(String.valueOf(repo.getTotalAppointments()));
        lblScheduled.setText(String.valueOf(repo.getScheduledAppointments()));
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(this::refresh);
    }

    /**
     * We need to override to properly build the stats row with card references.
     * This simplified version lays out four cards in a grid and holds label refs.
     */
    private JPanel buildStatsRowFixed() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        row.add(makeCard("Patients",     UIHelper.PRIMARY,          n -> lblPatients     = n));
        row.add(makeCard("Doctors",      UIHelper.ACCENT,           n -> lblDoctors      = n));
        row.add(makeCard("Appointments", new Color(0x6F42C1),       n -> lblAppointments = n));
        row.add(makeCard("Scheduled",    UIHelper.WARN,             n -> lblScheduled    = n));
        return row;
    }

    @FunctionalInterface
    private interface LabelSetter { void set(JLabel lbl); }

    private JPanel makeCard(String caption, Color accent, LabelSetter setter) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(UIHelper.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(4, 0, 0, 0, accent),
                new EmptyBorder(20, 16, 20, 16)));

        JLabel numLbl = new JLabel("0", SwingConstants.CENTER);
        numLbl.setFont(new Font("Segoe UI", Font.BOLD, 42));
        numLbl.setForeground(accent);

        JLabel capLbl = new JLabel(caption, SwingConstants.CENTER);
        capLbl.setFont(UIHelper.FONT_BODY);
        capLbl.setForeground(UIHelper.TEXT_MUTED);

        card.add(numLbl, BorderLayout.CENTER);
        card.add(capLbl, BorderLayout.SOUTH);

        setter.set(numLbl);
        return card;
    }

    // ── Called once during construction to build the panel correctly ──────────

    public static DashboardPanel create() {
        DashboardPanel dp = new DashboardPanel();

        dp.removeAll();
        dp.add(dp.buildHeader(),        BorderLayout.NORTH);
        dp.add(dp.buildStatsRowFixed(), BorderLayout.CENTER);
        dp.add(dp.buildQuickHelp(),     BorderLayout.SOUTH);
        dp.refresh();
        return dp;
    }
}
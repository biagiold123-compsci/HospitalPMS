package hospital.view;

import hospital.controller.DoctorController;
import hospital.exception.RecordNotFoundException;
import hospital.exception.ValidationException;
import hospital.model.Doctor;
import hospital.repository.DataRepository;
import hospital.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Swing panel for managing doctor profiles.
 */
public class DoctorPanel extends JPanel implements DataRepository.RepositoryListener {

    private final DoctorController controller = new DoctorController();

    private DefaultTableModel tableModel;
    private JTable table;

    private JTextField fldFirst, fldLast, fldLicense, fldPhone;
    private JComboBox<Doctor.Specialization> cmbSpec;
    private JCheckBox chkAvailable;

    private Doctor selected = null;

    public DoctorPanel() {
        DataRepository.getInstance().addListener(this);
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = UIHelper.headerLabel("Doctor Profiles");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTable(), buildForm());
        split.setDividerLocation(460);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        reload();
    }

    private JPanel buildTable() {
        JPanel p = UIHelper.cardPanel();
        p.setLayout(new BorderLayout(0, 8));

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Specialization", "Available"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(UIHelper.ACCENT);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelect();
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        JButton btnNew = UIHelper.successButton("+ New Doctor");
        JButton btnDel = UIHelper.dangerButton("Delete");
        btnNew.addActionListener(e -> clearForm());
        btnDel.addActionListener(e -> deleteSelected());
        btns.add(btnNew); btns.add(btnDel);

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildForm() {
        JPanel p = UIHelper.cardPanel();
        p.setLayout(new BorderLayout(0, 12));

        JLabel lbl = new JLabel("Doctor Details");
        lbl.setFont(UIHelper.FONT_HEADER);
        lbl.setForeground(UIHelper.ACCENT);
        p.add(lbl, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;

        fldFirst   = UIHelper.field(14);
        fldLast    = UIHelper.field(14);
        fldLicense = UIHelper.field(14);
        fldPhone   = UIHelper.field(14);
        cmbSpec    = new JComboBox<>(Doctor.Specialization.values());
        chkAvailable = new JCheckBox("Available for appointments", true);
        chkAvailable.setOpaque(false);

        row(form, g, 0, "First name *",     fldFirst);
        row(form, g, 1, "Last name *",      fldLast);
        row(form, g, 2, "Specialization",   cmbSpec);
        row(form, g, 3, "License no. *",    fldLicense);
        row(form, g, 4, "Phone *",          fldPhone);
        row(form, g, 5, "Status",           chkAvailable);

        p.add(new JScrollPane(form) {{ setBorder(null); }}, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        JButton btnCancel = UIHelper.neutralButton("Cancel");
        JButton btnSave   = UIHelper.primaryButton("Save");
        btnCancel.addActionListener(e -> clearForm());
        btnSave  .addActionListener(e -> saveForm());
        btns.add(btnCancel); btns.add(btnSave);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private void row(JPanel f, GridBagConstraints g, int r, String lbl, Component c) {
        g.gridx = 0; g.gridy = r; g.weightx = 0; f.add(new JLabel(lbl), g);
        g.gridx = 1; g.weightx = 1; f.add(c, g);
    }

    private void onSelect() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        try {
            selected = controller.getById((String) tableModel.getValueAt(r, 0));
            fldFirst  .setText(selected.getFirstName());
            fldLast   .setText(selected.getLastName());
            fldLicense.setText(selected.getLicenseNumber());
            fldPhone  .setText(selected.getPhoneNumber());
            cmbSpec   .setSelectedItem(selected.getSpecialization());
            chkAvailable.setSelected(selected.isAvailable());
        } catch (RecordNotFoundException e) {
            UIHelper.showError(this, e.getMessage());
        }
    }

    private void clearForm() {
        selected = null; table.clearSelection();
        fldFirst.setText(""); fldLast.setText("");
        fldLicense.setText(""); fldPhone.setText("");
        cmbSpec.setSelectedIndex(0); chkAvailable.setSelected(true);
    }

    private void saveForm() {
        try {
            if (selected == null) {
                controller.addDoctor(fldFirst.getText(), fldLast.getText(),
                        (Doctor.Specialization) cmbSpec.getSelectedItem(),
                        fldLicense.getText(), fldPhone.getText());
                UIHelper.showSuccess(this, "Doctor added successfully.");
            } else {
                controller.updateDoctor(selected,
                        fldFirst.getText(), fldLast.getText(),
                        (Doctor.Specialization) cmbSpec.getSelectedItem(),
                        fldLicense.getText(), fldPhone.getText(), chkAvailable.isSelected());
                UIHelper.showSuccess(this, "Doctor updated successfully.");
            }
            clearForm();
        } catch (ValidationException ex) {
            UIHelper.showError(this, "Validation error:\n" + ex.getMessage());
        } catch (RecordNotFoundException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        if (selected == null) { UIHelper.showError(this, "Please select a doctor first."); return; }
        if (!UIHelper.confirm(this, "Delete Dr. " + selected.getFullName() + " and their appointments?")) return;
        try {
            controller.deleteDoctor(selected.getId());
            clearForm();
        } catch (RecordNotFoundException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void reload() {
        tableModel.setRowCount(0);
        List<Doctor> list = controller.getAllDoctors();
        for (Doctor d : list) {
            tableModel.addRow(new Object[]{
                    d.getId(), d.getFullName(), d.getSpecialization(),
                    d.isAvailable() ? "Yes" : "No"
            });
        }
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(this::reload);
    }
}
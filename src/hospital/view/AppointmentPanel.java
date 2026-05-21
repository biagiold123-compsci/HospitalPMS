package hospital.view;

import hospital.controller.AppointmentController;
import hospital.exception.RecordNotFoundException;
import hospital.exception.ValidationException;
import hospital.model.Appointment;
import hospital.model.Doctor;
import hospital.model.Patient;
import hospital.repository.DataRepository;
import hospital.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Swing panel for scheduling and managing appointments.
 */
public class AppointmentPanel extends JPanel implements DataRepository.RepositoryListener {

    private final AppointmentController controller = new AppointmentController();
    private final DataRepository repo = DataRepository.getInstance();

    private DefaultTableModel tableModel;
    private JTable table;

    // Form
    private JComboBox<Patient> cmbPatient;
    private JComboBox<Doctor>  cmbDoctor;
    private JTextField         fldDateTime;
    private JTextField         fldReason;
    private JTextArea          fldNotes;
    private JComboBox<Appointment.Status> cmbStatus;

    private Appointment selected = null;

    public AppointmentPanel() {
        repo.addListener(this);
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = UIHelper.headerLabel("Appointments");
        title.setBorder(new EmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTable(), buildForm());
        split.setDividerLocation(500);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        reload();
    }

    private JPanel buildTable() {
        JPanel p = UIHelper.cardPanel();
        p.setLayout(new BorderLayout(0, 8));

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Patient", "Doctor", "Date & Time", "Reason", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                String status = (String) getModel().getValueAt(row, 5);
                if (!isRowSelected(row)) {
                    c.setBackground(switch (status) {
                        case "COMPLETED"  -> new Color(0xE8F5E9);
                        case "CANCELLED"  -> new Color(0xFFEBEE);
                        case "NO_SHOW"    -> new Color(0xFFF8E1);
                        default           -> Color.WHITE;
                    });
                }
                return c;
            }
        };
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setBackground(new Color(0x6F42C1));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelect();
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        JButton btnNew = UIHelper.successButton("+ New Appointment");
        JButton btnDel = UIHelper.dangerButton("Cancel/Delete");
        btnNew.addActionListener(e -> clearForm());
        btnDel.addActionListener(e -> deleteSelected());
        btns.add(btnNew); btns.add(btnDel);

        // Quick status change buttons
        JButton btnComplete = UIHelper.primaryButton("Mark Complete");
        JButton btnNoShow   = new JButton("No-Show");
        btnNoShow.setBackground(UIHelper.WARN);
        btnNoShow.setForeground(Color.WHITE);
        btnNoShow.setFocusPainted(false);
        btnNoShow.setOpaque(true);
        btnNoShow.setBorderPainted(false);
        btnComplete.addActionListener(e -> quickStatus(Appointment.Status.COMPLETED));
        btnNoShow  .addActionListener(e -> quickStatus(Appointment.Status.NO_SHOW));
        btns.add(btnComplete); btns.add(btnNoShow);

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildForm() {
        JPanel p = UIHelper.cardPanel();
        p.setLayout(new BorderLayout(0, 12));

        JLabel lbl = new JLabel("Appointment Details");
        lbl.setFont(UIHelper.FONT_HEADER);
        lbl.setForeground(new Color(0x6F42C1));
        p.add(lbl, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;

        cmbPatient = new JComboBox<>();
        cmbDoctor  = new JComboBox<>();
        fldDateTime = UIHelper.field(14);
        fldDateTime.putClientProperty("JTextField.placeholderText", controller.getDateTimePattern());
        fldReason  = UIHelper.field(14);
        fldNotes   = UIHelper.textArea(3, 14);
        cmbStatus  = new JComboBox<>(Appointment.Status.values());

        row(form, g, 0, "Patient *",    cmbPatient);
        row(form, g, 1, "Doctor *",     cmbDoctor);
        row(form, g, 2, "Date & Time *", fldDateTime);
        row(form, g, 3, "Reason *",     fldReason);
        row(form, g, 4, "Status",       cmbStatus);
        row(form, g, 5, "Notes",        new JScrollPane(fldNotes));
        row(form, g, 6, "",
                UIHelper.mutedLabel("Format: " + controller.getDateTimePattern()));

        p.add(new JScrollPane(form) {{ setBorder(null); }}, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        JButton btnCancel = UIHelper.neutralButton("Cancel");
        JButton btnSave   = UIHelper.primaryButton("Save");
        btnCancel.addActionListener(e -> clearForm());
        btnSave  .addActionListener(e -> saveForm());
        btns.add(btnCancel); btns.add(btnSave);
        p.add(btns, BorderLayout.SOUTH);

        repopulateCombos();
        return p;
    }

    private void row(JPanel f, GridBagConstraints g, int r, String lbl, Component c) {
        g.gridx = 0; g.gridy = r; g.weightx = 0; f.add(new JLabel(lbl), g);
        g.gridx = 1; g.weightx = 1; f.add(c, g);
    }

    private void repopulateCombos() {
        cmbPatient.removeAllItems();
        cmbDoctor .removeAllItems();
        for (Patient p : repo.getAllPatients()) cmbPatient.addItem(p);
        for (Doctor  d : repo.getAllDoctors())  cmbDoctor .addItem(d);
    }

    private void onSelect() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        String id = (String) tableModel.getValueAt(r, 0);
        try {
            selected = controller.getAllAppointments().stream()
                    .filter(a -> a.getId().equals(id)).findFirst()
                    .orElseThrow(() -> new RecordNotFoundException("Not found: " + id));
            fldDateTime.setText(selected.getDisplayDateTime()
                    .replace("  ", " ")); // normalize for editing
            fldReason  .setText(selected.getReason());
            fldNotes   .setText(selected.getNotes());
            cmbStatus  .setSelectedItem(selected.getStatus());
            selectComboById(cmbPatient, selected.getPatientId());
            selectComboById(cmbDoctor,  selected.getDoctorId());
        } catch (RecordNotFoundException e) {
            UIHelper.showError(this, e.getMessage());
        }
    }

    private <T> void selectComboById(JComboBox<T> combo, String id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            String itemId = (item instanceof Patient) ? ((Patient)item).getId()
                          : (item instanceof Doctor)  ? ((Doctor)item).getId()
                          : "";
            if (itemId.equals(id)) { combo.setSelectedIndex(i); return; }
        }
    }

    private void clearForm() {
        selected = null; table.clearSelection();
        fldDateTime.setText(""); fldReason.setText(""); fldNotes.setText("");
        cmbStatus.setSelectedIndex(0);
        if (cmbPatient.getItemCount() > 0) cmbPatient.setSelectedIndex(0);
        if (cmbDoctor .getItemCount() > 0) cmbDoctor .setSelectedIndex(0);
    }

    private void saveForm() {
        Patient patient = (Patient) cmbPatient.getSelectedItem();
        Doctor  doctor  = (Doctor)  cmbDoctor .getSelectedItem();
        if (patient == null || doctor == null) {
            UIHelper.showError(this, "Please select both a patient and a doctor.");
            return;
        }
        try {
            if (selected == null) {
                controller.scheduleAppointment(
                        patient.getId(), doctor.getId(),
                        fldDateTime.getText(), fldReason.getText());
                UIHelper.showSuccess(this, "Appointment scheduled.");
            } else {
                selected.setStatus((Appointment.Status) cmbStatus.getSelectedItem());
                selected.setNotes(fldNotes.getText());
                selected.setReason(fldReason.getText());
                repo.updateAppointment(selected);
                UIHelper.showSuccess(this, "Appointment updated.");
            }
            clearForm();
        } catch (ValidationException ex) {
            UIHelper.showError(this, "Validation error:\n" + ex.getMessage());
        } catch (RecordNotFoundException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void quickStatus(Appointment.Status status) {
        if (selected == null) { UIHelper.showError(this, "Please select an appointment first."); return; }
        try {
            controller.updateStatus(selected.getId(), status);
            selected = null; table.clearSelection();
        } catch (RecordNotFoundException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        if (selected == null) { UIHelper.showError(this, "Please select an appointment first."); return; }
        if (!UIHelper.confirm(this, "Delete appointment " + selected.getId() + "?")) return;
        try {
            controller.deleteAppointment(selected.getId());
            clearForm();
        } catch (RecordNotFoundException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void reload() {
        repopulateCombos();
        tableModel.setRowCount(0);
        List<Appointment> list = controller.getAllAppointments();
        list.sort((a, b) -> a.getDateTime().compareTo(b.getDateTime()));
        for (Appointment a : list) {
            String patName, docName;
            try { patName = repo.getPatientById(a.getPatientId()).getFullName(); }
            catch (RecordNotFoundException e) { patName = a.getPatientId(); }
            try { docName = repo.getDoctorById(a.getDoctorId()).getFullName(); }
            catch (RecordNotFoundException e) { docName = a.getDoctorId(); }
            tableModel.addRow(new Object[]{
                    a.getId(), patName, docName,
                    a.getDisplayDateTime(), a.getReason(), a.getStatus().name()
            });
        }
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(this::reload);
    }
}
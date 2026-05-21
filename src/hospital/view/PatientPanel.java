package hospital.view;

import hospital.controller.PatientController;
import hospital.exception.RecordNotFoundException;
import hospital.exception.ValidationException;
import hospital.model.Patient;
import hospital.repository.DataRepository;
import hospital.util.InputValidator;
import hospital.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Swing panel for managing patient records (add, edit, delete, search).
 */
public class PatientPanel extends JPanel implements DataRepository.RepositoryListener {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PatientController controller = new PatientController();

    // ── Table ──────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField searchField;

    // ── Form fields ────────────────────────────────────────────────────────────
    private JTextField   fldFirstName, fldLastName, fldDob, fldPhone, fldAddress;
    private JComboBox<Patient.BloodType> cmbBlood;
    private JTextArea    fldAllergies, fldNotes;

    // ── State ──────────────────────────────────────────────────────────────────
    private Patient selectedPatient = null;

    public PatientPanel() {
        DataRepository.getInstance().addListener(this);
        setBackground(UIHelper.BG);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);

        loadTable(controller.getAllPatients());
    }

    // ── Layout builders ────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = UIHelper.headerLabel("Patient Records");
        bar.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        searchField = UIHelper.field(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search by name, ID, phone…");
        JButton btnSearch = UIHelper.primaryButton("Search");
        JButton btnClear  = UIHelper.neutralButton("Clear");

        btnSearch.addActionListener(e -> performSearch());
        btnClear .addActionListener(e -> { searchField.setText(""); loadTable(controller.getAllPatients()); });
        searchField.addActionListener(e -> performSearch());

        right.add(new JLabel("Search:"));
        right.add(searchField);
        right.add(btnSearch);
        right.add(btnClear);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JSplitPane buildCenter() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildTablePanel(), buildFormPanel());
        split.setDividerLocation(480);
        split.setBorder(null);
        split.setOpaque(false);
        return split;
    }

    private JPanel buildTablePanel() {
        JPanel p = UIHelper.cardPanel();
        p.setLayout(new BorderLayout(0, 8));

        String[] cols = {"ID", "Name", "Age", "D.O.B", "Phone", "Blood Type"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);

        JTableHeader header = table.getTableHeader();
        header.setBackground(UIHelper.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);

        // FIX: proper header rendering (prevents transparent header issue)
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                label.setBackground(UIHelper.PRIMARY);
                label.setForeground(Color.WHITE);
                label.setOpaque(true);
                label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));

                return label;
            }
        });

        table.setFont(UIHelper.FONT_BODY);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(40);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER));

        // Action buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        JButton btnNew    = UIHelper.successButton("+ New Patient");
        JButton btnDelete = UIHelper.dangerButton("Delete");

        btnNew   .addActionListener(e -> clearForm());
        btnDelete.addActionListener(e -> deleteSelected());

        btns.add(btnNew);
        btns.add(btnDelete);

        p.add(scroll, BorderLayout.CENTER);
        p.add(btns,   BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildFormPanel() {
        JPanel p = UIHelper.cardPanel();
        p.setLayout(new BorderLayout(0, 12));

        JLabel formTitle = new JLabel("Patient Details");
        formTitle.setFont(UIHelper.FONT_HEADER);
        formTitle.setForeground(UIHelper.PRIMARY);
        p.add(formTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        fldFirstName = UIHelper.field(14);
        fldLastName  = UIHelper.field(14);
        fldDob       = UIHelper.field(14);
        fldPhone     = UIHelper.field(14);
        fldAddress   = UIHelper.field(14);
        cmbBlood     = new JComboBox<>(Patient.BloodType.values());
        fldAllergies = UIHelper.textArea(2, 14);
        fldNotes     = UIHelper.textArea(3, 14);

        int row = 0;
        addFormRow(form, gbc, row++, "First name *",   fldFirstName);
        addFormRow(form, gbc, row++, "Last name *",    fldLastName);
        addFormRow(form, gbc, row++, "Date of birth *","dd/MM/yyyy", fldDob);
        addFormRow(form, gbc, row++, "Phone *",        fldPhone);
        addFormRow(form, gbc, row++, "Address *",      fldAddress);
        addFormRow(form, gbc, row++, "Blood type",     cmbBlood);
        addFormRow(form, gbc, row++, "Allergies",      new JScrollPane(fldAllergies));
        addFormRow(form, gbc, row,   "Notes",          new JScrollPane(fldNotes));

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(null);
        p.add(formScroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);
        JButton btnSave = UIHelper.primaryButton("Save");
        JButton btnCancel = UIHelper.neutralButton("Cancel");
        btnSave.addActionListener(e -> saveForm());
        btnCancel.addActionListener(e -> clearForm());

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        p.add(btnRow, BorderLayout.SOUTH);

        return p;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row,
                            String label, String hint, JTextField field) {
        field.putClientProperty("JTextField.placeholderText", hint);
        addFormRow(form, gbc, row, label, field);
    }

    private void performSearch() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) loadTable(controller.getAllPatients());
        else loadTable(controller.searchPatients(q));
    }

    private void onRowSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String id = (String) tableModel.getValueAt(row, 0);
        try {
            selectedPatient = controller.getById(id);
            populateForm(selectedPatient);
        } catch (RecordNotFoundException e) {
            UIHelper.showError(this, e.getMessage());
        }
    }

    private void populateForm(Patient p) {
        fldFirstName.setText(p.getFirstName());
        fldLastName.setText(p.getLastName());
        fldDob.setText(p.getDateOfBirth().format(DATE_FMT));
        fldPhone.setText(p.getPhoneNumber());
        fldAddress.setText(p.getAddress());
        cmbBlood.setSelectedItem(p.getBloodType());
        fldAllergies.setText(p.getAllergies());
        fldNotes.setText(p.getMedicalNotes());
    }

    private void clearForm() {
        selectedPatient = null;
        table.clearSelection();
        fldFirstName.setText(""); fldLastName.setText("");
        fldDob.setText(""); fldPhone.setText("");
        fldAddress.setText(""); cmbBlood.setSelectedIndex(0);
        fldAllergies.setText(""); fldNotes.setText("");
        fldFirstName.requestFocus();
    }

    private void saveForm() {
        try {
            if (selectedPatient == null) {
                Patient p = controller.addPatient(
                        fldFirstName.getText(), fldLastName.getText(),
                        fldDob.getText(), fldPhone.getText(), fldAddress.getText(),
                        (Patient.BloodType) cmbBlood.getSelectedItem());
                p.setAllergies(fldAllergies.getText());
                p.setMedicalNotes(fldNotes.getText());
                DataRepository.getInstance().updatePatient(p);
                UIHelper.showSuccess(this, "Patient added: " + p.getFullName());
            } else {
                controller.updatePatient(selectedPatient,
                        fldFirstName.getText(), fldLastName.getText(),
                        fldDob.getText(), fldPhone.getText(), fldAddress.getText(),
                        (Patient.BloodType) cmbBlood.getSelectedItem(),
                        fldAllergies.getText(), fldNotes.getText());
                UIHelper.showSuccess(this, "Patient updated successfully.");
            }
            clearForm();
        } catch (ValidationException ex) {
            UIHelper.showError(this, "Validation error:\n" + ex.getMessage());
        } catch (RecordNotFoundException ex) {
            UIHelper.showError(this, "Record not found:\n" + ex.getMessage());
        }
    }

    private void deleteSelected() {
        if (selectedPatient == null) { UIHelper.showError(this, "Please select a patient first."); return; }
        if (!UIHelper.confirm(this, "Delete patient \"" + selectedPatient.getFullName() + "\" and all their appointments?")) return;
        try {
            controller.deletePatient(selectedPatient.getId());
            clearForm();
        } catch (RecordNotFoundException ex) {
            UIHelper.showError(this, ex.getMessage());
        }
    }

    private void loadTable(List<Patient> list) {
        tableModel.setRowCount(0);
        for (Patient p : list) {
            tableModel.addRow(new Object[]{
                    p.getId(), p.getFullName(), p.getAge(),
                    p.getDateOfBirth().format(DATE_FMT),
                    p.getPhoneNumber(), p.getBloodType()
            });
        }
    }

    @Override
    public void onDataChanged() {
        SwingUtilities.invokeLater(() -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) loadTable(controller.getAllPatients());
            else loadTable(controller.searchPatients(query));
        });
    }
}
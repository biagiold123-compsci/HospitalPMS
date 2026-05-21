package hospital.repository;

import hospital.exception.RecordNotFoundException;
import hospital.model.Appointment;
import hospital.model.Doctor;
import hospital.model.Patient;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton Repository that holds all runtime data and persists it
 * to disk as serialized binary files (*.dat).
 *
 * <p>Pattern: Singleton + Repository</p>
 */
public class DataRepository {

    // ── Singleton ────────────────────────────────────────────────────────────

    private static DataRepository instance;

    private DataRepository() {
        loadAll();
        if (patients.isEmpty()) seedDemoData();
    }

    public static synchronized DataRepository getInstance() {
        if (instance == null) instance = new DataRepository();
        return instance;
    }

    // ── Storage paths ────────────────────────────────────────────────────────

    private static final String DATA_DIR         = System.getProperty("user.home") + "/HospitalPMS/data/";
    private static final String PATIENTS_FILE    = DATA_DIR + "patients.dat";
    private static final String DOCTORS_FILE     = DATA_DIR + "doctors.dat";
    private static final String APPOINTMENTS_FILE= DATA_DIR + "appointments.dat";

    // ── In-memory stores ─────────────────────────────────────────────────────

    private Map<String, Patient>     patients     = new LinkedHashMap<>();
    private Map<String, Doctor>      doctors      = new LinkedHashMap<>();
    private Map<String, Appointment> appointments = new LinkedHashMap<>();

    // ── Observers (simple Observer pattern) ──────────────────────────────────

    private final List<RepositoryListener> listeners = new ArrayList<>();

    public void addListener(RepositoryListener l)    { listeners.add(l); }
    public void removeListener(RepositoryListener l) { listeners.remove(l); }

    private void notifyChange() {
        listeners.forEach(RepositoryListener::onDataChanged);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PATIENT CRUD
    // ════════════════════════════════════════════════════════════════════════

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patients.values());
    }

    public Patient getPatientById(String id) throws RecordNotFoundException {
        Patient p = patients.get(id);
        if (p == null) throw new RecordNotFoundException("Patient not found: " + id);
        return p;
    }

    public List<Patient> searchPatients(String query) {
        String q = query.toLowerCase();
        return patients.values().stream()
                .filter(p -> p.getFullName().toLowerCase().contains(q)
                          || p.getId().toLowerCase().contains(q)
                          || p.getPhoneNumber().contains(q))
                .collect(Collectors.toList());
    }

    public void addPatient(Patient patient) {
        patients.put(patient.getId(), patient);
        saveAll();
        notifyChange();
    }

    public void updatePatient(Patient patient) throws RecordNotFoundException {
        if (!patients.containsKey(patient.getId()))
            throw new RecordNotFoundException("Cannot update – patient not found: " + patient.getId());
        patients.put(patient.getId(), patient);
        saveAll();
        notifyChange();
    }

    public void deletePatient(String id) throws RecordNotFoundException {
        if (patients.remove(id) == null)
            throw new RecordNotFoundException("Cannot delete – patient not found: " + id);
        // Also remove related appointments
        appointments.values().removeIf(a -> a.getPatientId().equals(id));
        saveAll();
        notifyChange();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DOCTOR CRUD
    // ════════════════════════════════════════════════════════════════════════

    public List<Doctor> getAllDoctors() {
        return new ArrayList<>(doctors.values());
    }

    public Doctor getDoctorById(String id) throws RecordNotFoundException {
        Doctor d = doctors.get(id);
        if (d == null) throw new RecordNotFoundException("Doctor not found: " + id);
        return d;
    }

    public void addDoctor(Doctor doctor) {
        doctors.put(doctor.getId(), doctor);
        saveAll();
        notifyChange();
    }

    public void updateDoctor(Doctor doctor) throws RecordNotFoundException {
        if (!doctors.containsKey(doctor.getId()))
            throw new RecordNotFoundException("Cannot update – doctor not found: " + doctor.getId());
        doctors.put(doctor.getId(), doctor);
        saveAll();
        notifyChange();
    }

    public void deleteDoctor(String id) throws RecordNotFoundException {
        if (doctors.remove(id) == null)
            throw new RecordNotFoundException("Cannot delete – doctor not found: " + id);
        appointments.values().removeIf(a -> a.getDoctorId().equals(id));
        saveAll();
        notifyChange();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  APPOINTMENT CRUD
    // ════════════════════════════════════════════════════════════════════════

    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments.values());
    }

    public Appointment getAppointmentById(String id) throws RecordNotFoundException {
        Appointment a = appointments.get(id);
        if (a == null) throw new RecordNotFoundException("Appointment not found: " + id);
        return a;
    }

    public List<Appointment> getAppointmentsForPatient(String patientId) {
        return appointments.values().stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .sorted(Comparator.comparing(Appointment::getDateTime))
                .collect(Collectors.toList());
    }

    public List<Appointment> getAppointmentsForDoctor(String doctorId) {
        return appointments.values().stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .sorted(Comparator.comparing(Appointment::getDateTime))
                .collect(Collectors.toList());
    }

    public void addAppointment(Appointment appointment) {
        appointments.put(appointment.getId(), appointment);
        saveAll();
        notifyChange();
    }

    public void updateAppointment(Appointment appointment) throws RecordNotFoundException {
        if (!appointments.containsKey(appointment.getId()))
            throw new RecordNotFoundException("Cannot update – appointment not found: " + appointment.getId());
        appointments.put(appointment.getId(), appointment);
        saveAll();
        notifyChange();
    }

    public void deleteAppointment(String id) throws RecordNotFoundException {
        if (appointments.remove(id) == null)
            throw new RecordNotFoundException("Cannot delete – appointment not found: " + id);
        saveAll();
        notifyChange();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STATISTICS
    // ════════════════════════════════════════════════════════════════════════

    public int getTotalPatients()      { return patients.size(); }
    public int getTotalDoctors()       { return doctors.size(); }
    public int getTotalAppointments()  { return appointments.size(); }

    public long getScheduledAppointments() {
        return appointments.values().stream()
                .filter(a -> a.getStatus() == Appointment.Status.SCHEDULED)
                .count();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PERSISTENCE
    // ════════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void loadAll() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Warning: could not create data directory: " + e.getMessage());
        }
        patients     = loadMap(PATIENTS_FILE,     patients);
        doctors      = loadMap(DOCTORS_FILE,      doctors);
        appointments = loadMap(APPOINTMENTS_FILE, appointments);
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> loadMap(String path, Map<K, V> defaultValue) {
        File f = new File(path);
        if (!f.exists()) return defaultValue;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Map<K, V>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Warning: could not load " + path + " – " + e.getMessage());
            return defaultValue;
        }
    }

    public void saveAll() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            saveMap(patients,     PATIENTS_FILE);
            saveMap(doctors,      DOCTORS_FILE);
            saveMap(appointments, APPOINTMENTS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private void saveMap(Map<?, ?> map, String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(map);
        } catch (IOException e) {
            System.err.println("Error writing " + path + ": " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DEMO DATA (first launch only)
    // ════════════════════════════════════════════════════════════════════════

    private void seedDemoData() {
        // Doctors
        Doctor d1 = new Doctor("Eleanor", "Hayes",   Doctor.Specialization.CARDIOLOGY,    "LIC-1001", "555-0101");
        Doctor d2 = new Doctor("Marcus",  "Webb",    Doctor.Specialization.NEUROLOGY,     "LIC-1002", "555-0102");
        Doctor d3 = new Doctor("Priya",   "Sharma",  Doctor.Specialization.GENERAL_PRACTICE, "LIC-1003", "555-0103");
        doctors.put(d1.getId(), d1);
        doctors.put(d2.getId(), d2);
        doctors.put(d3.getId(), d3);

        // Patients
        Patient p1 = new Patient("John",   "Carter",  LocalDate.of(1978, 3, 14), "555-2001", "12 Oak St",    Patient.BloodType.O_POS);
        Patient p2 = new Patient("Maria",  "Lopez",   LocalDate.of(1990, 7, 22), "555-2002", "88 Maple Ave", Patient.BloodType.A_NEG);
        Patient p3 = new Patient("Samuel", "Chen",    LocalDate.of(1965, 11, 5), "555-2003", "7 Pine Rd",    Patient.BloodType.B_POS);
        p1.setAllergies("Penicillin");
        p3.setMedicalNotes("Hypertension – monitored");
        patients.put(p1.getId(), p1);
        patients.put(p2.getId(), p2);
        patients.put(p3.getId(), p3);

        // Appointments
        Appointment a1 = new Appointment(p1.getId(), d1.getId(),
                LocalDateTime.now().plusDays(1).withHour(9).withMinute(0), "Chest pain follow-up");
        Appointment a2 = new Appointment(p2.getId(), d3.getId(),
                LocalDateTime.now().plusDays(2).withHour(11).withMinute(30), "Annual check-up");
        Appointment a3 = new Appointment(p3.getId(), d2.getId(),
                LocalDateTime.now().plusDays(3).withHour(14).withMinute(0), "Migraine consultation");
        appointments.put(a1.getId(), a1);
        appointments.put(a2.getId(), a2);
        appointments.put(a3.getId(), a3);

        saveAll();
    }

    // ── Listener interface ───────────────────────────────────────────────────

    public interface RepositoryListener {
        void onDataChanged();
    }
}
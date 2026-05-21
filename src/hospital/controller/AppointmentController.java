package hospital.controller;

import hospital.exception.RecordNotFoundException;
import hospital.exception.ValidationException;
import hospital.model.Appointment;
import hospital.model.Doctor;
import hospital.model.Patient;
import hospital.repository.DataRepository;
import hospital.util.InputValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller for appointment-related operations.
 */
public class AppointmentController {

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DataRepository repo = DataRepository.getInstance();

    public List<Appointment> getAllAppointments() {
        return repo.getAllAppointments();
    }

    public List<Appointment> getForPatient(String patientId) {
        return repo.getAppointmentsForPatient(patientId);
    }

    public List<Appointment> getForDoctor(String doctorId) {
        return repo.getAppointmentsForDoctor(doctorId);
    }

    /**
     * Schedules a new appointment.
     *
     * @param patientId  ID of the patient
     * @param doctorId   ID of the doctor
     * @param dateTimeText "dd/MM/yyyy HH:mm" string from the form
     * @param reason     brief reason for the visit
     * @throws ValidationException     if inputs are invalid or the slot is in the past
     * @throws RecordNotFoundException if the patient or doctor ID is invalid
     */
    public Appointment scheduleAppointment(String patientId, String doctorId,
                                           String dateTimeText, String reason)
            throws ValidationException, RecordNotFoundException {

        // Validate that referenced records exist
        repo.getPatientById(patientId);
        repo.getDoctorById(doctorId);

        InputValidator.requireNonBlank(reason, "Reason for visit");

        LocalDateTime dt;
        try {
            dt = LocalDateTime.parse(dateTimeText.trim(), DT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Date/time must be in dd/MM/yyyy HH:mm format.");
        }

        if (dt.isBefore(LocalDateTime.now()))
            throw new ValidationException("Appointment date must be in the future.");

        Appointment appointment = new Appointment(patientId, doctorId, dt, reason);
        repo.addAppointment(appointment);
        return appointment;
    }

    /**
     * Changes the status of an existing appointment.
     */
    public void updateStatus(String appointmentId, Appointment.Status newStatus)
            throws RecordNotFoundException {
        Appointment a = repo.getAppointmentById(appointmentId);
        a.setStatus(newStatus);
        repo.updateAppointment(a);
    }

    /**
     * Adds clinical notes to a completed appointment.
     */
    public void addNotes(String appointmentId, String notes)
            throws RecordNotFoundException {
        Appointment a = repo.getAppointmentById(appointmentId);
        a.setNotes(notes);
        repo.updateAppointment(a);
    }

    public void deleteAppointment(String id) throws RecordNotFoundException {
        repo.deleteAppointment(id);
    }

    public String getDateTimePattern() { return "dd/MM/yyyy HH:mm"; }
}
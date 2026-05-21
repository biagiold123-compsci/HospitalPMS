package hospital.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a scheduled appointment between a patient and a doctor.
 */
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status { SCHEDULED, COMPLETED, CANCELLED, NO_SHOW }

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy  HH:mm");

    private final String id;
    private String patientId;
    private String doctorId;
    private LocalDateTime dateTime;
    private String reason;
    private String notes;
    private Status status;

    public Appointment(String patientId, String doctorId,
                       LocalDateTime dateTime, String reason) {
        this.id        = "APT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.patientId = patientId;
        this.doctorId  = doctorId;
        this.dateTime  = dateTime;
        this.reason    = reason;
        this.notes     = "";
        this.status    = Status.SCHEDULED;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()             { return id; }
    public String getPatientId()      { return patientId; }
    public String getDoctorId()       { return doctorId; }
    public LocalDateTime getDateTime(){ return dateTime; }
    public String getDisplayDateTime(){ return dateTime.format(DISPLAY_FORMAT); }
    public String getReason()         { return reason; }
    public String getNotes()          { return notes; }
    public Status getStatus()         { return status; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setPatientId(String patientId)  { this.patientId = patientId; }
    public void setDoctorId(String doctorId)    { this.doctorId = doctorId; }
    public void setDateTime(LocalDateTime dt)   { this.dateTime = dt; }
    public void setReason(String reason)        { this.reason = reason; }
    public void setNotes(String notes)          { this.notes = notes; }
    public void setStatus(Status status)        { this.status = status; }

    @Override
    public String toString() {
        return "[" + id + "] " + getDisplayDateTime() + " – " + reason + " (" + status + ")";
    }
}
package hospital.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a doctor/physician at the hospital.
 */
public class Doctor implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Specialization {
        GENERAL_PRACTICE("General Practice"),
        CARDIOLOGY("Cardiology"),
        NEUROLOGY("Neurology"),
        ORTHOPEDICS("Orthopedics"),
        PEDIATRICS("Pediatrics"),
        ONCOLOGY("Oncology"),
        DERMATOLOGY("Dermatology"),
        PSYCHIATRY("Psychiatry"),
        SURGERY("Surgery"),
        EMERGENCY("Emergency Medicine");

        private final String displayName;

        Specialization(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() { return displayName; }
    }

    private final String id;
    private String firstName;
    private String lastName;
    private Specialization specialization;
    private String licenseNumber;
    private String phoneNumber;
    private boolean available;

    public Doctor(String firstName, String lastName,
                  Specialization specialization, String licenseNumber, String phoneNumber) {
        this.id              = "DR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.firstName       = firstName;
        this.lastName        = lastName;
        this.specialization  = specialization;
        this.licenseNumber   = licenseNumber;
        this.phoneNumber     = phoneNumber;
        this.available       = true;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()                          { return id; }
    public String getFirstName()                   { return firstName; }
    public String getLastName()                    { return lastName; }
    public String getFullName()                    { return "Dr. " + firstName + " " + lastName; }
    public Specialization getSpecialization()      { return specialization; }
    public String getLicenseNumber()               { return licenseNumber; }
    public String getPhoneNumber()                 { return phoneNumber; }
    public boolean isAvailable()                   { return available; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setFirstName(String firstName)               { this.firstName = firstName; }
    public void setLastName(String lastName)                 { this.lastName = lastName; }
    public void setSpecialization(Specialization spec)       { this.specialization = spec; }
    public void setLicenseNumber(String licenseNumber)       { this.licenseNumber = licenseNumber; }
    public void setPhoneNumber(String phoneNumber)           { this.phoneNumber = phoneNumber; }
    public void setAvailable(boolean available)              { this.available = available; }

    @Override
    public String toString() {
        return getFullName() + " – " + specialization;
    }
}
package hospital.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a patient record in the hospital system.
 * Implements Serializable for file-based persistence.
 */

public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum BloodType { A_POS, A_NEG, B_POS, B_NEG, AB_POS, AB_NEG, O_POS, O_NEG, UNKNOWN }

    private final String id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String address;
    private BloodType bloodType;
    private String allergies;
    private String medicalNotes;

    public Patient(String firstName, String lastName, LocalDate dateOfBirth,
                   String phoneNumber, String address, BloodType bloodType) {
        this.id          = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.firstName   = firstName;
        this.lastName    = lastName;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.address     = address;
        this.bloodType   = bloodType;
        this.allergies   = "";
        this.medicalNotes = "";
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()            { return id; }
    public String getFirstName()     { return firstName; }
    public String getLastName()      { return lastName; }
    public String getFullName()      { return firstName + " " + lastName; }
    public LocalDate getDateOfBirth(){ return dateOfBirth; }
    public String getPhoneNumber()   { return phoneNumber; }
    public String getAddress()       { return address; }
    public BloodType getBloodType()  { return bloodType; }
    public String getAllergies()     { return allergies; }
    public String getMedicalNotes()  { return medicalNotes; }

    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setFirstName(String firstName)       { this.firstName = firstName; }
    public void setLastName(String lastName)         { this.lastName = lastName; }
    public void setDateOfBirth(LocalDate dob)        { this.dateOfBirth = dob; }
    public void setPhoneNumber(String phoneNumber)   { this.phoneNumber = phoneNumber; }
    public void setAddress(String address)           { this.address = address; }
    public void setBloodType(BloodType bloodType)    { this.bloodType = bloodType; }
    public void setAllergies(String allergies)       { this.allergies = allergies; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    @Override
    public String toString() {
        return "[" + id + "] " + getFullName() + " (Age: " + getAge() + ")";
    }
}
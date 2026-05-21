package hospital.controller;

import hospital.exception.RecordNotFoundException;
import hospital.exception.ValidationException;
import hospital.model.Patient;
import hospital.repository.DataRepository;
import hospital.util.InputValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for all patient-related operations.
 * Validates input then delegates to the DataRepository.
 */
public class PatientController {

    private final DataRepository repo = DataRepository.getInstance();

    /** Returns all patients. */
    public List<Patient> getAllPatients() {
        return repo.getAllPatients();
    }

    /** Returns patients whose name, ID, or phone matches the query. */
    public List<Patient> searchPatients(String query) {
        return repo.searchPatients(query);
    }

    /** Fetches a single patient by ID. */
    public Patient getById(String id) throws RecordNotFoundException {
        return repo.getPatientById(id);
    }

    /**
     * Validates and creates a new patient record.
     *
     * @return the newly created Patient
     * @throws ValidationException if any field fails validation
     */
    public Patient addPatient(String firstName, String lastName, String dobText,
                              String phone, String address,
                              Patient.BloodType bloodType) throws ValidationException {

        String fn  = InputValidator.requireNonBlank(firstName, "First name");
        String ln  = InputValidator.requireNonBlank(lastName,  "Last name");
        LocalDate dob = InputValidator.parseDate(dobText, "Date of birth");
        String ph  = InputValidator.validatePhone(phone);
        String adr = InputValidator.requireNonBlank(address, "Address");

        Patient patient = new Patient(fn, ln, dob, ph, adr, bloodType);
        repo.addPatient(patient);
        return patient;
    }

    /**
     * Updates an existing patient's editable fields.
     *
     * @throws ValidationException      if any field fails validation
     * @throws RecordNotFoundException  if the patient no longer exists
     */
    public void updatePatient(Patient patient, String firstName, String lastName,
                              String dobText, String phone, String address,
                              Patient.BloodType bloodType, String allergies,
                              String medicalNotes)
            throws ValidationException, RecordNotFoundException {

        patient.setFirstName(InputValidator.requireNonBlank(firstName, "First name"));
        patient.setLastName(InputValidator.requireNonBlank(lastName,   "Last name"));
        patient.setDateOfBirth(InputValidator.parseDate(dobText, "Date of birth"));
        patient.setPhoneNumber(InputValidator.validatePhone(phone));
        patient.setAddress(InputValidator.requireNonBlank(address, "Address"));
        patient.setBloodType(bloodType);
        patient.setAllergies(allergies);
        patient.setMedicalNotes(medicalNotes);

        repo.updatePatient(patient);
    }

    /**
     * Deletes a patient and all their appointments.
     *
     * @throws RecordNotFoundException if the patient does not exist
     */
    public void deletePatient(String id) throws RecordNotFoundException {
        repo.deletePatient(id);
    }
}
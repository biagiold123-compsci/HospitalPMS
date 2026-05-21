package hospital.controller;

import hospital.exception.RecordNotFoundException;
import hospital.exception.ValidationException;
import hospital.model.Doctor;
import hospital.repository.DataRepository;
import hospital.util.InputValidator;

import java.util.List;

/**
 * Controller for doctor-related operations.
 */
public class DoctorController {

    private final DataRepository repo = DataRepository.getInstance();

    public List<Doctor> getAllDoctors() {
        return repo.getAllDoctors();
    }

    public Doctor getById(String id) throws RecordNotFoundException {
        return repo.getDoctorById(id);
    }

    public Doctor addDoctor(String firstName, String lastName,
                            Doctor.Specialization specialization,
                            String licenseNumber, String phone)
            throws ValidationException {

        String fn  = InputValidator.requireNonBlank(firstName,     "First name");
        String ln  = InputValidator.requireNonBlank(lastName,      "Last name");
        String lic = InputValidator.requireNonBlank(licenseNumber, "License number");
        String ph  = InputValidator.validatePhone(phone);

        Doctor doctor = new Doctor(fn, ln, specialization, lic, ph);
        repo.addDoctor(doctor);
        return doctor;
    }

    public void updateDoctor(Doctor doctor, String firstName, String lastName,
                             Doctor.Specialization specialization,
                             String licenseNumber, String phone, boolean available)
            throws ValidationException, RecordNotFoundException {

        doctor.setFirstName(InputValidator.requireNonBlank(firstName,     "First name"));
        doctor.setLastName(InputValidator.requireNonBlank(lastName,       "Last name"));
        doctor.setSpecialization(specialization);
        doctor.setLicenseNumber(InputValidator.requireNonBlank(licenseNumber, "License number"));
        doctor.setPhoneNumber(InputValidator.validatePhone(phone));
        doctor.setAvailable(available);

        repo.updateDoctor(doctor);
    }

    public void deleteDoctor(String id) throws RecordNotFoundException {
        repo.deleteDoctor(id);
    }
}
# HospitalPMS

A desktop Hospital Patient Management System built with **Java** and **Swing**, demonstrating a clean MVC architecture, real data persistence, and a polished multi-panel GUI fit for a professional environment.

---

## Features

| Feature | Detail |
|---|---|
| **Patient Management** | Register, search, edit, and delete patient records with blood type, allergies, and medical notes |
| **Doctor Management** | Manage physician profiles, specializations, license numbers, and availability status |
| **Appointment Scheduling** | Book, cancel, and complete appointments with date/time validation and clinical notes |
| **Live Dashboard** | Stat cards showing total patients, doctors, appointments, and scheduled count — auto-refreshes on data changes |
| **Binary Persistence** | All records serialized to `.dat` files in `~/HospitalPMS/data/` and reloaded on every launch |
| **Demo Data Seeding** | Pre-populates three doctors, three patients, and three appointments on first launch |
| **Input Validation** | Centralized `InputValidator` enforces non-blank fields, `dd/MM/yyyy` date format, and phone number rules |
| **Custom Exception Hierarchy** | `ValidationException` and `RecordNotFoundException` cleanly separate input errors from data errors |
| **Observer Pattern** | `RepositoryListener` interface propagates data changes to all live panels without coupling |
| **Consistent UI System** | `UIHelper` provides a shared color palette, font scale, button factory, and card panel factory |

---

## Project Structure

```
HospitalPMS/
└── src/
    └── hospital/
        ├── HospitalApp.java                  # Entry point — launches Swing on the EDT
        ├── module-info.java                  # Java module declaration
        ├── controller/
        │   ├── AppointmentController.java    # Validates and delegates appointment operations
        │   ├── DoctorController.java         # Validates and delegates doctor operations
        │   └── PatientController.java        # Validates and delegates patient operations
        ├── exception/
        │   ├── RecordNotFoundException.java  # Thrown when a record lookup fails
        │   └── ValidationException.java      # Thrown when form input fails validation
        ├── model/
        │   ├── Appointment.java              # Appointment entity with Status enum
        │   ├── Doctor.java                   # Doctor entity with Specialization enum
        │   └── Patient.java                  # Patient entity with BloodType enum
        ├── repository/
        │   └── DataRepository.java           # Singleton data store with CRUD + serialization
        ├── util/
        │   ├── InputValidator.java           # Static form validation helpers
        │   └── UIHelper.java                 # Shared Swing styling constants and factories
        └── view/
            ├── AppointmentPanel.java         # Appointments tab UI
            ├── DashboardPanel.java           # Dashboard with live stat cards
            ├── DoctorPanel.java              # Doctors tab UI
            ├── MainFrame.java                # Top-level JFrame with sidebar and CardLayout
            └── PatientPanel.java             # Patients tab UI
```

---

## Key Java Concepts Demonstrated

- **MVC Architecture** — models, controllers, and Swing views are cleanly separated with no cross-layer dependencies
- **Singleton Pattern** — `DataRepository.getInstance()` ensures a single shared data store across all controllers and views
- **Observer Pattern** — `RepositoryListener` interface lets panels subscribe to data changes and refresh automatically
- **Serializable** — all three model classes implement `Serializable` for file-based binary persistence via `ObjectOutputStream`
- **Enums with constructors** — `Doctor.Specialization`, `Patient.BloodType`, and `Appointment.Status` carry display names and behaviour
- **Java Time API** — `LocalDate`, `LocalDateTime`, and `DateTimeFormatter` used throughout for type-safe date handling
- **Streams and Collectors** — `searchPatients`, `getAppointmentsForPatient`, and statistics methods use `Stream.filter`, `sorted`, and `count`
- **Checked exceptions** — `ValidationException` and `RecordNotFoundException` enforce explicit error handling at every call site
- **Swing EDT compliance** — all UI construction and updates run through `SwingUtilities.invokeLater`
- **CardLayout navigation** — the main frame swaps panels without destroying state, driven by a sidebar button group
- **Factory methods** — `UIHelper` exposes `primaryButton`, `dangerButton`, `cardPanel`, and label factories for consistent styling
- **UUID-based IDs** — patients get 8-character IDs, doctors get `DR-XXXXXX` prefixed IDs, appointments get `APT-XXXXXX`
- **Functional interface** — `LabelSetter` in `DashboardPanel` is a custom `@FunctionalInterface` used as a lambda callback

---

## Getting Started

### Prerequisites

- Java 17 or later (Java 21 recommended)
- Eclipse IDE (the project includes `.classpath` and is configured for Eclipse out of the box)

### Run in Eclipse

1. **File → Import → Existing Projects into Workspace**
2. Select the `HospitalPMS` root folder and click **Finish**
3. Right-click `HospitalApp.java` → **Run As → Java Application**

### Run from the command line

```bash
cd HospitalPMS
javac -d out --module-source-path src -m hospital
java --module-path out -m hospital/hospital.HospitalApp
```

---

## Using the Application

Once launched, the app opens to the **Dashboard** tab. Use the left sidebar to navigate between sections.

| Tab | What you can do |
|---|---|
| **Dashboard** | View live counts of patients, doctors, total appointments, and scheduled appointments |
| **Patients** | Add new patients, search by name/ID/phone, edit records, view appointment history, delete |
| **Doctors** | Add physicians, set specialization and license number, toggle availability, delete |
| **Appointments** | Schedule appointments by selecting a patient and doctor, update status, add clinical notes, cancel |

Data is saved automatically to `~/HospitalPMS/data/` after every change and reloaded the next time the application starts.

---

## Data Persistence

Records are stored as serialized binary files in your home directory:

```
~/HospitalPMS/data/
├── patients.dat
├── doctors.dat
└── appointments.dat
```

These files are created automatically on first launch. Deleting them resets the application to the demo seed data.

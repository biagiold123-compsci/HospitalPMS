package hospital.util;

import hospital.exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Static utility methods for validating and parsing user input
 * from Swing form fields.
 */
public final class InputValidator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private InputValidator() {}

    /**
     * Ensures a text field is non-null and non-blank.
     *
     * @throws ValidationException if blank
     */
    public static String requireNonBlank(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty())
            throw new ValidationException(fieldName + " must not be empty.");
        return value.trim();
    }

    /**
     * Parses a date string in dd/MM/yyyy format.
     *
     * @throws ValidationException if the format is invalid or the date is in the future
     */
    public static LocalDate parseDate(String text, String fieldName) throws ValidationException {
        requireNonBlank(text, fieldName);
        try {
            LocalDate date = LocalDate.parse(text.trim(), DATE_FORMAT);
            if (date.isAfter(LocalDate.now()))
                throw new ValidationException(fieldName + " cannot be a future date.");
            return date;
        } catch (DateTimeParseException e) {
            throw new ValidationException(fieldName + " must be in dd/MM/yyyy format.");
        }
    }

    /**
     * Validates a basic phone number (digits, spaces, dashes, parentheses).
     *
     * @throws ValidationException if the format looks wrong
     */
    public static String validatePhone(String phone) throws ValidationException {
        requireNonBlank(phone, "Phone number");
        String stripped = phone.replaceAll("[\\s\\-()]", "");
        if (!stripped.matches("\\d{7,15}"))
            throw new ValidationException("Phone number must contain 7–15 digits.");
        return phone.trim();
    }

    /** Returns the date format pattern used by this validator. */
    public static String getDatePattern() { return "dd/MM/yyyy"; }
}
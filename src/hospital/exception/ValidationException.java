package hospital.exception;

/**
 * Thrown when user-provided data fails validation rules.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
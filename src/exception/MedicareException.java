package exception;

/**
 * Base domain exception for the MEDICARE application.
 * Demonstrates custom exception hierarchies in Java.
 */
public class MedicareException extends Exception {
    public MedicareException(String message) {
        super(message);
    }

    public MedicareException(String message, Throwable cause) {
        super(message, cause);
    }
}

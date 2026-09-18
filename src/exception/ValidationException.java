package exception;

/**
 * Thrown when entity or input validation rules fail.
 */
public class ValidationException extends MedicareException {
    private final String fieldName;

    public ValidationException(String message) {
        super(message);
        this.fieldName = "";
    }

    public ValidationException(String fieldName, String message) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}

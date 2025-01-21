package exceptions;

public class TaskTimeOverlapException extends RuntimeException {
    public TaskTimeOverlapException() {
    }

    public TaskTimeOverlapException(String message) {
        super(message);
    }
}

package core.exceptions;

public class MessageTooShortException extends Exception {
    public MessageTooShortException(String message) {
        super(message);
    }
}

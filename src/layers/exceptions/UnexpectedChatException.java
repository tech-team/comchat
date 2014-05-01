package layers.exceptions;

public class UnexpectedChatException extends ChatException {
    public UnexpectedChatException(String message) {
        super(message);
    }

    public UnexpectedChatException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedChatException(Throwable cause) {
        super(cause);
    }
}

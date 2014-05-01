package layers.exceptions;

public class LayerUnavailableException extends ChatException {
    public LayerUnavailableException(String message) {
        super(message);
    }

    public LayerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public LayerUnavailableException(Throwable cause) {
        super(cause);
    }
}

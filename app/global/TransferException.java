package global;

public class TransferException extends RuntimeException {
    public TransferException() { super(); }
    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }
    public TransferException(String message) { super(message); }

}

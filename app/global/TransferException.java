package global;

import models.Status;

public class TransferException extends RuntimeException {
    private Status status;

    public TransferException(Status status ,String message) {
        super("Exception during: " + status.toString() + " / " + message);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}

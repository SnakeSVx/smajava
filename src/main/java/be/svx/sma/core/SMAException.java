package be.svx.sma.core;

/**
 * Created by Stijn on 7/02/14.
 */
public class SMAException extends RuntimeException {

    public SMAException() {
    }

    public SMAException(String message) {
        super(message);
    }

    public SMAException(String message, Throwable cause) {
        super(message, cause);
    }

}

package be.svx.smajava;

/**
 * Created by Stijn on 5/02/14.
 */
public interface Request {

    RequestType getRequestType();

    ResponseType getResponseType();

    byte[] dataToSend();

}

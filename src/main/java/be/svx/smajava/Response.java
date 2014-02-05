package be.svx.smajava;

/**
 * Created by Stijn on 5/02/14.
 */
public interface Response {

    ResponseType getResponseType();

    void processData(byte[] bytes) throws FaultyResponseException;
}

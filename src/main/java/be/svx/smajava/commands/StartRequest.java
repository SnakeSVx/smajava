package be.svx.smajava.commands;

import be.svx.smajava.Request;
import be.svx.smajava.RequestType;
import be.svx.smajava.ResponseType;

/**
 * Created by Stijn on 5/02/14.
 */
public class StartRequest implements Request {
    @Override
    public RequestType getRequestType() {
        return RequestType.START;
    }

    @Override
    public ResponseType getResponseType() {
        return ResponseType.START;
    }

    @Override
    public byte[] dataToSend() {
        return null;
    }
}

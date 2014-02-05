package be.svx.smajava.commands;


import be.svx.smajava.engine.RequestType;
import be.svx.smajava.engine.ResponseType;
import be.svx.smajava.engine.Engine;
import be.svx.smajava.engine.Request;

/**
 * Created by Stijn on 5/02/14.
 */
public class InitRequest extends Request {

    public InitRequest(Engine engine) {
        super(engine);
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.INIT;
    }

    @Override
    public ResponseType getResponseType() {
        return ResponseType.INIT;
    }

    @Override
    public byte[] dataToSend() {
        return null;
    }
}

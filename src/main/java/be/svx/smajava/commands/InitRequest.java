package be.svx.smajava.commands;


import be.svx.smajava.engine.*;

/**
 * Created by Stijn on 5/02/14.
 */
public class InitRequest extends Request {

    public InitRequest(Engine engine) {
        super(engine);
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.INIT_STEP1;
    }

    @Override
    public ResponseType getResponseType() {
        return ResponseType.INIT_STEP1;
    }

    @Override
    public Packet dataToSend() {
        return null;
    }
}

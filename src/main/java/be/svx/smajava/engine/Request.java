package be.svx.smajava.engine;

/**
 * Created by Stijn on 5/02/14.
 */
public abstract class Request {

    private Engine engine;

    public Request(Engine engine){
        this.engine = engine;
    }

    public abstract RequestType getRequestType();

    public abstract ResponseType getResponseType();

    public abstract byte[] dataToSend();

    public Engine getEngine() {
        return engine;
    }
}

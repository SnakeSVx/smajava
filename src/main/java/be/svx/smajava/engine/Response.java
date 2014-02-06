package be.svx.smajava.engine;

/**
 * Created by Stijn on 5/02/14.
 */
public abstract class Response {

    private Engine engine;

    public Response(Engine engine){
        this.engine = engine;
    }

    public abstract ResponseType getResponseType();

    public abstract void processData(Packet packet) throws FaultyResponseException;

    public Engine getEngine() {
        return engine;
    }

}

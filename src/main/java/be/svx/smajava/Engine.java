package be.svx.smajava;

import be.geek.smajava.ByteResult;
import be.geek.smajava.Inverter;
import be.geek.smajava.SmajavaException;

import java.io.IOException;

/**
 * Created by Stijn on 5/02/14.
 */
public class Engine {

    /**
     * Maximum number of tries while receiving data that did not match the instructions file
     */
    private static final int MAX_READ_RETRIES = 4;

    /**
     * Maximum number of tries while failing to extract data
     */
    private static final int MAX_EXTRACTING_RETRIES = 60;




    private Inverter inverter;

    private Request currentRequest;

    public Engine(Inverter inverter){
        this.inverter = inverter;
    }

    public Response processRequest(Request request) throws IOException, SmajavaException {
        if(currentRequest != null) throw new RuntimeException("Already processing a request");
        currentRequest = request;
        byte[] data = request.dataToSend();
        if(data != null){
            inverter.send(request.dataToSend());
        }
        Response response = getResponse();
        currentRequest = null;
        return response;
    }

    private Response getResponse() throws IOException, SmajavaException {
        return getResponse(0);
    }

    private Response getResponse(int run) throws IOException, SmajavaException {
        Response response = RequestResponseFactory.makeResponse(currentRequest.getResponseType());
        if(response instanceof RequiresInverter){
            ((RequiresInverter)response).setInverter(inverter);
        }
        try{
            ByteResult byteResult = inverter.receive();
            response.processData(byteResult.getResult());
        } catch (FaultyResponseException e){
            if (run > MAX_READ_RETRIES) {
                throw new SmajavaException("Could not receive expected data from response: "+ response);
            }
            response = getResponse(run + 1);
        }
        return response;
    }

}

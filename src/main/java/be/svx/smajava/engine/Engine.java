package be.svx.smajava.engine;

import be.geek.smajava.ByteResult;
import be.geek.smajava.Inverter;
import be.geek.smajava.SmajavaException;
import be.svx.smajava.commands.InitRequest;
import be.svx.smajava.commands.InitRequest2;
import be.svx.smajava.commands.InitResponse;
import be.svx.smajava.commands.InitResponse2;

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

    private byte[] address;

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
        Response response = makeResponse(currentRequest.getResponseType());
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

    public void open() throws IOException, SmajavaException {
        inverter.openConnection();
        //INIT
        processRequest(new InitRequest(this));
        processRequest(new InitRequest2(this));
    }

    public void close() throws IOException {
        inverter.closeConnection();
    }

    public Request makeRequest(RequestType requestType){
        Request request = null;
        switch (requestType){
            case INIT:
                request = new InitRequest(this);
                break;
            case INIT2:
                request = new InitRequest2(this);
                break;
        }
        return request;
    }


    public Response makeResponse(ResponseType responseType){
        Response response = null;
        switch (responseType){
            case INIT:
                response = new InitResponse(this);
                break;
            case INIT2:
                response = new InitResponse2(this);
                break;
        }
        return  response;
    }

    private Inverter getInverter() {
        return inverter;
    }

    public byte[] getAddress(){
        if(address == null){
            address = inverter.convertAddress();
        }
        return address;
    }

    public byte getInverterCode() {
        return inverter.getInverterCode();
    }

    public void setInverterCode(byte inverterCode) {
        inverter.setInverterCode(inverterCode);
    }

    public void setSecondAddress(byte[] address2) {
        inverter.setSecondAddress(address2);
    }

    public byte[] getSecondAddress() {
        return inverter.getSecondAddress();
    }
}

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

    public Response processRequest(Request request) throws IOException, SmajavaException, FaultyResponseException {
        if(currentRequest != null) throw new RuntimeException("Already processing a request");
        currentRequest = request;
        Packet packet = request.dataToSend();
        if(packet != null){
            inverter.send(packet.toBytes());
        }
        Response response = getResponse();
        currentRequest = null;
        return response;
    }

    private Response getResponse() throws IOException, SmajavaException, FaultyResponseException {
        Response response = makeResponse(currentRequest.getResponseType());
        ByteResult byteResult = inverter.receive();
        Packet packet = new Packet(byteResult.getResult());
        if(packet.isValid()){
            response.processData(packet);
        } else {
            throw new FaultyResponseException("Invalid packet received.");
        }
        return response;
    }

    public void open() throws IOException, SmajavaException, FaultyResponseException {
        inverter.openConnection();
        //INIT_STEP1
        processRequest(new InitRequest(this));
        processRequest(new InitRequest2(this));
    }

    public void close() throws IOException {
        inverter.closeConnection();
    }

    public Request makeRequest(RequestType requestType){
        Request request = null;
        switch (requestType){
            case INIT_STEP1:
                request = new InitRequest(this);
                break;
            case INIT_STEP2:
                request = new InitRequest2(this);
                break;
        }
        return request;
    }


    public Response makeResponse(ResponseType responseType){
        Response response = null;
        switch (responseType){
            case INIT_STEP1:
                response = new InitResponse(this);
                break;
            case INIT_STEP2:
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

package be.svx.sma.core;

import be.svx.sma.util.Util;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Stijn on 16/02/14.
 */
public class MessagingService {

    public final static byte[] DEFAULT_ANY_ADRESS = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public final static byte[] DEFAULT_ALL_ADRESS = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    public final static byte[] DEFAULT_HOST_ADRESS = DEFAULT_ANY_ADRESS;


    private List<Request> requestList;
    private List<Class<? extends Response>> expectedResponses;
    private Request lastRequest;

    //Bluetooth devices
    private RemoteDevice remoteDevice;
    private LocalDevice localDevice;
    private byte[] remoteAddress;
    private byte[] localAddress;

    //Inverter data
    private String serialNumber;
    private byte[] serial;
    private byte inverterCode;

    //Internal Connection
    private StreamConnection connection;
    private InputStream inputStream;
    private OutputStream outputStream;

    private List<MessageHandler> messageHandlers;

    public MessagingService(LocalDevice localDevice, RemoteDevice remoteDevice, boolean async) {
        requestList = new ArrayList<Request>();
        expectedResponses = new ArrayList<Class<? extends Response>>();
        messageHandlers = new ArrayList<MessageHandler>();
        lastRequest = null;
        this.remoteDevice = remoteDevice;
        this.localDevice = localDevice;
        this.remoteAddress = Util.convertAddressToBytes(remoteDevice.getBluetoothAddress());
        this.localAddress = Util.convertAddressToBytes(localDevice.getBluetoothAddress());
        try {
            String deviceName = remoteDevice.getFriendlyName(false);
            String SN = deviceName.substring(deviceName.length() - 12);
            if(SN.startsWith("SN")) {
                serialNumber = SN.substring(2);
                serial = Util.convertAddressToBytes(serialNumber);
            } else {
                serialNumber = "UNKNOWN";
                serial = new byte[0];
            }
        } catch (IOException e) {
            throw new SMAException("Failed retrieving Remote Device data", e);
        }
    }

    public void open(){
        try {
            connection = (StreamConnection) Connector.open("btspp://" + remoteDevice.getBluetoothAddress() + ":1;authenticate=false;encrypt=false;master=false");
            this.remoteDevice = RemoteDevice.getRemoteDevice(connection); //Refetch the RemoteDevice just to be sure!
            inputStream = connection.openInputStream();
            outputStream = connection.openOutputStream();
        } catch (IOException e) {
            throw new SMAException("Can't open connection", e);
        }
        //Retrieve the login packet
        SMAPacket packet;

        boolean lastPacket = false;
        do{
            packet = receive();
            if(packet.isValid()){
                if(packet instanceof LoginRequest) {
                    LoginRequest loginRequest = (LoginRequest)packet;
                    inverterCode = loginRequest.getInverterCode();
                    loginRequest.setDestination(remoteAddress);
                    loginRequest.setSource(localAddress);
                    send(loginRequest);
                } else {
                    lastPacket = packet instanceof Login3Response;
                    /*if(packet instanceof Login1Response){
                        //Do something?
                    } else if(packet instanceof Login2Response){
                        //Do something?
                    } else if(packet instanceof Login3Response) {
                        lastPacket = true;
                        //Do something?
                    } */
                }
            }
        }while(!lastPacket);
        handleEvent((Response)packet);
    }

    public void close(){
        try {
            inputStream.close();
            outputStream.close();
            connection.close();
        } catch (IOException e) {
            //ignore
        }
        inputStream = null;
        outputStream = null;
        connection = null;
    }

    private void send(SMAPacket packet) {
        byte[] data = packet.getData();
        Util.printLogPacket(packet, "OUT");
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            throw new SMAException(e.getMessage(), e);
        }
    }

    private SMAPacket receive() {
        SMAPacket packet;
        int size = 0x6D;
        final byte[] content = new byte[size];
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Integer> callable = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return inputStream.read(content);
            }
        };

        Future<Integer> future = executor.submit(callable);
        try {
            int read = future.get(5000, TimeUnit.MILLISECONDS);
            byte[] data =  Arrays.copyOf(content, read);
            packet = SMAPacket.createPacket(data);
            Util.printLogPacket(packet, "IN");
        } catch (InterruptedException ex) {
            packet = null;
        } catch (ExecutionException ex) {
            packet = null;
        } catch (TimeoutException ex) {
            packet = null;
        }
        return packet;
    }

    private void handleEvent(Response response){
        if(response != null){
            for(MessageHandler messageHandler : messageHandlers){
                messageHandler.processReponse(response);
            }
        }
    }

    public void addEventHandler(MessageHandler messageHandler){
        messageHandlers.add(messageHandler);
    }

    public void doRequest(Request request){
        if(request.isInternal()){
            throw new SMAException("You can't do internal Requests using this method!");
        }
        List<Class> expectedResponses = request.responseClass();
        send((SMAPacket)request);
        Response lastResponse = null;
        while(!expectedResponses.isEmpty()){
            SMAPacket smaPacket = receive();
            lastResponse = (Response) smaPacket;
            Class rClass = expectedResponses.remove(0);
            if(!rClass.equals(smaPacket.getClass())){
              throw new SMAException("Expected " + rClass.toString() + " got " + smaPacket.getClass().toString());
            }
        }
        handleEvent(lastResponse);
    }

    public byte[] getLocalAddress() {
        return localAddress.clone();
    }

    public byte[] getRemoteAddress() {
        return remoteAddress.clone();
    }
}

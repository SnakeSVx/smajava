package be.svx.sma;

import be.geek.smajava.Log;
import be.svx.sma.protocol.Packet;
import be.svx.sma.core.SMAException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.*;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Created by Stijn on 7/02/14.
 */
public class InverterService {

    public static int MAX_RETRIES = 4;
    public final static byte[] DEFAULT_HOST_ADRESS = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    /**
     * RFCOMM url
     */
    private static final String URLTemplate = "btspp://%s:1;authenticate=false;encrypt=false;master=false";

    //Internal Connection
    private StreamConnection connection;
    private InputStream inputStream;
    private OutputStream outputStream;

    //Data required to make the connection
    private String address;
    private String pin;
    //TODO is type needed?
    private String type;

    private String url;


    //Retrieved/Calculated values
    private byte[] addressBytes;
    private byte inverterCode;
    private byte[] serial;
    private byte[] hostAddressBytes = DEFAULT_HOST_ADRESS;


    private void printLogPacket(Packet packet, String info){
        Log.info(this, "FMT \t" + packet.getFormat());
        Log.debugBytes(this, info + "\t", packet.getPacket());
    }

    public InverterService(String address, String type, String pin){
        this.address = address;
        this.pin = pin;
        this.type = type;
        this.url = URLTemplate.replaceFirst("%s", address);

        this.addressBytes = Util.convertAddressToBytes(address);
    }

    public void open(){
        try {
            connection = (StreamConnection) Connector.open(url);
            inputStream = connection.openInputStream();
            outputStream = connection.openOutputStream();
        } catch (IOException e) {
            throw new SMAException("Can't open connection", e);
        }
        //Retrieve the login packet
        Packet packet;

        boolean lastPacket = false;
        do{
            packet = receive();
            if(packet.isValid()){
                if(packet.isCommand(Packet.LOGIN_REQUEST_COMMAND)){
                    inverterCode = packet.getByte(4);
                    Log.debugBytes(this, "Inverter code: ", new byte[]{inverterCode});
                    //We have to send the same packet back as reply, but with the source/destination address modified
                    packet.setSource(DEFAULT_HOST_ADRESS);
                    packet.setDestination(addressBytes);
                    send(packet);
                }else if(packet.isCommand(Packet.LOGIN_PART1_COMMAND)){

                }else if(packet.isCommand(Packet.LOGIN_PART2_COMMAND)){

                }else if(packet.isCommand(Packet.LOGIN_PART3_COMMAND)){
                    hostAddressBytes = new byte[6];
                    hostAddressBytes[0] = packet.getByte(8);
                    hostAddressBytes[1] = packet.getByte(9);
                    hostAddressBytes[2] = packet.getByte(10);
                    hostAddressBytes[3] = packet.getByte(11);
                    hostAddressBytes[4] = packet.getByte(12);
                    hostAddressBytes[5] = packet.getByte(13);
                    lastPacket = true;
                    Log.debugBytes(this, "Hostadress: ", hostAddressBytes);
                }
            }
        }while(!lastPacket);
    }

    public void sendRequest1(){
        Packet packet = new Packet();
        packet.setDestination(addressBytes);
        packet.setCommand(Packet.REQUEST_INFORMATION_COMMAND);
        packet.setContent(new byte[]{0x01, 0x00, 0x01});
        send(packet);

        packet = receive();
        if(packet != null && packet.isValid() && packet.isCommand(Packet.RESPONSE_INFORMATION_COMMAND)){
            Log.info(this, "Response 1: ", Arrays.toString(packet.getPacket()));
        }
    }

    public void sendRequest2(){
        Packet packet = new Packet();
        packet.setDestination(addressBytes);
        packet.setCommand(Packet.REQUEST_INFORMATION_COMMAND);
        packet.setContent(new byte[]{(byte)0x02, 0x00});
        send(packet);

        packet = receive();
        if(packet != null && packet.isValid() && packet.isCommand(Packet.RESPONSE_INFORMATION_COMMAND)){
            Log.info(this, "Response 2: ", Arrays.toString(packet.getPacket()));
        }
    }

    public double getSignalStrength(){
        Packet packet = new Packet();
        packet.setDestination(addressBytes);
        packet.setSource(DEFAULT_HOST_ADRESS);
        packet.setCommand(Packet.REQUEST_INFORMATION_COMMAND);
        packet.setContent(new byte[]{0x05, 0x00});
        send(packet);
        double reception = 0;
        packet = receive();
        if(packet != null && packet.isValid() && packet.isCommand(Packet.RESPONSE_INFORMATION_COMMAND) && packet.getByte(0) == 0x05 && packet.getByte(1) == 0x00){
            byte strength = packet.getByte(4);
            reception = ((float)(strength & 0xFF)/(float)0xFF)*100;
        }
        return reception;
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

    private void send(Packet packet) {
        byte[] data = packet.getPacket();
        printLogPacket(packet, "OUT");
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            throw new SMAException(e.getMessage(), e);
        }
    }

    private Packet receive() {
        Packet packet;
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
            packet = new Packet(data);
            printLogPacket(packet, "IN");
        } catch (InterruptedException ex) {
            packet = null;
        } catch (ExecutionException ex) {
            packet = null;
        } catch (TimeoutException ex) {
            packet = null;
        }
        return packet;

        /*int read = 1;


        // header is always 3 bytes
        final byte[] header = new byte[3];

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Integer> callable = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return inputStream.read(header);
            }
        };

        read = Util.readForCallable(executor, callable);


        final int contentlength = (new Byte(header[1])).intValue();
        Log.debug(this, "Content length is " + contentlength + " bytes.");

        // construct byte array to receive data in
        final byte[] result = new byte[contentlength];
        result[0] = header[0];
        result[1] = header[1];
        result[2] = header[2];
        Log.debug(this, "Receiving...");


        executor = Executors.newFixedThreadPool(2);

        callable = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                return inputStream.read(result, 3, contentlength - 3);
            }
        };

        read = Util.readForCallable(executor, callable);

        //read = inputStream.read(result, 3, contentlength - 3);
        read+=3;
        Log.debug(this, "Read "+read+" bytes.");
        Log.debugBytes(this, "received: ", result);
        ByteResult byteResult = new ByteResult();
        if (result[result.length-1] == 0x7e) {
            byteResult.setTerminated(true);
        }
        byte[] tempBuffer = new byte[read];
        int cntr=0;
        for (int i = 0; i < read; i++) {
            if (result[i] == 0x7d && (cntr > 2)) { //did we receive the escape char
                switch (result[i + 1]) {   // act depending on the char after the escape char

                    case 0x5e:
                        tempBuffer[cntr] = 0x7e;
                        break;

                    case 0x5d:
                        tempBuffer[cntr] = 0x7d;
                        break;

                    default:
                        tempBuffer[cntr] = (byte) (result[i + 1] ^ 0x20);
                        break;
                }
                i++;
            } else {
                tempBuffer[cntr] = result[i];
            }
            cntr++;
        }
        tempBuffer = Util.addToBuffer(new byte[cntr], tempBuffer, 0);
        tempBuffer = Util.fix_length_received(tempBuffer, cntr);
        byteResult.setResult(tempBuffer);
        return new Packet(byteResult.getResult()); */
    }

}

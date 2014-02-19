package be.svx.sma;

import be.svx.sma.protocol.Packet;
import be.svx.sma.protocol.PacketLevel;
import be.svx.sma.core.SMAException;
import be.svx.sma.util.Log;
import be.svx.sma.util.Util;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stijn on 7/02/14.
 */
public class InverterServiceWithThreads {

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

    //threads
    private boolean running;
    private Thread incomingThread;
    private Thread outgoingThread;
    private Thread processingThread;


    //Retrieved/Calculated values
    private byte[] addressBytes;
    private byte inverterCode;
    private byte[] serial;
    private byte[] hostAddressBytes = DEFAULT_HOST_ADRESS;


    //Packets

    private List<Packet> incomingPackets;
    private List<Packet> outgoingPackets;
    private boolean initComplete = false;


    public InverterServiceWithThreads(String address, String type, String pin){
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


        running = true;

        incomingPackets = new ArrayList<Packet>();
        outgoingPackets = new ArrayList<Packet>();

        incomingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(running){
                    if(initComplete){
                        Packet packet = receive();
                        incomingPackets.add(packet);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new SMAException("Thread sleep failed: incoming request!");
                    }
                }
            }
        });

        outgoingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(running){
                    if(initComplete){
                        if(!outgoingPackets.isEmpty()){
                            Packet packet = outgoingPackets.remove(0);
                            if(packet != null){
                                send(packet);
                            }
                        } else {
                            getSignalStrength();
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new SMAException("Thread sleep failed: incoming request!");
                    }
                }
            }
        });

        processingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running){
                    if(!incomingPackets.isEmpty()){
                        Packet packet = incomingPackets.remove(0);
                        if(packet != null && packet.isValid()){
                           /*if(packet.isCommand(Packet.LOGIN_REQUEST_COMMAND)){
                               Log.info(this, "Login Request");
                               inverterCode = packet.getByte(4);
                               //We have to send the same packet back as reply, but with the source/destination address modified
                               packet.setSource(DEFAULT_HOST_ADRESS);
                               packet.setDestination(addressBytes);
                               outgoingPackets.add(packet);
                           } else if(packet.isCommand(Packet.LOGIN_PART1_COMMAND) || packet.isCommand(Packet.LOGIN_PART2_COMMAND)){
                               Log.info(this, "Login Part 1/Login Part 2");
                               //Do nothing
                           } else if(packet.isCommand(Packet.LOGIN_PART3_COMMAND)){
                               Log.info(this, "Login Part 3");
                               initComplete = true;
                               hostAddressBytes[0] = packet.getByte(8);
                               hostAddressBytes[1] = packet.getByte(9);
                               hostAddressBytes[2] = packet.getByte(10);
                               hostAddressBytes[3] = packet.getByte(11);
                               hostAddressBytes[4] = packet.getByte(12);
                               hostAddressBytes[5] = packet.getByte(13);
                           } else*/
                            if(packet.isCommand(Packet.RESPONSE_INFORMATION_COMMAND)){
                                //TODO check type and process
                                if(packet.getByte(0) == 0x05 && packet.getByte(1) == 0x00){
                                    byte strength = packet.getByte(4);
                                    double reception = (strength/255f)*100;
                                    Log.info(this, "Signal Strength " + reception);
                                }
                            } else if(packet.getLevel() == PacketLevel.LEVEL2){
                                Log.info(this, "Found level 2 packet!");
                            }
                        }
                    }
                }
            }
        });



        //Start init process


        //Retrieve the login packet
        Packet packet = receive();
        if(packet.isValid() && packet.isCommand(Packet.LOGIN_REQUEST_COMMAND)){
            inverterCode = packet.getByte(4);
            //We have to send the same packet back as reply, but with the source/destination address modified
            packet.setSource(DEFAULT_HOST_ADRESS);
            packet.setDestination(addressBytes);
            send(packet);
        } else {
            throw new SMAException("We didn't get the Login Request we expected!");
        }

        packet = receive();
        if(!(packet.isValid() && packet.isCommand(Packet.LOGIN_PART1_COMMAND))){
            //Arrays.toString(packet.getContent());
            //} else {
            throw new SMAException("We didn't get the Login Response we expected!");
        }

        packet = receive();
        if(!(packet.isValid() && packet.isCommand(Packet.LOGIN_PART2_COMMAND))){
            //Arrays.toString(packet.getContent());
            //} else {
            throw new SMAException("We didn't get the Login Response 2 we expected!");
        }

        packet = receive();
        if(packet.isValid() && packet.isCommand(Packet.LOGIN_PART3_COMMAND)){
            initComplete = true;
            hostAddressBytes[0] = packet.getByte(8);
            hostAddressBytes[1] = packet.getByte(9);
            hostAddressBytes[2] = packet.getByte(10);
            hostAddressBytes[3] = packet.getByte(11);
            hostAddressBytes[4] = packet.getByte(12);
            hostAddressBytes[5] = packet.getByte(13);
        } else {
            throw new SMAException("We didn't get the Login Response 2 we expected!");
        }

        incomingThread.start();
        outgoingThread.start();
        processingThread.start();


    }

    public void checkConnection(){
        Packet packet = new Packet();
        packet.setDestination(addressBytes);
        packet.setSource(DEFAULT_HOST_ADRESS);
        packet.setCommand(Packet.REQUEST_INFORMATION_COMMAND);
        packet.setContent(new byte[]{0x02, 0x00});
        outgoingPackets.add(packet);
    }

    public void getSignalStrength(){
        Packet packet = new Packet();
        packet.setDestination(addressBytes);
        packet.setSource(DEFAULT_HOST_ADRESS);
        packet.setCommand(Packet.REQUEST_INFORMATION_COMMAND);
        packet.setContent(new byte[]{0x05, 0x00});
        outgoingPackets.add(packet);
    }

    public void close(){
        running = false;
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
        Log.debugBytes(this, "Sending data: ", data);
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            throw new SMAException(e.getMessage(), e);
        }
    }

    private Packet receive() {
        //int size = 0x6D;
        byte[] content;
        int read = 0;
        try {
            byte start = (byte)inputStream.read();
            int size = inputStream.read();
            content = new byte[size];
            content[0] = start;
            content[1] = (byte)size;
            read = inputStream.read(content, 2, size-2);
        } catch (IOException e) {
            throw new SMAException("Error reading bytes.", e);
        }
        return new Packet(content);

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

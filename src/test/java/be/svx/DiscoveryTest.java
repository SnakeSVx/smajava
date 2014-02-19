package be.svx;

import be.svx.sma.util.Log;
import be.svx.sma.util.Util;
import be.svx.sma.core.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by Stijn on 13/02/14.
 */
@RunWith(JUnit4.class)
public class DiscoveryTest implements DiscoveryListener, MessageHandler {

    //object used for waiting
    private static Object lock=new Object();

    //vector containing the devices discovered
    private static Vector vecDevices=new Vector();

    @Test
    public void testDisovery() throws IOException {
        Log.initLogging();

        DiscoveryTest client = new DiscoveryTest();

        //display local device address and name
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: "+localDevice.getBluetoothAddress());
        System.out.println("Name: "+localDevice.getFriendlyName());

        //find devices
        DiscoveryAgent agent = localDevice.getDiscoveryAgent();

        System.out.println("Starting device inquiry...");
        agent.startInquiry(DiscoveryAgent.GIAC, client);

        try {
            synchronized(lock){
                lock.wait();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println("Device Inquiry Completed. ");

        //check for spp service
        RemoteDevice remoteDevice=(RemoteDevice)vecDevices.elementAt(0);

        System.out.println("Address: "+remoteDevice.getBluetoothAddress());
        System.out.println("Name: "+remoteDevice.getFriendlyName(false));

        String deviceName = remoteDevice.getFriendlyName(false);
        String SN = deviceName.substring(deviceName.length() - 12);
        System.out.println("Serial: " + SN.substring(2));
        Log.debugBytes(this, "Serial in bytes: ", Util.convertAddressToBytes(SN.substring(2)));

        MessagingService service = new MessagingService(localDevice, remoteDevice, false);
        service.addEventHandler(this);
        service.open();
        Log.info(this, "Sending basic 1 request");
        service.doRequest(new Basic1Request(service.getRemoteAddress()));
        Log.info(this, "Sending basic 2 request");
        service.doRequest(new Basic2Request(service.getRemoteAddress()));
        Log.info(this, "Sending signal strength request");
        service.doRequest(new SignalStrengthRequest(service.getRemoteAddress()));
        service.close();
    }

    @Override
    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        System.out.println("Device Discovered!" + btDevice.getBluetoothAddress());
        //add the device to the vector
        if(!vecDevices.contains(btDevice)){
            vecDevices.addElement(btDevice);
        }
    }

    @Override
    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void inquiryCompleted(int discType) {
        //TODO not used!!
        System.out.println("Inquiry Discovered!");
        synchronized(lock){
            lock.notify();
        }
    }

    @Override
    public void processReponse(Response response) {
        if(response instanceof Login3Response){
            System.out.println("Initialization successfull");
        } else if(response instanceof SignalStrengthResponse){
            System.out.println("Signal Strength: "  + ((SignalStrengthResponse)response).getSignalStrength());
        } else {
            System.out.println("Got Event: " + response.getClass());
        }
    }
}

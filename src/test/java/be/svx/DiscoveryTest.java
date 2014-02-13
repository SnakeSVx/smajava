package be.svx;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by Stijn on 13/02/14.
 */
@RunWith(JUnit4.class)
public class DiscoveryTest implements DiscoveryListener {

    //object used for waiting
    private static Object lock=new Object();

    //vector containing the devices discovered
    private static Vector vecDevices=new Vector();

    private static String connectionURL=null;

    @Test
    public void testDisovery() throws IOException {

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


        StreamConnection connection = (StreamConnection) Connector.open("btspp://" + remoteDevice.getBluetoothAddress() + ":1;authenticate=false;encrypt=false;master=false");

        remoteDevice = RemoteDevice.getRemoteDevice(connection);

        System.out.println("Address: "+remoteDevice.getBluetoothAddress());
        System.out.println("Name: "+remoteDevice.getFriendlyName(false));

       /* UUID[] uuidSet = new UUID[1];
       // uuidSet[0]=new UUID("4e3aea40e2a511e095720800200c9a66", false);

        System.out.println("\nSearching for service...");
        agent.searchServices(null,uuidSet,remoteDevice,client);

        try {
            synchronized(lock){
                lock.wait();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(connectionURL==null){
            System.out.println("Device does not support Simple SPP Service.");
            System.exit(0);
        } */
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
        //TODO not used!!!
        System.out.println("Service Discovered!");
        System.out.println(servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));
        System.out.println(servRecord[0].getConnectionURL(ServiceRecord.AUTHENTICATE_NOENCRYPT, false));
        System.out.println(servRecord[0].getConnectionURL(ServiceRecord.AUTHENTICATE_ENCRYPT, false));
        if(servRecord!=null && servRecord.length>0){
            connectionURL=servRecord[0].getConnectionURL(ServiceRecord.AUTHENTICATE_ENCRYPT,false);
        }
        synchronized(lock){
            lock.notify();
        }
    }

    @Override
    public void serviceSearchCompleted(int transID, int respCode) {
        //TODO not used!!
        System.out.println("Service Seearch Completed!");
        synchronized(lock){
            lock.notify();
        }
    }

    @Override
    public void inquiryCompleted(int discType) {
        //TODO not used!!
        System.out.println("Inquiry Discovered!");
        synchronized(lock){
            lock.notify();
        }
    }
}

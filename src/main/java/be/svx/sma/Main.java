package be.svx.sma;

import be.geek.smajava.Log;
import be.svx.sma.protocol.SMAException;

import java.io.IOException;

//These are the requests that should work: https://code.google.com/r/janus44444-sma-bluetooth/source/browse/sma2plus-sb5000tl-session-analysis.log?spec=svn102508ced71b4c8aed5b0fc827d4c649ebb45a25&r=102508ced71b4c8aed5b0fc827d4c649ebb45a25
/**
 * Created by Stijn on 7/02/14.
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Log.initLogging();

        String address = "008025A66FD7";
        String type = "";
        String pin = "0000";

        InverterService inverterService = new InverterService(address, type, pin);
        try{
            inverterService.open();
            inverterService.sendRequest1();
            inverterService.sendRequest2();
            double signal = -1;
            for(int i = 0; i < 4; i++){
                signal = inverterService.getSignalStrength();
            }
            Log.info(inverterService, "Signal Strength: " + signal);
        } catch (SMAException e){
            Log.error(inverterService, "Something went wrong during INIT" , e);
        } finally {
            inverterService.close();
        }


    }
}

package be.svx.smajava.commands;

import be.geek.smajava.Inverter;
import be.svx.smajava.FaultyResponseException;
import be.svx.smajava.RequiresInverter;
import be.svx.smajava.Response;
import be.svx.smajava.ResponseType;

import java.util.Arrays;

/**
 * Created by Stijn on 5/02/14.
 */
public class StartResponse implements Response, RequiresInverter {

    private String[] commands = new String[]{
            "7E 1F 00 61 $ADDR 00 00 00 00 00 00 02 00 00 04 70 00 $END;",
            "E $INVCODE $END;"
    };

    private Inverter inverter;


    @Override
    public ResponseType getResponseType() {
        return ResponseType.START;
    }

    @Override
    public void processData(byte[] bytes) throws FaultyResponseException {

         System.out.println("7E: " + (0x7E == bytes[0]));
         System.out.println("1F: " + (0x1F == bytes[1]));
         System.out.println("00: " + (0x00 == bytes[2]));
         System.out.println("61: " + (0x61 == bytes[3]));
         System.out.println("Process adress");
         byte[] ADDR = inverter.convertAddress();
         for(int i = 0; i < ADDR.length; i++){
             System.out.println(ADDR[i] + " = " + bytes[4 + i] + ": " + (ADDR[i] == bytes[4 + i]));
         }
         System.out.println("End Process adress");

         int base = 3 + ADDR.length;
         System.out.println("00: " + (0x00 == bytes[base + 1]));
         System.out.println("00: " + (0x00 == bytes[base + 2]));
         System.out.println("00: " + (0x00 == bytes[base + 3]));
         System.out.println("00: " + (0x00 == bytes[base + 4]));
         System.out.println("00: " + (0x00 == bytes[base + 5]));
         System.out.println("00: " + (0x00 == bytes[base + 6]));
         System.out.println("02: " + (0x02 == bytes[base + 7]));
         System.out.println("00: " + (0x00 == bytes[base + 8]));
         System.out.println("00: " + (0x00 == bytes[base + 9]));
         System.out.println("04: " + (0x04 == bytes[base + 10]));
         System.out.println("70: " + (0x70 == bytes[base + 11]));
         System.out.println("00: " + (0x00 == bytes[base + 12]));

         if(bytes.length > base + 12){
             for(int i = base + 13; i < bytes.length; i++){
                 System.out.println("EXTRA: " + Integer.toHexString(bytes[i]));
             }
         }


    }

    @Override
    public void setInverter(Inverter inverter) {
        this.inverter = inverter;
    }
}

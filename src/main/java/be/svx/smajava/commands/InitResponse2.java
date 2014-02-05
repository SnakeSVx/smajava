package be.svx.smajava.commands;

import be.svx.smajava.engine.Engine;
import be.svx.smajava.engine.FaultyResponseException;
import be.svx.smajava.engine.Response;
import be.svx.smajava.engine.ResponseType;

import java.util.Arrays;

/**
 * Created by Stijn on 5/02/14.
 */
public class InitResponse2 extends Response {

    private String[] commands = new String[]{
            "7E 22 00 5C $ADDR 00 00 00 00 00 00 05 00 $ADDR $END",
            "E $ADD2 $END;"
    };

    public InitResponse2(Engine engine) {
        super(engine);
    }


    @Override
    public ResponseType getResponseType() {
        return ResponseType.INIT;
    }

    private byte[] generateVerifictionBytes(){
        byte[] ADDR = getEngine().getAddress();
        byte[] bytes = new byte[4 + ADDR.length + 8 + ADDR.length];
        bytes[0] = 0x7e;
        bytes[1] = 0x22;
        bytes[2] = 0x00;
        bytes[3] = 0x5c;
        for(int i = 0; i < ADDR.length; i++){
            bytes[i+4] = ADDR[i];
        }
        int base = 3 + ADDR.length;
        bytes[base + 1] = 0x00;
        bytes[base + 2] = 0x00;
        bytes[base + 3] = 0x00;
        bytes[base + 4] = 0x00;
        bytes[base + 5] = 0x00;
        bytes[base + 6] = 0x00;
        bytes[base + 7] = 0x05;
        bytes[base + 8] = 0x00;
        base = base + 9 ;
        for(int i = 0; i < ADDR.length; i++){
            bytes[i+base] = ADDR[i];
        }
        return bytes;
    }

    private int verifyStartOfRequest(byte[] bytes) throws FaultyResponseException {
        byte[] verification = generateVerifictionBytes();
        for(int i = 0; i < verification.length ; i++){
            if(verification[i] != bytes[i]){
                throw new FaultyResponseException("Invalid data for byte " + i + ". Expected " + verification[i] + " got " + bytes[i]);
            }
        }
        return verification.length;
    }

    @Override
    public void processData(byte[] bytes) throws FaultyResponseException {

         int next = verifyStartOfRequest(bytes);
         for(int i = next; i < 26; i++){
             System.out.println("EXTRA2: " + Integer.toHexString(bytes[i]));
         }

        byte[] address2 = Arrays.copyOfRange(bytes, 26, 32);
        getEngine().setSecondAddress(address2);

        next = 33;

         if(bytes.length > next){
             for(int i = next; i < bytes.length; i++){
                 System.out.println("EXTRA2: " + Integer.toHexString(bytes[i]));
             }
         }


    }

}

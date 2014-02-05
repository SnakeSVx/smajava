package be.svx.smajava.commands;

import be.svx.smajava.engine.FaultyResponseException;
import be.svx.smajava.engine.ResponseType;
import be.svx.smajava.engine.Engine;
import be.svx.smajava.engine.Response;

/**
 * Created by Stijn on 5/02/14.
 */
public class InitResponse extends Response {

    private String[] commands = new String[]{
            "7E 1F 00 61 $ADDR 00 00 00 00 00 00 02 00 00 04 70 00 $END;",
            "E $INVCODE $END;"
    };

    public InitResponse(Engine engine) {
        super(engine);
    }


    @Override
    public ResponseType getResponseType() {
        return ResponseType.INIT;
    }

    private byte[] generateVerifictionBytes(){
        byte[] ADDR = getEngine().getAddress();
        byte[] bytes = new byte[4 + ADDR.length + 12];
        bytes[0] = 0x7e;
        bytes[1] = 0x1F;
        bytes[2] = 0x00;
        bytes[3] = 0x61;
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
        bytes[base + 7] = 0x02;
        bytes[base + 8] = 0x00;
        bytes[base + 9] = 0x00;
        bytes[base + 10] = 0x04;
        bytes[base + 11] = 0x70;
        bytes[base + 12] = 0x00;

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
         byte invertercode = bytes[next];

         getEngine().setInverterCode(invertercode);

         next = next + 1;

         if(bytes.length > next){
             for(int i = next; i < bytes.length; i++){
                 System.out.println("EXTRA: " + Integer.toHexString(bytes[i]));
             }
         }


    }

}

package be.svx.smajava.commands;

import be.geek.smajava.Log;
import be.svx.smajava.engine.*;

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
        return ResponseType.INIT_STEP1;
    }

    private byte[] generateVerifictionBytes(){
        byte[] ADDR = getEngine().getAddress();
        byte[] bytes = new byte[4];
        bytes[0] = 0x00;
        bytes[1] = 0x04;
        bytes[2] = 0x70;
        bytes[3] = 0x00;
        return bytes;
    }

    private int verifyContents(byte[] bytes) throws FaultyResponseException {
        byte[] verification = generateVerifictionBytes();
        for(int i = 0; i < verification.length ; i++){
            if(verification[i] != bytes[i]){
                throw new FaultyResponseException("Invalid data for byte " + i + ". Expected " + verification[i] + " got " + bytes[i]);
            }
        }
        return verification.length;
    }

    /**
     * R 7E 1F 00 61 $ADDR 00 00 00 00 00 00 02 00 00 04 70 00 $END;
     * E $INVCODE $END;
     *
     * @param packet the packet received
     * @throws FaultyResponseException
     */
    @Override
    public void processData(Packet packet) throws FaultyResponseException {
        Log.info(this, "Process Response " + getResponseType().name());
        Log.info(this, "CommandCode: " + packet.getCommandCode());  // 2

         int next = verifyContents(packet.getContent());
         byte invertercode = packet.getContent()[next];

         getEngine().setInverterCode(invertercode);
         Log.info(this, "Received invercode: " + Integer.toHexString(invertercode));

         next = next + 1;

         if(packet.getContent().length > next){
             for(int i = next; i < packet.getContent().length; i++){
                 Log.info(this, "Extra Data received: " + Integer.toHexString(packet.getContent()[i]));
             }
         }


    }

}

package be.svx.smajava.commands;

import be.geek.smajava.Log;
import be.svx.smajava.engine.*;

import java.io.UnsupportedEncodingException;
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
        return ResponseType.INIT_STEP1;
    }

    private byte[] generateVerifictionBytes(){
       return getEngine().getAddress();
    }

    private int verifyRequestContent(byte[] bytes) throws FaultyResponseException {
        byte[] verification = generateVerifictionBytes();
        for(int i = 0; i < verification.length ; i++){
            if(verification[i] != bytes[i]){
                throw new FaultyResponseException("Invalid data for byte " + i + ". Expected " + verification[i] + " got " + bytes[i]);
            }
        }
        return verification.length;
    }

    /**
     *
     * R 7E 22 00 5C $ADDR 00 00 00 00 00 00 05 00 $ADDR $END;
     * E $ADD2 $END;
     *
     * @param packet
     * @throws FaultyResponseException
     */
    @Override
    public void processData(Packet packet) throws FaultyResponseException {
         Log.info(this, "Process Response " + getResponseType().name());
         Log.info(this, "CommandCode: " + packet.getCommandCode());  //10

         int next = verifyRequestContent(packet.getContent());
         /*for(int i = next; i < 8; i++){
             Log.info(this, "Extra Data received: " + Integer.toHexString(packet.getContent()[i]));
         }*/
        //Klopt dit??
        byte[] address2 = Arrays.copyOfRange(packet.getContent(), 6, 12);

        try {
            Log.info(this, "Received alternate adress: " + new String(address2 ,"UTF-8"));
            for(byte b : address2){
                Log.info(this, "Adress part: " + Integer.toHexString(b));
            }
        } catch (UnsupportedEncodingException e) {
            Log.info(this, "Couldn't print alternate adress");
        }

        getEngine().setSecondAddress(address2);

        next = 15;

         if(packet.getContent().length > next){
             for(int i = next; i < packet.getContent().length; i++){
                 Log.info(this, "Extra Data received: " + Integer.toHexString(packet.getContent()[i]));
             }
         }


    }

}

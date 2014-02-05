package be.svx.smajava.commands;


import be.svx.smajava.engine.RequestType;
import be.svx.smajava.engine.ResponseType;
import be.svx.smajava.engine.Engine;
import be.svx.smajava.engine.Request;

/**
 * Created by Stijn on 5/02/14.
 */
public class InitRequest2 extends Request {

    public InitRequest2(Engine engine) {
        super(engine);
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.INIT2;
    }

    @Override
    public ResponseType getResponseType() {
        return ResponseType.INIT2;
    }

    @Override
    public byte[] dataToSend() {
        byte[] ADDR = getEngine().getAddress();
        byte[] bytes = new byte[10 + ADDR.length + 6 + 1 + 8];
        bytes[0] = 0x7E;
        bytes[1] = 0x1F;
        bytes[2] = 0x00;
        bytes[3] = 0x61;
        bytes[4] = 0x00;
        bytes[5] = 0x00;
        bytes[6] = 0x00;
        bytes[7] = 0x00;
        bytes[8] = 0x00;
        bytes[9] = 0x00;
        for(int i = 0; i < ADDR.length; i++){
            bytes[10 + i] = ADDR[i];
        }
        int base = 9 + ADDR.length;
        bytes[base + 1] = 0x02;
        bytes[base + 2] = 0x00;
        bytes[base + 3] = 0x00;
        bytes[base + 4] = 0x04;
        bytes[base + 5] = 0x70;
        bytes[base + 6] = 0x00;

        bytes[base + 7] = getEngine().getInverterCode();

        bytes[base + 8] = 0x00;
        bytes[base + 9] = 0x00;
        bytes[base + 10] = 0x00;
        bytes[base + 11] = 0x00;
        bytes[base + 12] = 0x01;
        bytes[base + 13] = 0x00;
        bytes[base + 14] = 0x00;
        bytes[base + 15] = 0x00;

        return bytes;
    }
}

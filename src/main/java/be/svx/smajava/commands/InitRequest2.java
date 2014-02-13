package be.svx.smajava.commands;


import be.svx.smajava.engine.*;

/**
 * Created by Stijn on 5/02/14.
 */
public class InitRequest2 extends Request {

    public InitRequest2(Engine engine) {
        super(engine);
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.INIT_STEP2;
    }

    @Override
    public ResponseType getResponseType() {
        return ResponseType.INIT_STEP2;
    }

    /**
     * S 7E 1F 00 61 00 00 00 00 00 00 $ADDR 02 00 00 04 70 00 $INVCODE 00 00 00 00 01 00 00 00 $END;
     *
     * @return the bytes to be send
     */
    @Override
    public Packet dataToSend() {
        Packet packet = new Packet();
        packet.setDestination(getEngine().getAddress());

        byte[] command = new byte[2]; //2
        command[0] = 0x02;  //Command code byte 1:
        command[1] = 0x00;  //Command code byte 2:
        packet.setCommand(command);

        byte[] content = new byte[13];
        content[0] = 0x00;
        content[1] = 0x04;
        content[2] = 0x70;
        content[3] = 0x00;

        content[4] = getEngine().getInverterCode();

        content[5] = 0x00;
        content[6] = 0x00;
        content[7] = 0x00;
        content[8] = 0x00;
        content[9] = 0x01;
        content[10] = 0x00;
        content[11] = 0x00;
        content[12] = 0x00;

        packet.setContent(content);
        return packet;
    }
}

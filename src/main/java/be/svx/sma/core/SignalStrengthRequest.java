package be.svx.sma.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stijn on 16/02/14.
 */
public class SignalStrengthRequest extends SMAPacket implements Request {

    public SignalStrengthRequest(byte[] destination) {
        header[16] = 0x03;
        header[17] = 0x00;

        content = new byte[]{0x05, 0x00};
        setDestination(destination);
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public List<Class> responseClass() {
        List<Class> responses = new ArrayList<Class>();
        responses.add(SignalStrengthResponse.class);
        return responses;
    }

    @Override
    public String getPacketFormat() {
        return "7e 14 00 6a 00 00 00 00 00 00 DA DA DA DA DA DA 03 00 05 00";
    }
}

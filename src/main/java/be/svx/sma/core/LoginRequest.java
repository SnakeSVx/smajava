package be.svx.sma.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stijn on 16/02/14.
 */
public class LoginRequest extends SMAPacket implements Response, Request {

    public LoginRequest(byte[] data) {
        super(data);
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getPacketFormat() {
        return "7E 1F 00 61 SA SA SA SA SA SA 00 00 00 00 00 00 02 00 00 04 70 00 IV 00 00 00 00 ?? 00 00 00";
    }

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    public byte getInverterCode(){
        return get(4);
    }

    @Override
    public List<Class> responseClass() {
        List<Class> responses = new ArrayList<Class>();
        responses.add(Login1Response.class);
        responses.add(Login2Response.class);
        responses.add(Login3Response.class);
        return responses;
    }


}

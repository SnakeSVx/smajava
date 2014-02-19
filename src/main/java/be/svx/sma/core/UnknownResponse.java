package be.svx.sma.core;

/**
 * Created by Stijn on 19/02/14.
 */
public class UnknownResponse extends SMAPacket implements Response {


    public UnknownResponse(byte[] data) {
        super(data);
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getPacketFormat() {
        return "UNKNOWN";
    }
}

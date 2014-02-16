package be.svx.sma.core;

/**
 * Created by Stijn on 16/02/14.
 */
public class SignalStrengthResponse extends SMAPacket implements Response {

    public SignalStrengthResponse(byte[] data) {
        super(data);
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getPacketFormat() {
        return "7e 18 00 66 SA SA SA SA SA SA 00 00 00 00 00 00 04 00 05 00 00 00 ## 00 ";
    }

    public float getSignalStrength(){
        return ((float)(get(4) & 0xFF)/(float)0xFF)*100;
    }
}

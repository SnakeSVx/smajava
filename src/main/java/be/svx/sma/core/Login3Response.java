package be.svx.sma.core;

/**
 * Created by Stijn on 16/02/14.
 */
public class Login3Response extends SMAPacket implements Response {

    public Login3Response(byte[] data) {
        super(data);
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public String getPacketFormat() {
        return "7e 1f 00 61 SA SA SA SA SA SA 00 00 00 00 00 00 0a 00 SA SA SA SA SA SA 01 DA DA DA DA DA DA 00 02 01";
    }

}

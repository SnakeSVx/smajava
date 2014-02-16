package be.svx.sma.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stijn on 16/02/14.
 */
public abstract class SMAL2Packet extends SMAPacket {

    private List<SMAL2Packet> packets;

    protected SMAL2Packet() {
        packets = new ArrayList<SMAL2Packet>();
    }

    protected SMAL2Packet(byte[] data) {
        super(data);
        packets = new ArrayList<SMAL2Packet>();
    }

    public void addPacket(SMAL2Packet packet){
        packets.add(packet);
    }


}

package be.svx.sma.core;

/**
 * Created by Stijn on 16/02/14.
 */
public interface Packet {

    byte[] getContent();

    byte[] getHeader();

    byte[] getSource();

    byte[] getDestination();

    void setSource(byte[] source);

    void setDestination(byte[] destination);

    boolean isValid();

    byte[] getData();

    byte get(int index);

    String getPacketFormat();


}

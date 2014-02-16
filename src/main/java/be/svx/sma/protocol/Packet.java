package be.svx.sma.protocol;

import be.svx.sma.core.SMAException;

import java.util.Arrays;

/**
 * Created by Stijn on 7/02/14.
 */
public class Packet {

    /* L2 Partial Packet example 5000TL-21
        7E 6D 00 13 D7 6F A6 25 80 00 19 2D 0F DC 1B 00 08 00
        7E Head
        FF 03 60 65 Header
        1B Packet length
        D0 Destination Adress header
        78 00 73 14 D7 3A Destination Adress
        00 Padding
        A0 Source Address header
        8A 00 7D 5E 1B F9 Source Address
        7D 5E Mystery 1
        00 Acknowledge
        00 Mystery 2
        00 Telegram number
        00 Mystery 3
        00 Counter
        00 Command group 1
        0B Command group 2
        80 Command group 3
        01 02 Command
            00 70 A4 0A 00 00 A9 0A 00 00
            FC 0D FD 52 0D F5 00 00 00 00 00 00 28 0F FD 52 0D F5 00 00 00 00 00 00
            54 10 FD 52 0D F5 00 00 00 00 00 00 80 7D 31 FD 52 0D F5 00 00
        00 00 FCS????
        00 Footer

       L2 Packet (0100) example

        7E 30 00 4E D7 6F A6 25 80 00 19 2D 0F DC 1B 00 01 00
        00 Head
        AC 7D 32 FD  Header
        52 Packet length
        0D Destination Adress header
        F5 00 00 00 00 00 Destination Adress
        00 Source Address header
        D8 7D 33 FD 52 0D Source Adress
        F5 Mystery 1
        00 Acknowledge
        00 Mystery 2
        00 Counter
        00 Command group 1
        00 command group 2
        00 command group 3
        66 1E Command or FCS??
        7E footer



     */

    public static final String LEVEL1_FORMAT = "7E L1 L2 CS SA SA SA SA SA SA DA DA DA DA DA DA CB CB DATA";
    public static final String LEVEL2_FORMAT = "7E L1 L2 CS SA SA SA SA SA SA DA DA DA DA DA DA CB CB 7E FF 03 60 65 L3 DB SN SN SN SN SN SN 00 SB SN SN SN SN SN M1 AK M2 TN M3 C# C1 C2 C3 CB CB DATA(-3 bytes) FC FC 7E";


    //REQUEST
    public final static byte[] REQUEST_INFORMATION_COMMAND = new byte[]{0x03, 0x00};

    //RESPONSE/REQUEST
    public final static byte[] LOGIN_REQUEST_COMMAND = new byte[]{0x02, 0x00};


    //RESPONSE
    public final static byte[] RESPONSE_INFORMATION_COMMAND = new byte[]{0x04, 0x00};
    public final static byte[] L2_PACKET_END_COMMAND = new byte[]{0x01, 0x00};
    public final static byte[] L2_PART_PACKET_COMMAND = new byte[]{0x08, 0x00};
    public final static byte[] ERROR_COMMAND = new byte[]{0x07, 0x00};

    public final static byte[] LOGIN_PART1_COMMAND = new byte[]{0x0a, 0x00};
    public final static byte[] LOGIN_PART2_COMMAND = new byte[]{0x0c, 0x00};
    public final static byte[] LOGIN_PART3_COMMAND = new byte[]{0x05, 0x00};

    public final static byte[][] VALID_REQUEST_COMMANDS = new byte[][]{
            REQUEST_INFORMATION_COMMAND
    };

    private byte[] header;
    private byte[] content;

    public Packet(){
        header = new byte[18];
        header[0] = 0x7E;
        header[1] = 0x00;
        header[2] = 0x00;
        header[3] = 0x00;
        this.content = new byte[0];
    }

    public Packet(byte[] response){
        header = new byte[18];
        for(int i = 0; i < 18; i++){
            header[i] = response[i];
        }
        content = new byte[response.length - 18];
        for(int i = 18; i < response.length ; i++){
            content[i-18] = response[i];
        }
    }

    /*
        LEVEL 1
     */

    public byte getStartByte(){
        return header[0];
    }

    public byte getPacketLength(){
        return header[1];
    }

    public byte getSecondaryPacketLength(){
        return header[2];
    }

    public byte getCheckSum(){
        return header[3];
    }

    public byte[] getSource(){
        byte[] source = new byte[6];
        source[0] = header[4];
        source[1] = header[5];
        source[2] = header[6];
        source[3] = header[7];
        source[4] = header[8];
        source[5] = header[9];
        return source;
    }

    public byte[] getDestination(){
        byte[] destination = new byte[6];
        destination[0] = header[10];
        destination[1] = header[11];
        destination[2] = header[12];
        destination[3] = header[13];
        destination[4] = header[14];
        destination[5] = header[15];
        return destination;
    }

    public byte[] getCommandBytes(){
        byte[] command = new byte[2];
        command[0] = header[16];
        command[1] = header[17];
        return command;
    }

    public byte[] getContent(){
        return content.clone();
    }

    public byte[] getHeader(){
        return header.clone();
    }

    private void setPacketLength(byte size){
        if(size < 0x12 || size > 0x6D){
            throw new SMAException("Packet size is invalid");
        }
        header[1] = size;
    }

    public void setCommand(byte[] command){
        boolean found = false;
        for(byte[] toCheck : VALID_REQUEST_COMMANDS){
            if(Arrays.equals(toCheck, command)){
                found = true;
                break;
            }
        }
        if(!found){
            throw new SMAException("Invalid Request Command");
        }
        header[16] = command[0];
        header[17] = command[1];
    }

    public void setSource(byte[] source){
        header[4] = source[0];
        header[5] = source[1];
        header[6] = source[2];
        header[7] = source[3];
        header[8] = source[4];
        header[9] = source[5];
    }

    public void setDestination(byte[] destination){
        header[10] = destination[0];
        header[11] = destination[1];
        header[12] = destination[2];
        header[13] = destination[3];
        header[14] = destination[4];
        header[15] = destination[5];
    }

    public boolean isValid(){
        return (getStartByte() ^ getPacketLength() ^ getSecondaryPacketLength()) == getCheckSum();
    }

    public PacketLevel getLevel(){
        if(isCommand(L2_PACKET_END_COMMAND) ||isCommand(L2_PART_PACKET_COMMAND)){
            return PacketLevel.LEVEL2;
        }
        return PacketLevel.LEVEL1;
    }

    public boolean isCommand(byte[] command){
        return Arrays.equals(getCommandBytes(), command);
    }

    public byte getByte(int index){
        return content[index];
    }

    public byte[] getPacket(){
        byte[] fullPackage = new byte[18 + content.length];
        for(int i = 0; i < header.length ;i++){
            fullPackage[i] = header[i];
        }
        for(int i = 0; i < content.length;i++){
            fullPackage[18 + i] = content[i];
        }
        return fullPackage;
    }

    public void setContent(byte[] content){
        this.content = content;
        setPacketLength((byte) (18 + content.length));
        setCheckSum((byte)(0x7E ^ getPacketLength() ^ getSecondaryPacketLength()));
    }

    private void setCheckSum(byte b) {
        header[3] = b;
    }

    public String getFormat() {
        String format;
        if(getLevel() == PacketLevel.LEVEL1){
          format = LEVEL1_FORMAT;
        } else {
          format = LEVEL2_FORMAT;
        }
        return format;
    }

    /*
        LEVEL 2
     */




}

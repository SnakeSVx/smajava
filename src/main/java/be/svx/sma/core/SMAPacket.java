package be.svx.sma.core;

import be.svx.sma.util.Util;

/**
 * Created by Stijn on 16/02/14.
 */
public abstract class SMAPacket implements Packet {

    private static final String LEVEL1_FORMAT = "7E L1 L2 CS SA SA SA SA SA SA DA DA DA DA DA DA CB CB DATA";

    protected byte[] header;
    protected byte[] content;

    public SMAPacket(){
        header = new byte[18];
        header[0] = 0x7E; //StartByte, always 0x7E
        header[1] = 0x00; //Low Length
        header[2] = 0x00; //High length (requires * 0xFF)
        header[3] = 0x00; //CheckSum (StartByte ^ Low Len ^ High Len)

        content = new byte[0];
    }

    public SMAPacket(byte[] data){
        if(data[0] != 0x7E || data.length < 18){
            throw new SMAException("Invalid SMA Packet!");
        }
        data = Util.removeEscapeBytes(data);
        header = new byte[18];
        for(int i = 0; i < 18; i++){
            header[i] = data[i];
        }
        content = new byte[data.length - 18];
        for(int i = 18; i < data.length ; i++){
            content[i-18] = data[i];
        }
    }

    protected byte getPacketLength(){
        return header[1];
    }

    protected byte getSecondaryPacketLength(){
        return header[2];
    }

    protected byte getCheckSum(){
        return header[3];
    }

    protected void setCheckSum(byte b) {
        header[3] = b;
    }

    protected byte[] getCommand(){
        byte[] command = new byte[2];
        command[0] = header[16];
        command[1] = header[17];
        return command;
    }

    protected void setPacketLength(byte size){
        if(size < 0x12 || size > 0x6D){
            throw new SMAException("Packet size is invalid");
        }
        header[1] = size;
    }

    protected void setCommand(byte[] command){
        header[16] = command[0];
        header[17] = command[1];
    }

    protected void calculateLengthAndCheckSum(){
        setPacketLength((byte) (18 + content.length));
        setCheckSum((byte)(0x7E ^ getPacketLength() ^ getSecondaryPacketLength()));
    }

    /*
        Public
     */


    public byte[] getContent(){
        return content.clone();
    }

    public byte[] getHeader(){
        return header.clone();
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
        return (0x7E ^ getPacketLength() ^ getSecondaryPacketLength()) == getCheckSum();
    }

    public byte[] getData(){
        calculateLengthAndCheckSum();
        byte[] fullPackage = new byte[18 + content.length];
        for(int i = 0; i < header.length ;i++){
            fullPackage[i] = header[i];
        }
        for(int i = 0; i < content.length;i++){
            fullPackage[18 + i] = content[i];
        }
        return fullPackage;
    }

    public byte get(int index){
        return content[index];
    }


    public static SMAPacket createPacket(byte[] data){
        SMAPacket packet = null;
        switch(data[16]){
            case 0x02:
               packet = new LoginRequest(data);
               break;
            case 0x0a:
               packet = new Login1Response(data);
               break;
            case 0x0c:
               packet = new Login2Response(data);
               break;
            case 0x05:
               packet = new Login3Response(data);
               break;
            case 0x07: //Error Response
                break;
            case 0x01: //L2 Packet
                break;
            case 0x08: //L2 Part Packet
                break;
            case 0x04: //Response to request (0x03)
                switch(data[18]){
                    case 0x05:
                        packet = new SignalStrengthResponse(data);
                        break;
                    case 0x01:
                        packet = new Basic1Response(data);
                        break;
                    case 0x02:
                        packet = new Basic2Response(data);
                        break;
                    default:
                        packet = new UnknownResponse(data);
                        break;
                }
                break;
            default:
                packet = new UnknownResponse(data);
                break;
        }
        return packet;
    }
}

package be.svx.smajava.engine;

/**
 * A packet consists of
 * 1 start byte 0x7E
 * 1 Len byte (1) = total amount of bytes in packet
 * 1 Len byte (2) = UNKNOWN
 * 1 checksum byte = start byte ^ len1 ^ len2
 * 6 lvl 1 Source Address bytes
 * 6 lvl 1 Destination Address bytes
 * 2 Command Code Bytes
 * # content
 *
 * Created by Stijn on 6/02/14.
 */
public final class Packet {

    private byte startByte;
    private byte len1;
    private byte len2;
    private byte checksum;
    private byte[] source;
    private byte[] destination;
    private byte[] command;
    //TO CHECK AFTER THIS
    private byte[] content;

    private boolean canModify;

    public Packet(){
        this.startByte = 0x7E;
        this.source = new byte[6];
        this.destination = new byte[6];
        this.command = new byte[2];
        this.len1 = 0;
        this.len2 = 0;
        this.checksum = 0;
        this.source[0] = 0;
        this.source[1] = 0;
        this.source[2] = 0;
        this.source[3] = 0;
        this.source[4] = 0;
        this.source[5] = 0;

        this.destination[0] = 0;
        this.destination[1] = 0;
        this.destination[2] = 0;
        this.destination[3] = 0;
        this.destination[4] = 0;
        this.destination[5] = 0;

        this.command[0] = 0;
        this.command[1] = 0;

        canModify = true;
    }

    public Packet(byte[] packet){
        this();

        canModify = false;

        this.startByte = packet[0];
        this.len1 = packet[1];
        this.len2 = packet[2];
        this.checksum = packet[3];

        this.source[0] = packet[4];
        this.source[1] = packet[5];
        this.source[2] = packet[6];
        this.source[3] = packet[7];
        this.source[4] = packet[8];
        this.source[5] = packet[9];

        this.destination[0] = packet[10];
        this.destination[1] = packet[11];
        this.destination[2] = packet[12];
        this.destination[3] = packet[13];
        this.destination[4] = packet[14];
        this.destination[5] = packet[15];

        this.command[0] = packet[16];
        this.command[1] = packet[17];

        if(packet.length > 18) {
            this.content = new byte[packet.length - 18];
            for(int i = 18; i < packet.length; i++){
                this.content[i-18] = packet[i];
            }
        } else {
            this.content = new byte[0];
        }

    }

    public byte getStartByte() {
        return startByte;
    }

    public byte getLen1() {
        if(canModify){
            return (byte)(18 + content.length);
        }
        return len1;
    }

    public byte getLen2() {
        //TODO calculate this?
        return len2;
    }

    public byte getChecksum() {
        if(canModify){
            return (byte)(startByte ^ getLen1() ^ getLen2());
        }
        return checksum;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        if(source.length != 6){
            throw new RuntimeException("Wrong parameter size");
        }
        this.source = source;
    }

    public byte[] getDestination() {
        return destination;
    }

    public void setDestination(byte[] destination) {
        if(destination.length != 6){
            throw new RuntimeException("Wrong parameter size");
        }
        this.destination = destination;
    }

    public byte[] getCommand() {
        return command;
    }

    public void setCommand(byte[] command) {
        if(command.length != 2){
            throw new RuntimeException("Wrong parameter size");
        }
        this.command = command;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;

        //TODO calculate len1, len2 + checksum
    }


    public boolean isValid(){
        return (startByte ^ len1 ^ len2) == checksum;
    }

    /**
     * This should be an unsiged int, but because java can't handle it we will use a regular int for now
     * @return
     */
    public int getCommandCode(){
       return command[0] + (command[1] * 256);
    }

    public byte[] toBytes(){
        byte[] total = new byte[18 + content.length];
        total[0] = startByte;
        total[1] = getLen1();
        total[2] = getLen2();
        total[3] = getChecksum();
        total[4] = source[0];
        total[5] = source[1];
        total[6] = source[2];
        total[7] = source[3];
        total[8] = source[4];
        total[9] = source[5];
        total[10] = destination[0];
        total[11] = destination[1];
        total[12] = destination[2];
        total[13] = destination[3];
        total[14] = destination[4];
        total[15] = destination[5];
        total[16] = command[0];
        total[17] = command[1];
        for(int i = 0; i < content.length ; i++){
            total[18 + i] = content[i];
        }
        return total;
    }
}

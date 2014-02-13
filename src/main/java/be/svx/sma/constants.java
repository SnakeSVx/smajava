package be.svx.sma;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stijn on 9/02/14.
 */
public class Constants {
    public final static byte MIN = 0x00;
    public final static byte MAX = (byte) 0xFF;

    public final static byte L1_START_BYTE = 0x7E;
    public final static byte L2_START_BYTE = L1_START_BYTE;
    public final static byte L2_END_BYTE = 0x7E;
    public final static byte[] BROADCAST_ADRESS = new byte[]{MIN, MIN, MIN, MIN, MIN, MIN};
    public final static byte[] UNKNOWN_ADRESS = new byte[]{MAX, MAX, MAX, MAX, MAX, MAX};
    public final static byte L1_MAX_PACKET_SIZE = 0x6D;
    public final static byte L1_MIN_PACKET_SIZE = 0x12;
    public final static byte L2_MIN_PACKET_SIZE = 0x07;

    /*
        L1 Commands
     */
    public final static Map<String, Byte[]> L1_REQUEST_COMMANDS = new HashMap<String, Byte[]>();
    public final static Map<Byte[], Byte[]> L1_REQUEST_COMMANDS_DATA = new HashMap<Byte[], Byte[]>();
    static {
        L1_REQUEST_COMMANDS.put("UNKNOWN1", new Byte[]{0x01, 0x00} );
        L1_REQUEST_COMMANDS.put("UNKNOWN2", new Byte[]{0x02, 0x00} );
        L1_REQUEST_COMMANDS.put("SIGNAL", new Byte[]{0x05, 0x00} );
        L1_REQUEST_COMMANDS.put("UNKNOWN3", new Byte[]{(byte)0xAC, 0x00} );

        L1_REQUEST_COMMANDS_DATA.put(new Byte[]{0x01, 0x00}, new Byte[]{0x01}); //UNKNOWN, Response should be 0x000001
        L1_REQUEST_COMMANDS_DATA.put(new Byte[]{0x02, 0x00}, new Byte[]{}); //UNKNOWN, Response should be 0x000020FD
        L1_REQUEST_COMMANDS_DATA.put(new Byte[]{0x05, 0x00}, new Byte[]{}); //Signal Strength, Response should be signal strength (XX): 0x0000XX00 (XX * 100/256)
        L1_REQUEST_COMMANDS_DATA.put(new Byte[]{(byte)0xAC, 0x00}, new Byte[]{(byte) 0xAC}); //UNKNOWN: Response should be 0x0000AC
    }

    //REQUEST
    public final static byte[] REQUEST_INFORMATION_COMMAND = new byte[]{0x03, 0x00};

    //RESPONSE/REQUEST
    public final static byte[] LOGIN_REQUEST_COMMAND = new byte[]{0x02, 0x00};


    //RESPONSE
    public final static byte[] RESPONSE_INFORMATION_COMMAND = new byte[]{0x04, 0x00};

    //L2
    public final static byte[] L2_PACKET_END_COMMAND = new byte[]{0x01, 0x00};
    public final static byte[] L2_PART_PACKET_COMMAND = new byte[]{0x08, 0x00};

    //ERROR
    public final static byte[] ERROR_COMMAND = new byte[]{0x07, 0x00};

    //LOGIN/INIT
    public final static byte[] LOGIN_PART1_COMMAND = new byte[]{0x0a, 0x00};
    public final static byte[] LOGIN_PART2_COMMAND = new byte[]{0x0c, 0x00};
    public final static byte[] LOGIN_PART3_COMMAND = new byte[]{0x05, 0x00};


    /*
        L2 COMMANDS
     */
}

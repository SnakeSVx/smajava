package be.svx;

import be.svx.smajava.engine.Packet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by Stijn on 6/02/14.
 */
@RunWith(JUnit4.class)
public class TestPacket {


    @Test
    public void testPacketValidation() {
        byte[] bytes = new byte[18];
        bytes[0] = 0x7e; //StartByte
        bytes[1] = 0x1F; //Len 1
        bytes[2] = 0x00; //Len 2
        bytes[3] = 0x61; //Checksum

        Packet packet = new Packet(bytes);
        Assert.assertTrue(packet.isValid());
    }

    @Test
    public void testPacketLen1Len2CheckSumCalculation(){
        Packet packet = new Packet();
        packet.setContent(new byte[13]);

        Assert.assertTrue(packet.getLen1() == 0x1F);
        Assert.assertTrue(packet.getLen2() == 0x00);
        Assert.assertTrue(packet.getChecksum() == 0x61);
    }

}

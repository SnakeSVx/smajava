package be.svx.sma;

import be.geek.smajava.Log;
import be.svx.sma.protocol.SMAException;

import javax.xml.bind.DatatypeConverter;
import java.util.concurrent.*;

/**
 * Created by Stijn on 7/02/14.
 */
public class Util {

    public static byte[] convertAddressToBytes(String address){
        String[] codes = address.split("(?<=\\G..)");
        byte[] result = new byte[codes.length];
        int cntr = 0;
        for (int i = codes.length - 1; i >= 0; i--) {
            result[cntr] = encodeHexString(codes[i]);
            cntr++;
        }
        return result;
    }

    public static byte encodeHexString(String hexCode) {
        return DatatypeConverter.parseHexBinary(hexCode)[0];
    }

    public static int readForCallable(ExecutorService executor, Callable callable){
        int read = 1;
        Future<Integer> future = executor.submit(callable);
        try {
            read = future.get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            throw new SMAException(ex.getMessage());
        } catch (ExecutionException ex) {
            throw new SMAException(ex.getMessage());
        } catch (TimeoutException ex) {
            throw new SMAException(ex.getMessage()); //SmajavaException.Type.BLUETOOTH_RESET
        }
        return read;
    }

    public static byte[] addToBuffer(byte[] dest, byte[] toAdd, int cntr) {
        for (int i = 0; i < toAdd.length && i < dest.length; i++) {
            dest[cntr] = toAdd[i];
            cntr++;
        }
        return dest;
    }

    public static byte[] fix_length_received(byte[] received, int len) {
        if (received[1] != len) {
            if ((received[3] != 0x13) && (received[3] != 0x14)) {
                int sum = received[1] + received[3];
                received[1] = (byte) (len);
                switch (received[1]) {
                    case 0x52:
                        received[3] = 0x2c;
                        break;
                    case 0x5a:
                        received[3] = 0x24;
                        break;
                    case 0x66:
                        received[3] = 0x1a;
                        break;
                    case 0x6a:
                        received[3] = 0x14;
                        break;
                    default:
                        received[3] = (byte) (sum - received[1]);
                        break;
                }
            }
        }
        return received;
    }
}

package be.geek.smajava;

import be.svx.sma.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Read instructions file and dispatch to sender, receiver or extractor.
 * @author geek
 */
public class Engine {
    
    /**
     * Maximum number of tries while receiving data that did not match the instructions file
     */
    private static final int MAX_READ_RETRIES = 4;
    
    /**
     * Maximum number of tries while failing to extract data 
     */
    private static final int MAX_EXTRACTING_RETRIES = 60;
    
    /**
     * Encodes data to send, data which should be received and matched, decodes for extraction
     */
    private Codec encoder = new Codec();
    
    public void process(Inverter inverter) throws IOException, SmajavaException, InterruptedException {        
        InputStream inputStream = Engine.class.getResourceAsStream(Configuration.getDataFilename());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        
        String line;
        byte[] received = null;
        int linecntr = 0;
        int resettedline = 0;
        int tries = 0;
        while ((line = bufferedReader.readLine()) != null) {
            linecntr++;
            if (line.startsWith("S")) {
                send(line);
            } else if (line.startsWith("R")) {
                received = receive(line);
            } else if (line.startsWith("E")) {
                try {
                    extract(line, received);
                } catch (SmajavaException e) {
                    if (e.getType() == SmajavaException.Type.BLUETOOTH_RESET) {
                        tries++;
                        if (tries > MAX_EXTRACTING_RETRIES) {
                            throw e;
                        }
                        bufferedReader.reset();
                        linecntr = resettedline;
                    } else {
                        throw e;
                    }
                }
            } else if (line.startsWith(":")) {
                // mark at this position to return to this point if reads fail
                bufferedReader.mark(2048);
                resettedline = linecntr;
            }    
            Log.info(this, "line: " + linecntr);
        }
        Log.info(this, "The End");
        bufferedReader.close();
    }
    
    public void send(String line) throws SmajavaException, IOException {
        Log.debug(this, "Sending this: "+line);
        byte[] tosend = encoder.encode(line);
        Configuration.getInverter().send(tosend);
    }
    
    public byte[] receive(String line) throws IOException, SmajavaException {
        Log.debug(this, "Receiving this: "+line);
        String[] tokens = line.substring(2, line.length() - 1).split("\\s+");
        byte[] expected = encoder.encode(line);
        
        boolean found = false;
        int retries = 0;
        byte[] received = null;
        while (!found) {
            ByteResult br = Configuration.getInverter().receive();
            received = br.getResult();

            byte[] tocompareBytesRead = Arrays.copyOfRange(received, 4, expected.length);
            byte[] tocompareBytesExpd = Arrays.copyOfRange(expected, 4, expected.length);
            if (Arrays.equals(tocompareBytesRead, tocompareBytesExpd)) {        
                Log.debug(this, "Found expected data");
                found = true;
            } else {
                Log.debug(this, "Did not find expected data");
                Log.debugBytes(this, "expected ", tocompareBytesExpd);
                Log.debugBytes(this, "received ", tocompareBytesRead);
                retries++;
                if (retries > MAX_READ_RETRIES) {
                    throw new SmajavaException("Could not receive expected data from line: "+line);
                }
            }
        }
        return received;
    }

    private void extract(String line, byte[] received) throws SmajavaException, IOException, InterruptedException {
        Log.debug(this, "Extracting this: "+line);
        encoder.decode(line, received);
    }
    
}

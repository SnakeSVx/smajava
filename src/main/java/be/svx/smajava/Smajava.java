package be.svx.smajava;

import be.geek.smajava.Configuration;
import be.geek.smajava.Inverter;
import be.svx.sma.util.Log;
import be.geek.smajava.SmajavaException;
import be.svx.smajava.engine.Engine;
import be.svx.smajava.engine.FaultyResponseException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Stijn on 5/02/14.
 */
public class Smajava {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Log.info("main", "Starting smajava 2");

        if (args.length  == 0) {
            Log.error(Smajava.class, "Missing inverter address");
            System.exit(-1);
        }

        String inverterAddress = args[0];

        if (args.length > 1 && args[1].length() > 0) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date fromDate = dateFormat.parse(args[1]);
                Log.debug(Smajava.class, "from date: "+fromDate);
                Configuration.getEnvironment().setFromtime(fromDate.getTime() / 1000);
            } catch (ParseException ex) {
                Log.error(Smajava.class, "Could not parse fromdate arg: "+args[1], ex);
                System.exit(-1);
            }
        }

        if (args.length > 2) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date fromDate = dateFormat.parse(args[1]);
                Calendar cal = Calendar.getInstance();
                cal.setTime(fromDate);
                cal.set(Calendar.HOUR, 23);
                fromDate = cal.getTime();
                Log.debug(Smajava.class, "to date: "+fromDate);
                Configuration.getEnvironment().setTotime(fromDate.getTime() / 1000);
            } catch (ParseException ex) {
                Log.error(Smajava.class, "Could not parse todate arg: "+args[1], ex);
                System.exit(-1);
            }
        }

        Inverter inverter = new Inverter("5000TL21", inverterAddress);
        inverter.setPassword("0000");

        Engine engine = new Engine(inverter);
        try {
            engine.open();
        } catch (SmajavaException e) {
            e.printStackTrace();
        } catch (FaultyResponseException e) {
            e.printStackTrace();
        } finally {
            engine.close();
        }


    }

}

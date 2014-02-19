/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.geek.smajava;

import be.svx.sma.util.Log;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author geek
 */
public class Smajava {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {        
        Log.info("main", "Starting smajava");
        
        Configuration.init();        
        
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
        
        Configuration.setDataFilename("/be/geek/smajava/sma.in.new");
        Inverter inverter = new Inverter("5000TL21", inverterAddress);
        inverter.setPassword("0000");
        Configuration.setInverter(inverter);        
        
        Engine engine = new Engine();
        try {
            Configuration.getInverter().openConnection();
            engine.process(inverter);
            Configuration.getInverter().closeConnection();
        } catch (Exception ex) {
            Log.error("Smajava", ex.getMessage(), ex);
            System.exit(-1);
        }
    }
}

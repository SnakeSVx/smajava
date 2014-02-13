/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package be.geek.smajava;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author geek
 */
public class Log {

    public static void initLogging(){
        try {
            InputStream fis =  Log.class.getClassLoader().getResourceAsStream("logging.properties");
            LogManager.getLogManager().readConfiguration(fis);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void info(Object object, String text) {
        Logger.getLogger(object.getClass().getName()).info(text);
    }
    
    public static void info(Object object, String text, Object... values) {
        String message = String.format(text, values);
        Logger.getLogger(object.getClass().getName()).info(message);
    }
    
    
    public static void debug(Object object, String text) {
        Logger.getLogger(object.getClass().getName()).fine(text);
    }    
    
    public static void debug(Object object, String text, Object... vars) {
        String message = String.format(text, vars);
        Logger.getLogger(object.getClass().getName()).fine(message);
    }
    
    public static boolean isDebug() {
        return Logger.getAnonymousLogger().getLevel() == Level.FINE;
    }
    
    public static void debugBytes(Object object, String message, byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder(message);
        for (int i = 0; i < bytes.length; i++) {
            stringBuilder.append(String.format("%02x ", bytes[i]));
        }
        Log.info(object, stringBuilder.toString());
    }

    public static void error(Object object, String string, Exception ex) {
        Logger.getLogger(object.getClass().getName()).log(Level.SEVERE, string, ex);
    }
    
    public static void error(Object object, String message) {
        Logger.getLogger(object.getClass().getName()).log(Level.SEVERE, message);
    }
}

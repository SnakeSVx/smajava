package be.svx.smajava;

import be.geek.smajava.*;

import java.io.IOException;

/**
 * Created by Stijn on 5/02/14.
 */
public class TestRunner1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] arguments = new String[]{"008025A66FD7"};

        be.geek.smajava.Smajava.main(arguments);
        //Thread.sleep(2000);
        Smajava.main(arguments);
    }
}

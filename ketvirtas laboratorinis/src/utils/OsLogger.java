package utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by irmis on 2017.03.31.
 */
public class OsLogger {
    public static BufferedWriter log = null;
    public static void init(String filename) throws IOException {
        if(log == null) {
            FileWriter temp = new FileWriter(filename);
            log = new BufferedWriter(temp);
        }
    }
    public static void writeToLog(String msg){
        if(log != null) {
            try {
                log.write(msg);
                log.newLine();
                log.flush();
            } catch (IOException e) {
                System.out.println("Unable to write to file");
            }
        } else{
            System.out.println("Logger is not open");
        }
    }
    public static void close() throws IOException {
        if(log != null){
            log.close();
        }
    }
}

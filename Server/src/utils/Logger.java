package utils;

import java.io.FileWriter;
import java.io.IOException;


public class Logger {
    private static final String LOG_FILE = "application_time_log.txt";

    /**
     * Logs a message to the application log file
     * @param message
     */
    public static void log(String message) {
        try {
            FileWriter writer = new FileWriter(LOG_FILE, true);
            writer.write(message + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

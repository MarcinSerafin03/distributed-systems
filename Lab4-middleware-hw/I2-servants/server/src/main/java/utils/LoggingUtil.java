package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggingUtil {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static void log(String source, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[" + timestamp + "] [" + source + "] " + message);
    }
}
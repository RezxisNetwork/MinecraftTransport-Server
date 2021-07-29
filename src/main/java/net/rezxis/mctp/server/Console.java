package net.rezxis.mctp.server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Console {

    private static SimpleDateFormat sdf = new SimpleDateFormat("[MMM d, y, h:m:s a]");

    public static void info(String message) {
        System.out.println(sdf.format(new Date())+" INFO: "+message);
    }

    public static void error(String message) {
        System.out.println(sdf.format(new Date())+" ERROR: "+message);
    }
}

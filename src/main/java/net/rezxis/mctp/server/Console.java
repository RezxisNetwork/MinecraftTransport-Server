package net.rezxis.mctp.server;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Console {

    private static SimpleDateFormat sdf = new SimpleDateFormat("[MMM d, y, h:m:s a]");
    private static PrintWriter pw;

    public static void info(String message) {
        System.out.println(sdf.format(new Date())+" INFO: "+message);
        pw.println(sdf.format(new Date())+" INFO: "+message);
        pw.flush();
    }

    public static void error(String message) {
        System.out.println(sdf.format(new Date())+" ERROR: "+message);
        pw.println(sdf.format(new Date())+" INFO: "+message);
        pw.flush();
    }

    public static void exception(Throwable ex) {
        ex.printStackTrace();
        ex.printStackTrace(pw);
        pw.flush();
    }

    static {
        try {
            pw = new PrintWriter(new File("mctp.log"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

package net.rezxis.mctp.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.URL;

public class MCTPConfig {

    public static MCTPConfig instance;

    public String listen_host;
    public int listen_port;
    public String host;
    public int port_start;
    public int port_range;

    public static void load(File file) throws Exception {
        if (!file.exists()) {
            Console.info("generating default configuration file.");
            generate(file);
            return;
        }
        instance = new Gson().fromJson(new FileReader(file), MCTPConfig.class);
    }

    public static void generate(File file) throws Exception {
        instance = new MCTPConfig();
        instance.listen_host = "0.0.0.0";
        instance.listen_port = 9998;
        instance.port_start = 40000;
        instance.port_range = 10000;
        instance.host = getMyIp();
        file.createNewFile();
        PrintWriter pw = new PrintWriter(file);
        pw.println(new GsonBuilder().setPrettyPrinting().create().toJson(instance));
        pw.close();
    }

    private static String getMyIp() throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new URL("http://checkip.amazonaws.com").openStream()));

            return br.readLine();
        } catch (Exception e) {
            throw e;
        }
    }
}

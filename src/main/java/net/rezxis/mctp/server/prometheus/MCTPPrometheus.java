package net.rezxis.mctp.server.prometheus;

import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import net.rezxis.mctp.server.Console;
import net.rezxis.mctp.server.MCTPConfig;

import java.io.IOException;

public class MCTPPrometheus implements Runnable {

    public static MCTPPrometheus instance;
    private Gauge connectedMCTPServers;
    private Gauge connectedSessions;

    @Override
    public void run() {
        instance = this;
        if (!MCTPConfig.instance.enable_prometheus_metrics)
            return;
        this.connectedMCTPServers = Gauge.build().name("connected_mctp").help("connected mctp servers").register();
        this.connectedSessions = Gauge.build().name("connected_sessions").help("connected sessions").register();
        try {
            HTTPServer server = new HTTPServer(MCTPConfig.instance.prometheus_metrics_host, MCTPConfig.instance.prometheus_metrics_port);
        } catch (IOException e) {
            Console.exception(e);
        }
    }

    public void increaseConnectedMCTPServers() {
        if (MCTPConfig.instance.enable_prometheus_metrics)
            this.connectedMCTPServers.inc();
    }

    public void decreaseConnectedMCTPServers() {
        if (MCTPConfig.instance.enable_prometheus_metrics)
            this.connectedMCTPServers.dec();
    }

    public void increaseConnectedSessions() {
        if (MCTPConfig.instance.enable_prometheus_metrics)
            this.connectedSessions.inc();
    }

    public void decreaseConnectedSessions() {
        if (MCTPConfig.instance.enable_prometheus_metrics)
            this.connectedSessions.dec();
    }
}

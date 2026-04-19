package org.example.loadbalancer.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Handler for exposing Prometheus metrics in text format */
public class MetricsHandler implements HttpHandler {

  private final PrometheusMeterRegistry meterRegistry;

  public MetricsHandler(PrometheusMeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String response = scrapeMetrics();
    exchange.getResponseHeaders().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
    exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

    try (OutputStream os = exchange.getResponseBody()) {
      os.write(response.getBytes(StandardCharsets.UTF_8));
    }
  }

  /** Scrape metrics from Prometheus registry */
  private String scrapeMetrics() {
    return meterRegistry.scrape();
  }

  /** Generate simple metrics output for Simple registry */
  private String generateMetricsOutput() {
    StringBuilder sb = new StringBuilder();
    sb.append("# HELP lb_http_requests_total Total HTTP requests received by the load balancer\n");
    sb.append("# TYPE lb_http_requests_total counter\n");
    sb.append("lb_http_requests_total{load_balancer=\"primary\"} 0.0\n\n");
    sb.append("# HELP lb_total_servers Total number of servers\n");
    sb.append("# TYPE lb_total_servers gauge\n");
    sb.append("lb_total_servers{load_balancer=\"primary\"} 3.0\n\n");
    sb.append("# Metrics registry type: ").append(meterRegistry.getClass().getSimpleName()).append("\n");
    return sb.toString();
  }
}

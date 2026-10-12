package com.example.gateway.demo;

import com.example.gateway.checkout.CheckoutUpstreamService;
import com.logspectra.mdc.LogSpectraMdc;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Emits sample logs at different rates so LogSpectra UI shows mixed traffic for gateway-service.
 */
@Component
@ConditionalOnProperty(prefix = "gateway.demo-logs", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoLogEmitter {

    private static final Logger log = LoggerFactory.getLogger(DemoLogEmitter.class);

    private final DemoLogsProperties properties;
    private final CheckoutUpstreamService checkoutUpstreamService;
    private final AtomicLong fastSeq = new AtomicLong();
    private final AtomicLong mediumSeq = new AtomicLong();
    private final AtomicLong slowSeq = new AtomicLong();
    private final AtomicLong burstSeq = new AtomicLong();
    private final AtomicLong errorSeq = new AtomicLong();

    public DemoLogEmitter(DemoLogsProperties properties, CheckoutUpstreamService checkoutUpstreamService) {
        this.properties = properties;
        this.checkoutUpstreamService = checkoutUpstreamService;
    }

    @PostConstruct
    void logStartup() {
        log.info(
                "Demo log emitter active errorIntervalMs={} errorsPerTick={}",
                properties.getErrorIntervalMs(),
                properties.getErrorsPerTick());
    }

    @Scheduled(fixedRateString = "${gateway.demo-logs.fast-interval-ms:400}")
    public void fastHeartbeat() {
        LogSpectraMdc.runWithNewTrace(() -> {
            long n = fastSeq.incrementAndGet();
            log.info("gateway heartbeat seq={} path=/api/v1/health method=GET status=200", n);
        });
    }

    @Scheduled(fixedRateString = "${gateway.demo-logs.medium-interval-ms:2000}")
    public void mediumRouting() {
        LogSpectraMdc.runWithNewTrace(() -> {
            long n = mediumSeq.incrementAndGet();
            String route = n % 3 == 0 ? "catalog" : n % 3 == 1 ? "cart" : "checkout";
            log.debug("route selected route={} attempt={} upstream=inventory-service", route, n);
        });
    }

    @Scheduled(fixedRateString = "${gateway.demo-logs.slow-interval-ms:7000}")
    public void slowAudit() {
        LogSpectraMdc.runWithNewTrace(() -> {
            long n = slowSeq.incrementAndGet();
            int latencyMs = 120 + ThreadLocalRandom.current().nextInt(380);
            log.warn("downstream latency elevated service=pricing latencyMs={} auditTick={}", latencyMs, n);
        });
    }

    @Scheduled(fixedRateString = "${gateway.demo-logs.burst-interval-ms:12000}")
    public void burstOrError() {
        LogSpectraMdc.runWithNewTrace(() -> {
            long n = burstSeq.incrementAndGet();
            if (n % 2 == 0) {
                log.error(
                        "simulated payment-rail timeout correlationId=demo-{} upstream=http://payments/authorize",
                        n,
                        new IllegalStateException("read timed out after 3000ms"));
            } else {
                log.info("auth token refresh ok burst={} ttlSeconds=3600", n);
            }
        });
    }

    /** Guaranteed ERROR batch for dashboard / live SSE testing (1s buckets). */
    @Scheduled(fixedRateString = "${gateway.demo-logs.error-interval-ms:1000}")
    public void steadyErrorRate() {
        int batch = properties.getErrorsPerTick();
        for (int i = 0; i < batch; i++) {
            final int indexInBatch = i + 1;
            LogSpectraMdc.runWithNewTrace(() -> emitScheduledError(errorSeq.incrementAndGet(), indexInBatch, batch));
        }
    }

    private void emitScheduledError(long seq, int indexInBatch, int batchSize) {
        if (indexInBatch % 2 == 0) {
            int cartItemCount = seq % 2 == 0 ? 0 : 2;
            try {
                checkoutUpstreamService.forwardToCartService(cartItemCount);
            } catch (Exception ex) {
                log.error(
                        "gateway upstream failure route=/api/checkout status=502 seq={} batch={}/{} upstream=cart-service cartItems={}",
                        seq,
                        indexInBatch,
                        batchSize,
                        cartItemCount,
                        ex);
            }
        } else {
            log.error(
                    "rate limit exceeded clientId=demo-{} route=/api/catalog batch={}/{}",
                    seq,
                    indexInBatch,
                    batchSize);
        }
    }
}

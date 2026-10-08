package com.example.gateway.demo;

import com.logspectra.mdc.LogSpectraMdc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Shared logic for manual and scheduled ERROR bursts (Kafka / LogSpectra).
 */
@Service
public class DemoErrorBurstService {

    private static final Logger log = LoggerFactory.getLogger(DemoErrorBurstService.class);

    public int emitBurst(int count) {
        int n = Math.min(Math.max(count, 1), 100);
        for (int i = 0; i < n; i++) {
            final int index = i + 1;
            LogSpectraMdc.runWithNewTrace(() -> log.error(
                    "manual error burst i={}/{} route=/api/error-burst",
                    index,
                    n,
                    new IllegalStateException("burst sample " + index)));
        }
        log.info("Emitted {} ERROR log(s) for LogSpectra testing", n);
        return n;
    }
}
